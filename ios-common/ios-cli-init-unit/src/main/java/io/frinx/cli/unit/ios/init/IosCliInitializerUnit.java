/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.init;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import java.util.Collections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPassword;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Translate unit that does not actually translate anything.
 *
 * This translate unit's only responsibility is to properly initialize IOS cli
 * session. That is, upon establishing connection to IOS device, enter privileged
 * EXEC mode by issuing the 'enable' command and filling in the secret.
 */
// TODO Once the IosCliInitializer class is fixed and can work also in a setting
// where no secret is actually required (that is also the case, when we are in
// the privileged mode already), refactor this into abstract class implementing
// TranslateLogicProvider interface. Let other units extend this interface,
// instead of creating a standalone unit just for cli initialization purposes.
public class IosCliInitializerUnit  implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(IosCliInitializerUnit.class);

    // TODO This is reused all over the units. Move this to som Util class so
    // we can reuse it.
    // TODO XR devices does not actually have privileged exec mode, nor 'enable'
    // command. For them, it should be sufficient to just configure terminal to
    // length 0.
    private static final Device IOS_XR = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private static final Device IOS = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration iosXrReg;
    private TranslationUnitCollector.Registration iosReg;


    public IosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init()
    {
        iosXrReg = registry.registerTranslateUnit(IOS_XR, this);
        iosReg = registry.registerTranslateUnit(IOS, this);
    }

    public void close() {
        if (iosXrReg != null) {
            iosXrReg.close();
        }

        if (iosReg != null) {
            iosReg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
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
        // NO-OP
    }

    @Override
    public String toString() {
        return "IOS cli init (FRINX) translate unit";
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

