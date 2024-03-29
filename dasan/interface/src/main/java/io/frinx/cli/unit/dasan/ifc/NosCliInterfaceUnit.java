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

package io.frinx.cli.unit.dasan.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.EthernetConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.EthernetConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember.PhysicalPortVlanMemberConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember.PhysicalPortVlanMemberConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember.TrunkPortVlanMemberConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember.TrunkPortVlanMemberConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan.L3ipvlanConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan.L3ipvlanConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.lacp.BundleEtherLacpConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.lacp.BundleEtherLacpConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4.Ipv4AddressConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4.Ipv4AddressConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Interface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.AggregationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.IfL3ipvlanAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.L3ipvlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.top.Lacp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.top.LacpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Aggregation1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlanBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class NosCliInterfaceUnit extends AbstractUnit {

    private static final Device DASAN = new DeviceIdBuilder()
            .setDeviceType("nos")
            .setDeviceVersion("*")
            .build();

    public NosCliInterfaceUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate
                .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
                .rev170714.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan
                .rev180802.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext
                .rev180926.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member
                .rev171109.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp
                .rev170505.$YangModuleInfoImpl.getInstance()
                );
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericListWriter<>(IIDs.IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli)));

        writeRegistry.addAfter(new GenericWriter<>(IIDs.INTER_INTER_AUG_INTERFACE1, new NoopCliWriter<>()),
                IIDs.IN_IN_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_AGGREGATION, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_AG_CONFIG,
                new BundleEtherLacpConfigWriter(cli)));

        // if-ethernet
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET, new NoopCliWriter<>()));
        InstanceIdentifier<Config> iidEhernetConfig = InstanceIdentifier.create(Config.class);

        // LacpEthConfigAug
        writeRegistry.subtreeAddAfter(
                Sets.newHashSet(
                        RWUtils.cutIdFromStart(IIDs.IN_IN_ET_CO_AUG_CONFIG1, iidEhernetConfig),
                        RWUtils.cutIdFromStart(IIDs.INT_INT_ETH_CON_AUG_CONFIG1, iidEhernetConfig),
                        RWUtils.cutIdFromStart(
                            io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG,
                            iidEhernetConfig)
                        ),
                new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // if-l3ipvlan
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_AUG_IFL3IPVLANAUG, new NoopCliWriter<>()),
                IIDs.IN_IN_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_IFL3IPVLANAUG_L3IPVLAN, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_IFL3IPVLANAUG_L3_CONFIG, new L3ipvlanConfigWriter(cli)));

        // vlan
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SWITCHEDVLAN,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                new PhysicalPortVlanMemberConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_AG_AUG_AGGREGATION1_SWITCHEDVLAN,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_AG_AUG_AGGREGATION1_SW_CONFIG,
                new TrunkPortVlanMemberConfigWriter(cli)));

        // subinterface
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SUBINTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SU_CONFIG, new NoopCliWriter<>()));

        // ipv4address
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                        new Ipv4AddressConfigWriter(cli)), IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.IN_IN_STATE, new InterfaceStateReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.INT_INT_AUG_INTERFACE1, Interface1Builder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_INTERFACE1_AGGREGATION, AggregationBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AUG_INTERFACE1_AG_CONFIG,
                new BundleEtherLacpConfigReader(cli)));

        // if-ethernet
        readRegistry.addStructuralReader(IIDs.INTERF_INTERF_AUG_INTERFACE1,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222
            .Interface1Builder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET, EthernetBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigReader(cli)));

        // if-l3ipvlan
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_IFL3IPVLANAUG, IfL3ipvlanAugBuilder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_IFL3IPVLANAUG_L3IPVLAN, L3ipvlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AUG_IFL3IPVLANAUG_L3_CONFIG,
            new L3ipvlanConfigReader(cli)));

        // vlan
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1,
            Ethernet1Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SWITCHEDVLAN,
            SwitchedVlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
            new PhysicalPortVlanMemberConfigReader(cli)));
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_AG_AUG_AGGREGATION1,
                Aggregation1Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_AG_AUG_AGGREGATION1_SWITCHEDVLAN,
                SwitchedVlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_AG_AUG_AGGREGATION1_SW_CONFIG,
            new TrunkPortVlanMemberConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES, SubinterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader()));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader()));

        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1,
            Subinterface1Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IPV4,
            Ipv4Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_ADDRESSES,
            AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4AddressConfigReader(cli)));

        InstanceIdentifier<Lacp> lacpDummyIID = InstanceIdentifier.create(Lacp.class);
        readRegistry.addStructuralReader(lacpDummyIID, LacpBuilder.class);
        InstanceIdentifier<RoutingPolicy> routingPolicyIID = InstanceIdentifier.create(RoutingPolicy.class);
        readRegistry.addStructuralReader(routingPolicyIID, RoutingPolicyBuilder.class);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(DASAN);
    }

    @Override
    protected String getUnitName() {
        return "Dasan Interface (Openconfig) translate unit";
    }
}