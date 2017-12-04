/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.lldp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperListReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.lldp.handler.InterfaceConfigReader;
import io.frinx.cli.unit.ios.lldp.handler.InterfaceReader;
import io.frinx.cli.unit.ios.lldp.handler.InterfaceStateReader;
import io.frinx.cli.unit.ios.lldp.handler.NeighborReader;
import io.frinx.cli.unit.ios.lldp.handler.NeighborStateReader;
import io.frinx.openconfig.openconfig.lldp.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.LldpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LldpUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public LldpUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
    }

    private void provideReaders(ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // TODO CDP and LLDP are almost identical, reuse code, DRY
        rRegistry.addStructuralReader(IIDs.LLDP, LldpBuilder.class);
        rRegistry.addStructuralReader(IIDs.LL_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.LL_IN_INTERFACE, new InterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.LL_IN_IN_CONFIG, new InterfaceConfigReader()));
        rRegistry.add(new GenericOperReader<>(IIDs.LL_IN_IN_STATE, new InterfaceStateReader()));
        rRegistry.addStructuralReader(IIDs.LL_IN_IN_NEIGHBORS, NeighborsBuilder.class);
        rRegistry.add(new GenericOperListReader<>(IIDs.LL_IN_IN_NE_NEIGHBOR, new NeighborReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.LL_IN_IN_NE_NE_STATE, new NeighborStateReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS LLDP (Openconfig) translate unit";
    }
}
