/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.logging;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.logging.handler.LoggingInterfaceConfigWriter;
import io.frinx.cli.iosxr.logging.handler.LoggingInterfacesReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.openconfig.openconfig.logging.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.top.LoggingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class LoggingUnit implements TranslateUnit {

    private static final InstanceIdentifier<Interfaces> IFCS_ID = InstanceIdentifier.create(Interfaces.class);
    private static final InstanceIdentifier<Config> IFC_CFG_ID = InstanceIdentifier.create(Config.class);

    private static final Device IOS_XR_ALL = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public LoggingUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_XR_ALL, this);
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        wRegistry.add(new GenericListWriter<>(IIDs.LO_IN_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.subtreeAddAfter(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.LO_IN_IN_CO_ENABLEDLOGGINGFOREVENT, IFC_CFG_ID)),
                new GenericWriter<>(IIDs.LO_IN_IN_CONFIG, new LoggingInterfaceConfigWriter(cli)),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.LOGGING, LoggingBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.LO_IN_INTERFACE, IFCS_ID),
                RWUtils.cutIdFromStart(IIDs.LO_IN_IN_CONFIG, IFCS_ID),
                RWUtils.cutIdFromStart(IIDs.LO_IN_IN_CO_ENABLEDLOGGINGFOREVENT, IFCS_ID)),
                new GenericConfigReader<>(IIDs.LO_INTERFACES, new LoggingInterfacesReader(cli)));
    }

}
