/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.handler.AreaConfigReader;
import io.frinx.cli.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.ospf.handler.AreaStateReader;
import io.frinx.cli.ospf.handler.GlobalConfigReader;
import io.frinx.cli.ospf.handler.GlobalConfigWriter;
import io.frinx.cli.ospf.handler.GlobalStateReader;
import io.frinx.cli.ospf.handler.OspfAreaReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.AreasBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public OspfUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GL_TI_MA_CONFIG, new NoopCliWriter<>()),
            IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigWriter(cli)),
            IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OSPFV2, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GLOBAL, new NoopCliWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigWriter(cli)),
            IIDs.NE_NE_PR_PROTOCOL);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG, new NoopCliWriter<>()));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OSPFV2, Ospfv2Builder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_GLOBAL, GlobalBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_OS_GL_STATE, new GlobalStateReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AREAS, AreasBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader()));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_STATE, new AreaStateReader()));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new AreaInterfaceReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.$YangModuleInfoImpl.getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS OSPF unit";
    }
}
