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
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.handler.AreaConfigReader;
import io.frinx.cli.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.ospf.handler.AreaStateReader;
import io.frinx.cli.ospf.handler.GlobalConfigReader;
import io.frinx.cli.ospf.handler.GlobalStateReader;
import io.frinx.cli.ospf.handler.OspfAreaReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.Interfaces;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.Global;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2Builder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.Areas;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.AreasBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    // FIXME duplicate code with Network instance unit
    private static final InstanceIdentifier<NetworkInstances> NETWORK_INSTANCES_ID = InstanceIdentifier
            .create(NetworkInstances.class);
    private static final InstanceIdentifier<NetworkInstance> NETWORK_INSTANCE_ID = NETWORK_INSTANCES_ID
            .child(NetworkInstance.class);
    private static final InstanceIdentifier<Protocols> PROTOCOLS_ID = NETWORK_INSTANCE_ID
            .child(Protocols.class);
    private static final InstanceIdentifier<Protocol> PROTOCOL_ID = PROTOCOLS_ID
            .child(Protocol.class);

    private static final InstanceIdentifier<Ospfv2> NETWORK_INSTANCE_PROTOCOL_OSPF = PROTOCOL_ID.child(Ospfv2.class);
    private static final InstanceIdentifier<Global> NETWORK_INSTANCE_PROTOCOL_OSPF_GLOBAL = NETWORK_INSTANCE_PROTOCOL_OSPF
            .child(Global.class);
    private static final InstanceIdentifier<Config> PROTOCOL_GLOBAL_CONFIG = NETWORK_INSTANCE_PROTOCOL_OSPF_GLOBAL.child(Config.class);
    private static final InstanceIdentifier<State> PROTOCOL_GLOBAL_STATE = NETWORK_INSTANCE_PROTOCOL_OSPF_GLOBAL.child(State.class);
    private static final InstanceIdentifier<Areas> NETWORK_INSTANCE_PROTOCOL_OSPF_AREAS = NETWORK_INSTANCE_PROTOCOL_OSPF
            .child(Areas.class);
    private static final InstanceIdentifier<Area> NETWORK_INSTANCE_PROTOCOL_OSPF_AREA = NETWORK_INSTANCE_PROTOCOL_OSPF_AREAS
            .child(Area.class);

    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.Config> OSPF_AREA_CONFIG = NETWORK_INSTANCE_PROTOCOL_OSPF_AREA
            .child(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.Config.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.State> OSPF_AREA_STATE = NETWORK_INSTANCE_PROTOCOL_OSPF_AREA
            .child(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.State.class);

    private static final InstanceIdentifier<Interfaces> OSPF_AREA_INTERFACES = NETWORK_INSTANCE_PROTOCOL_OSPF_AREA
            .child(Interfaces.class);
    private static final InstanceIdentifier<Interface> OSPF_AREA_INTERFACS = OSPF_AREA_INTERFACES.child(Interface.class);

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
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(NETWORK_INSTANCE_PROTOCOL_OSPF, Ospfv2Builder.class);
        rRegistry.addStructuralReader(NETWORK_INSTANCE_PROTOCOL_OSPF_GLOBAL, GlobalBuilder.class);
        rRegistry.add(new GenericReader<>(PROTOCOL_GLOBAL_CONFIG, new GlobalConfigReader(cli)));
        rRegistry.add(new GenericReader<>(PROTOCOL_GLOBAL_STATE, new GlobalStateReader(cli)));
        rRegistry.addStructuralReader(NETWORK_INSTANCE_PROTOCOL_OSPF_AREAS, AreasBuilder.class);
        rRegistry.add(new GenericListReader<>(NETWORK_INSTANCE_PROTOCOL_OSPF_AREA, new OspfAreaReader(cli)));
        rRegistry.add(new GenericReader<>(OSPF_AREA_CONFIG, new AreaConfigReader()));
        rRegistry.add(new GenericReader<>(OSPF_AREA_STATE, new AreaStateReader()));
        rRegistry.addStructuralReader(OSPF_AREA_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericListReader<>(OSPF_AREA_INTERFACS, new AreaInterfaceReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.$YangModuleInfoImpl.getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS OSPF unit";
    }
}
