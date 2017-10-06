/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceInterfaceReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.ios.network.instance.handler.ProtocolConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.ProtocolReader;
import io.frinx.cli.unit.ios.network.instance.handler.ProtocolStateReader;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosNetworkInstanceUnit implements TranslateUnit {
    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();
    private static final InstanceIdentifier<NetworkInstances> NETWORK_INSTANCES_ID = InstanceIdentifier
            .create(NetworkInstances.class);
    private static final InstanceIdentifier<NetworkInstance> NETWORK_INSTANCE_ID = NETWORK_INSTANCES_ID
            .child(NetworkInstance.class);
    private static final InstanceIdentifier<Protocols> PROTOCOLS_ID = NETWORK_INSTANCE_ID
            .child(Protocols.class);
    private static final InstanceIdentifier<Protocol> PROTOCOL_ID = PROTOCOLS_ID
            .child(Protocol.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config> PROTOCOL_CFG_ID = PROTOCOL_ID
            .child(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.State> PROTOCOL_STATE_ID = PROTOCOL_ID
            .child(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.State.class);

    private static final InstanceIdentifier<Config> NETWORK_INSTANCE_CONFIG_ID = NETWORK_INSTANCE_ID
            .child(Config.class);
    private static final InstanceIdentifier<Interfaces> NETWORK_INSTANCE_INTERFFACES_ID = NETWORK_INSTANCE_ID
            .child(Interfaces.class);
    private static final InstanceIdentifier<Interface> NETWORK_INSTANCE_INTERFACE = NETWORK_INSTANCE_INTERFFACES_ID
            .child(Interface.class);

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
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
        // VRFs
        rRegistry.addStructuralReader(NETWORK_INSTANCES_ID, NetworkInstancesBuilder.class);
        rRegistry.add(new GenericListReader<>(NETWORK_INSTANCE_ID, new NetworkInstanceReader(cli)));
        rRegistry.add(new GenericReader<>(NETWORK_INSTANCE_CONFIG_ID, new NetworkInstanceConfigReader(cli)));
        rRegistry.addStructuralReader(NETWORK_INSTANCE_INTERFFACES_ID, InterfacesBuilder.class);
        rRegistry.add(new GenericListReader<>(NETWORK_INSTANCE_INTERFACE, new NetworkInstanceInterfaceReader(cli)));

        rRegistry.addStructuralReader(PROTOCOLS_ID, ProtocolsBuilder.class);
        rRegistry.add(new GenericListReader<>(PROTOCOL_ID, new ProtocolReader(cli)));
        rRegistry.add(new GenericReader<>(PROTOCOL_CFG_ID, new ProtocolConfigReader()));
        rRegistry.add(new GenericReader<>(PROTOCOL_STATE_ID, new ProtocolStateReader()));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS Network Instance (Openconfig) translate unit";
    }
}
