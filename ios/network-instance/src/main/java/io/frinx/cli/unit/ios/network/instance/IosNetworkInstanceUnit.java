/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.ios.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.IosDevices;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.network.instance.handler.ConnectionPointsReader;
import io.frinx.cli.unit.ios.network.instance.handler.ConnectionPointsWriter;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc.VrfInterfaceReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.ifc.VrfInterfaceWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.LocalAggregateConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.LocalAggregateConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.LocalAggregateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol.ProtocolStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.table.TableConnectionConfigWriter;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.table.TableConnectionReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.LocalAggregatesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.TableConnectionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosNetworkInstanceUnit implements TranslateUnit {

    public static final InstanceIdentifier<ConnectionPoints> CONN_PTS_ID = InstanceIdentifier.create(ConnectionPoints
            .class);
    private static final InstanceIdentifier<TableConnection> TABLE_CONN_ID = InstanceIdentifier.create(TableConnection
            .class);

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosDevices.IOS_ALL, this);
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NETWORKINSTANCE, new NoopCliWriter<>()));

        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli)),
                Sets.newHashSet(
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG,
                        io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG));

        writeRegistry.subtreeAddAfter(Sets.newHashSet(
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
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_INTERINSTANCEPOLICIES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_APPLYPOLICY, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_AP_CONFIG, new NoopCliWriter<>()));

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PROTOCOL, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli)),
                IIDs.NE_NE_IN_INTERFACE);

        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new LocalAggregateConfigWriter(cli)),
                Sets.newHashSet(IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG));

        // Interfaces for VRF
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_INTERFACES, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceWriter(cli)),
                IIDs.NE_NE_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_IN_CONFIG, new NoopCliWriter<>()));

        // Table connections for VRF
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_TABLECONNECTIONS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_TA_TABLECONNECTION, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_TA_TA_CONFIG, new TableConnectionConfigWriter(cli)),
                /*add after protocol writers*/
                Sets.newHashSet(IIDs.NE_NE_PR_PR_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG));

    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs, L2P2P
        readRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_STATE, new NetworkInstanceStateReader(cli)));

        // Interfaces for VRF
        readRegistry.addStructuralReader(IIDs.NE_NE_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceReader(cli)));

        // Protocols for VRF
        readRegistry.addStructuralReader(IIDs.NE_NE_PROTOCOLS, ProtocolsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader()));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader()));

        // Local aggregates
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_LOCALAGGREGATES, LocalAggregatesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new LocalAggregateReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new LocalAggregateConfigReader(cli)));

        // Table connections for VRF
        readRegistry.addStructuralReader(IIDs.NE_NE_TABLECONNECTIONS, TableConnectionsBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(RWUtils.cutIdFromStart(IIDs.NE_NE_TA_TA_CONFIG, TABLE_CONN_ID)),
                new GenericConfigListReader<>(IIDs.NE_NE_TA_TABLECONNECTION, new TableConnectionReader(cli)));

        // Connection points for L2P2p
        readRegistry.subtreeAdd(Sets.newHashSet(
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
