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
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.EthernetConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.EthernetConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember.PhysicalPortVlanMemberConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember.PhysicalPortVlanMemberConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan.L3ipvlanConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan.L3ipvlanConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.lacp.BundleEtherLacpConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.lacp.BundleEtherLacpConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4.Ipv4AddressConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4.Ipv4AddressConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class NosCliInterfaceUnit implements TranslateUnit {

    private static final Device DASAN = new DeviceIdBuilder()
            .setDeviceType("nos")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public NosCliInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(DASAN, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
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
                .rev180802.$YangModuleInfoImpl.getInstance()
                );
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    // subif-ipv4 IIDs
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1
        > SUBIFC_IPV4_AUG_ID =
            IIDs.IN_IN_SU_SUBINTERFACE
                .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                    .rev161222.Subinterface1.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4
        > SUBIFC_IPV4_ID =
            SUBIFC_IPV4_AUG_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222
                    .ipv4.top.Ipv4.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses
        > SUBIFC_IPV4_ADDRESSES_ID =
            SUBIFC_IPV4_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4
                    .Addresses.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses
        .Address
        > SUBIFC_IPV4_ADDRESS_ID =
            SUBIFC_IPV4_ADDRESSES_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4
                    .addresses.Address.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses
        .address.Config
        > SUBIFC_IPV4_CFG_ID =
            SUBIFC_IPV4_ADDRESS_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4
                    .addresses.address.Config.class);

    // if-lag IIDs
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Interface1
        > IFC_AGGREGATE_AUG_ID =
            IIDs.IN_INTERFACE
                .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222
                    .Interface1.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical
        .top.Aggregation
        > IFC_AGGREGATE_ID =
            IFC_AGGREGATE_AUG_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222
                    .aggregation.logical.top.Aggregation.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical
        .top.aggregation.Config
        > IFC_AGGREGATE_CFG_ID =
            IFC_AGGREGATE_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222
                    .aggregation.logical.top.aggregation.Config.class);

    // if-ethernet IIDs
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1
        > IFC_ETH_AUG_ID =
            IIDs.IN_INTERFACE
                .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222
                    .Interface1.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet
        > IFC_ETHERNET_ID =
            IFC_ETH_AUG_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet
                    .top.Ethernet.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet
        .Config
        > IFC_ETHERNET_CONFIG_ID =
            IFC_ETHERNET_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet
                    .top.ethernet.Config.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet
        .Config
        > IFC_ETH_CONFIG_ROOT_ID =
            InstanceIdentifier
                .create(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet
                    .top.ethernet.Config.class);

    // if-l3ipvlan IIDs
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.IfL3ipvlanAug
        > IFC_L3IPVLAN_AUG_ID =
            IIDs.IN_INTERFACE
                .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802
                    .IfL3ipvlanAug.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface
        .top.L3ipvlan
        > IFC_L3IPVLAN_ID =
            IFC_L3IPVLAN_AUG_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan
                    ._interface.top.L3ipvlan.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface
        .top.l3ipvlan.Config
        > IFC_L3IPVLAN_CONFIG_ID =
            IFC_L3IPVLAN_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan
                    ._interface.top.l3ipvlan.Config.class);

    // vlan IIDs
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1
        > IFC_VLAN_ETH_ID =
            IFC_ETHERNET_ID
                .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlan
        > IFC_VLAN_ID =
            IFC_VLAN_ETH_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top
                    .SwitchedVlan.class);
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan
        .Config
        > IFC_VLAN_CONFIG_ID =
            IFC_VLAN_ID
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top
                    .switched.vlan.Config.class);

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericListWriter<>(IIDs.IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli)));

        writeRegistry.addAfter(new GenericWriter<>(IFC_AGGREGATE_AUG_ID, new NoopCliWriter<>()), IIDs.IN_IN_CONFIG);
        writeRegistry.add(new GenericWriter<>(IFC_AGGREGATE_ID, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IFC_AGGREGATE_CFG_ID, new BundleEtherLacpConfigWriter(cli)));

        // if-ethernet
        writeRegistry.add(new GenericWriter<>(IFC_ETHERNET_ID, new NoopCliWriter<>()));
        writeRegistry.subtreeAddAfter(Sets.newHashSet(
                IFC_ETH_CONFIG_ROOT_ID.augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222
                    .Config1.class)
                ),
                new GenericWriter<>(IFC_ETHERNET_CONFIG_ID, new EthernetConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // if-l3ipvlan
        writeRegistry.addAfter(new GenericWriter<>(IFC_L3IPVLAN_AUG_ID, new NoopCliWriter<>()), IIDs.IN_IN_CONFIG);
        writeRegistry.add(new GenericWriter<>(IFC_L3IPVLAN_ID, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IFC_L3IPVLAN_CONFIG_ID, new L3ipvlanConfigWriter(cli)));

        // vlan
        writeRegistry.add(new GenericWriter<>(IFC_VLAN_ID, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IFC_VLAN_CONFIG_ID, new PhysicalPortVlanMemberConfigWriter(cli)));

        // subinterface
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SUBINTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SU_CONFIG, new NoopCliWriter<>()));

        // ipv4address
        writeRegistry.add(new GenericWriter<>(SUBIFC_IPV4_ADDRESS_ID, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(SUBIFC_IPV4_CFG_ID, new Ipv4AddressConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.IN_IN_STATE, new InterfaceStateReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(IFC_AGGREGATE_AUG_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222
            .Interface1Builder.class);
        readRegistry.addStructuralReader(IFC_AGGREGATE_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation
            .logical.top.AggregationBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_AGGREGATE_CFG_ID, new BundleEtherLacpConfigReader(cli)));

        // if-ethernet
        readRegistry.addStructuralReader(IFC_ETH_AUG_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222
            .Interface1Builder.class);
        readRegistry.addStructuralReader(IFC_ETHERNET_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top
            .EthernetBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                IFC_ETH_CONFIG_ROOT_ID.augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222
                    .Config1.class)
                ),
                new GenericConfigReader<>(IFC_ETHERNET_CONFIG_ID,
                    new EthernetConfigReader(cli)));

        // if-l3ipvlan
        readRegistry.addStructuralReader(IFC_L3IPVLAN_AUG_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802
            .IfL3ipvlanAugBuilder.class);
        readRegistry.addStructuralReader(IFC_L3IPVLAN_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802
            .l3ipvlan._interface.top.L3ipvlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_L3IPVLAN_CONFIG_ID,
            new L3ipvlanConfigReader(cli)));

        // vlan
        readRegistry.addStructuralReader(IFC_VLAN_ETH_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714
            .Ethernet1Builder.class);
        readRegistry.addStructuralReader(IFC_VLAN_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top
            .SwitchedVlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_VLAN_CONFIG_ID,
            new PhysicalPortVlanMemberConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES,
            SubinterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE,
            new SubinterfaceReader()));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG,
            new SubinterfaceConfigReader()));

        readRegistry.addStructuralReader(SUBIFC_IPV4_AUG_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222
            .Subinterface1Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV4_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top
            .Ipv4Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV4_ADDRESSES_ID,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4
            .AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(SUBIFC_IPV4_ADDRESS_ID, new Ipv4AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(SUBIFC_IPV4_CFG_ID, new Ipv4AddressConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "Dasan Interface (Openconfig) translate unit";
    }
}
