/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericInitListReader;
import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.ios.essential.crud.StorageReader;
import io.frinx.cli.unit.ios.essential.crud.VersionReader;
import io.frinx.cli.unit.ios.essential.crud.VrfReader;
import io.frinx.cli.unit.ios.essential.crud.VrfWriter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.Version;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.Vrfs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VrfsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.version.Storage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.Vrf;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IosEssentialUnit implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(IosEssentialUnit.class);

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosEssentialUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new IosCliInitializer(cliNodeConfiguration, id);
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        //  VRF
        wRegistry.add(
                new GenericListWriter<>(InstanceIdentifier.create(Vrfs.class).child(Vrf.class), new VrfWriter(cli)));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // VRFs
        rRegistry.addStructuralReader(InstanceIdentifier.create(Vrfs.class), VrfsBuilder.class);
        //  VRF
        rRegistry.add(new GenericInitListReader<>(InstanceIdentifier.create(Vrfs.class).child(Vrf.class), new VrfReader(cli)));
        // Version
        rRegistry.add(new GenericReader<>(InstanceIdentifier.create(Version.class), new VersionReader(cli)));
        // Storage
        rRegistry.add(new GenericReader<>(InstanceIdentifier.create(Version.class).child(Storage.class), new StorageReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS basic translate unit";
    }

    /**
     * Initialize IOS CLI session to be usable by various CRUD and RPC handlers
     */
    private static final class IosCliInitializer implements SessionInitializationStrategy {
        private final CliNode context;
        private final RemoteDeviceId id;

        IosCliInitializer(CliNode context, RemoteDeviceId id) {
            this.context = context;
            this.id = id;
        }

        @Override
        public void accept(@Nonnull Session session, @Nonnull String newline) {
            try {
                // Enable privileged mode
                write(session, newline, "enable");
                // Fill in password
                checkArgument(context.getCredentials() instanceof LoginPassword,
                        "{}: Unable to handle credentials type of: %s", id, context.getCredentials());
                String password = ((LoginPassword) context.getCredentials()).getPassword();
                write(session, newline, password);
                // Set terminal length to 0 to prevent "--More--" situation
                write(session, newline, "terminal length 0");
                // Read the output
                session.readUntilTimeout(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (SessionException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to initialize device", id, e);
                throw new IllegalStateException(id + ": Unable to initialize device", e);
            }
        }
    }
}
