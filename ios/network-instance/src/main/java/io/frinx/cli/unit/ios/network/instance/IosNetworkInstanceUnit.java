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
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.network.instance.handler.ConnectionPointsReader;
import io.frinx.cli.unit.ios.network.instance.handler.ConnectionPointsWriter;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.VrfTableConnectionConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.VrfTableConnectionReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc.VrfInterfaceReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc.VrfInterfaceWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolStateReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.TableConnectionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosNetworkInstanceUnit implements TranslateUnit {
    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();
    public static final InstanceIdentifier<ConnectionPoints> CONN_PTS_ID = InstanceIdentifier.create(ConnectionPoints.class);
    private static final InstanceIdentifier<TableConnection> TABLE_CONN_ID = InstanceIdentifier.create(TableConnection.class);

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
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        // No handling required on the network instance level
        wRegistry.add(new GenericWriter<>(IIDs.NE_NETWORKINSTANCE, new NoopCliWriter<>()));

        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli)),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        wRegistry.subtreeAddAfter(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CONNECTIONPOINT, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_ENDPOINTS, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_ENDPOINT, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_LOCAL, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_LO_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_REMOTE, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_RE_CONFIG, CONN_PTS_ID)
                ), new GenericWriter<>(IIDs.NE_NE_CONNECTIONPOINTS, new ConnectionPointsWriter(cli)),
                /*handle after network instance configuration*/ IIDs.NE_NE_CONFIG);

        //todo create proper writers once we support routing policies
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_INTERINSTANCEPOLICIES, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_APPLYPOLICY, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_AP_CONFIG, new NoopCliWriter<>()));

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PROTOCOL, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli)));

        // Interfaces for VRF
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_INTERFACES, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceWriter(cli)));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_IN_CONFIG, new NoopCliWriter<>()));

        // Table connections for VRF
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_TABLECONNECTIONS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_TA_TABLECONNECTION, new NoopCliListWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_TA_TA_CONFIG, new VrfTableConnectionConfigWriter(cli)),
                /*add after protocol writers*/ IIDs.NE_NE_PR_PR_CONFIG);

    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // VRFs, L2P2P
        rRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_STATE, new NetworkInstanceStateReader(cli)));

        // Interfaces for VRF
        rRegistry.addStructuralReader(IIDs.NE_NE_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceReader(cli)));

        // Protocols for VRF
        rRegistry.addStructuralReader(IIDs.NE_NE_PROTOCOLS, ProtocolsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader()));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader()));

        // Table connections for VRF
        rRegistry.addStructuralReader(IIDs.NE_NE_TABLECONNECTIONS, TableConnectionsBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(RWUtils.cutIdFromStart(IIDs.NE_NE_TA_TA_CONFIG, TABLE_CONN_ID)),
                new GenericConfigListReader<>(IIDs.NE_NE_TA_TABLECONNECTION, new VrfTableConnectionReader(cli)));

        // Connection points for L2P2p
        rRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CONNECTIONPOINT, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_STATE, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_ENDPOINTS, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_ENDPOINT, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_STATE, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_LOCAL, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_LO_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_LO_STATE, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_REMOTE, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_RE_CONFIG, CONN_PTS_ID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_CO_CO_EN_EN_RE_STATE, CONN_PTS_ID)),
                new GenericConfigReader<>(IIDs.NE_NE_CONNECTIONPOINTS, new ConnectionPointsReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS Network Instance (Openconfig) translate unit";
    }
}
