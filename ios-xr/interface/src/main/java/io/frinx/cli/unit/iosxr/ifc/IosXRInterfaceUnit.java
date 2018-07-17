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

package io.frinx.cli.unit.iosxr.ifc;

import static io.frinx.cli.iosxr.IosXrDevices.IOS_XR_ALL;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.ifc.handler.HoldTimeConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.HoldTimeConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceDampingConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceDampingConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceStatisticsConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceStatisticsConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd.BfdConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd.BfdConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.ethernet.EthernetConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.ethernet.EthernetConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceVlanConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6AdvertisementConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6AdvertisementConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6ConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.verify.RpfCheckIpv4Writer;
import io.frinx.cli.unit.iosxr.ifc.handler.verify.RpfCheckIpv6Writer;
import io.frinx.cli.unit.iosxr.ifc.handler.verify.RpfCheckReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.IfDampAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.IfDampAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.Damping;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.DampingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Interface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.Aggregation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.AggregationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.IfLagBfdAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.IfLagBfdAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.Bfd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.BfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoStatsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoStatsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.Statistics;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.StatisticsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableVia;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.RouterAdvertisement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.RouterAdvertisementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.HoldTimeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXRInterfaceUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosXRInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
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
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                        .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate
                        .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd
                        .rev171024.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                        .rev171024.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                        .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member
                        .rev171109.$YangModuleInfoImpl.getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();

        // TODO lot of handlers are exactly the same as the handlers from
        // ios-interface unit. Extract common logic and reuse it
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private static final InstanceIdentifier<Subinterface1> SUBIFC_IPV4_AUG_ID = IIDs.IN_IN_SU_SUBINTERFACE
            .augmentation(Subinterface1.class);
    private static final InstanceIdentifier<Ipv4> SUBIFC_IPV4_ID = SUBIFC_IPV4_AUG_ID.child(Ipv4.class);
    private static final InstanceIdentifier<Addresses> SUBIFC_IPV4_ADDRESSES_ID = SUBIFC_IPV4_ID.child(Addresses.class);
    private static final InstanceIdentifier<Address> SUBIFC_IPV4_ADDRESS_ID = SUBIFC_IPV4_ADDRESSES_ID.child(Address
            .class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
            .rev161222.ipv4.top.ipv4.addresses.address.Config> SUBIFC_IPV4_CFG_ID =
            SUBIFC_IPV4_ADDRESS_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                    .rev161222.ipv4.top.ipv4.addresses.address.Config.class);

    private static final InstanceIdentifier<Subinterface2> SUBIFC_IPV6_AUG_ID = IIDs.IN_IN_SU_SUBINTERFACE
            .augmentation(Subinterface2.class);
    private static final InstanceIdentifier<Ipv6> SUBIFC_IPV6_ID = SUBIFC_IPV6_AUG_ID.child(Ipv6.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
            .rev161222.ipv6.top.ipv6.Addresses> SUBIFC_IPV6_ADDRESSES_ID =
            SUBIFC_IPV6_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                    .rev161222.ipv6.top.ipv6.Addresses.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
            .rev161222.ipv6.top.ipv6.addresses.Address> SUBIFC_IPV6_ADDRESS_ID =
            SUBIFC_IPV6_ADDRESSES_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                    .rev161222.ipv6.top.ipv6.addresses.Address.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
            .rev161222.ipv6.top.ipv6.addresses.address.Config> SUBIFC_IPV6_CFG_ID =
            SUBIFC_IPV6_ADDRESS_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                    .rev161222.ipv6.top.ipv6.addresses.address.Config.class);
    private static final InstanceIdentifier<RouterAdvertisement> SUBIFC_IPV6_ROUTER_ADVERTIS
            = SUBIFC_IPV6_ID.child(RouterAdvertisement.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
            .rev161222.ipv6.top.ipv6.router.advertisement.Config> SUBIFC_IPV6_ROUTER_ADVERTIS_CONFIG =
            SUBIFC_IPV6_ROUTER_ADVERTIS.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .ip.rev161222.ipv6.top.ipv6.router.advertisement.Config.class);

    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
            .rev170714.Subinterface1> SUBIFC_VLAN_AUG_ID =
            IIDs.IN_IN_SU_SUBINTERFACE.augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
                    .rev170714.Subinterface1.class);
    private static final InstanceIdentifier<Vlan> SUBIFC_VLAN_ID = SUBIFC_VLAN_AUG_ID.child(Vlan.class);
    private static final InstanceIdentifier<Config> SUBIFC_VLAN_CFG_ID = SUBIFC_VLAN_ID.child(Config.class);

    // if-aggregate IIDs
    private static final InstanceIdentifier<Interface1> IFC_AGGREGATION_AUG_ID =
            IIDs.IN_INTERFACE.augmentation(Interface1.class);
    private static final InstanceIdentifier<Aggregation> IFC_AGGREGATION_ID =
            IFC_AGGREGATION_AUG_ID.child(Aggregation.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .aggregate.rev161222.aggregation.logical.top.aggregation.Config> IFC_AGGREGATION_CFG_ID =
            IFC_AGGREGATION_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate
                    .rev161222.aggregation.logical.top.aggregation.Config.class);

    // bfd IIDs
    private static final InstanceIdentifier<IfLagBfdAug> IFC_LAG_BFD_AUG_ID =
            IFC_AGGREGATION_ID.augmentation(IfLagBfdAug.class);
    private static final InstanceIdentifier<Bfd> IFC_LAG_BFD_ID = IFC_LAG_BFD_AUG_ID.child(Bfd.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .bfd.rev171024.bfd.top.bfd.Config> IFC_LAG_BFD_CFG_ID =
            IFC_LAG_BFD_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd
                    .rev171024.bfd.top.bfd.Config.class);
    // damping IIDs
    private static final InstanceIdentifier<IfDampAug> IFC_DAMPING_AUG_ID = IIDs.IN_INTERFACE.augmentation(IfDampAug
            .class);
    private static final InstanceIdentifier<Damping> IFC_DAMPING_ID = IFC_DAMPING_AUG_ID.child(Damping.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping
            .rev171024.damping.top.damping.Config> IFC_DAMPING_CFG_ID =
            IFC_DAMPING_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping
                    .rev171024.damping.top.damping.Config.class);

    // cisco-if-extension IIDs
    private static final InstanceIdentifier<IfCiscoStatsAug> IFC_CISCO_EX_AUG_ID =
            IIDs.IN_INTERFACE.augmentation(IfCiscoStatsAug.class);
    private static final InstanceIdentifier<Statistics> IFC_CISCO_EX_STAT_ID = IFC_CISCO_EX_AUG_ID.child(Statistics
            .class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .cisco.rev171024.statistics.top.statistics.Config> IFC_CISCO_EX_STAT_CONFIG_ID =
            IFC_CISCO_EX_STAT_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                    .rev171024.statistics.top.statistics.Config.class);

    // if-ethernet IIDs
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .ethernet.rev161222.Interface1> IFC_ETH_AUD_ID =
            IIDs.IN_INTERFACE.augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .ethernet.rev161222.Interface1.class);
    private static final InstanceIdentifier<Ethernet> IFC_ETHERNET_ID = IFC_ETH_AUD_ID.child(Ethernet.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .ethernet.rev161222.ethernet.top.ethernet.Config> IFC_ETHERNET_CONFIG_ID =
            IFC_ETHERNET_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                    .rev161222.ethernet.top.ethernet.Config.class);

    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .ethernet.rev161222.ethernet.top.ethernet.Config> IFC_ETH_CONFIG_ROOT_ID =
            InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                    .rev161222.ethernet.top.ethernet.Config.class);

    // RPF check IIDs
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .cisco.rev171024.Interface1> RPF_AUG_IID =
            IIDs.IN_INTERFACE.augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .cisco.rev171024.Interface1.class);
    private static final InstanceIdentifier<VerifyUnicastSourceReachableVia> RPF_IID =
            IIDs.IN_INTERFACE.augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .cisco.rev171024.Interface1.class)
                    .child(VerifyUnicastSourceReachableVia.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .cisco.rev171024.ipv4.verify.Ipv4> RPF_IPV4_IID =
            RPF_IID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                    .rev171024.ipv4.verify.Ipv4.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .cisco.rev171024.ipv6.verify.Ipv6> RPF_IPV6_IID =
            RPF_IID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                    .rev171024.ipv6.verify.Ipv6.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .cisco.rev171024.ipv4.verify.Ipv4> RPF_IPV4_SUBTREE_IID =
            InstanceIdentifier.create(VerifyUnicastSourceReachableVia.class)
                    .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                            .rev171024.ipv4.verify.Ipv4.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .cisco.rev171024.ipv6.verify.Ipv6> RPF_IPV6_SUBTREE_IID =
            InstanceIdentifier.create(VerifyUnicastSourceReachableVia.class)
                    .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                            .rev171024.ipv6.verify.Ipv6.class);

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericListWriter<>(IIDs.IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli)));

        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SUBINTERFACE, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);
        writeRegistry.addAfter(new GenericWriter<>(SUBIFC_VLAN_CFG_ID, new SubinterfaceVlanConfigWriter(cli)),
                IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.add(new GenericWriter<>(SUBIFC_IPV4_ADDRESS_ID, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(SUBIFC_IPV4_CFG_ID, new Ipv4ConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // TODO implement IPv6 writers, NOOP writers are just workaround so we
        // provide at least some writers for IPv6 data
        writeRegistry.add(new GenericWriter<>(SUBIFC_IPV6_ADDRESS_ID, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(SUBIFC_IPV6_CFG_ID, new Ipv6ConfigWriter(cli)));

        writeRegistry.add(new GenericWriter<>(SUBIFC_IPV6_ROUTER_ADVERTIS_CONFIG,
                new Ipv6AdvertisementConfigWriter(cli)));

        // hold-time
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_HO_CONFIG, new HoldTimeConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // if-aggregation
        writeRegistry.add(new GenericWriter<>(IFC_AGGREGATION_ID, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IFC_AGGREGATION_CFG_ID, new AggregateConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // bfd
        writeRegistry.add(new GenericWriter<>(IFC_LAG_BFD_ID, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IFC_LAG_BFD_CFG_ID, new BfdConfigWriter(cli)), IIDs.IN_IN_CONFIG);

        // damping
        writeRegistry.addAfter(new GenericWriter<>(IFC_DAMPING_CFG_ID, new InterfaceDampingConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // cisco-if extensions
        writeRegistry.addAfter(new GenericWriter<>(IFC_CISCO_EX_STAT_CONFIG_ID,
                        new InterfaceStatisticsConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // if-ethernet
        writeRegistry.add(new GenericWriter<>(IFC_ETHERNET_ID, new NoopCliWriter<>()));
        writeRegistry.subtreeAddAfter(Sets.newHashSet(
                IFC_ETH_CONFIG_ROOT_ID.augmentation(Config1.class),
                IFC_ETH_CONFIG_ROOT_ID.augmentation(LacpEthConfigAug.class)),
                new GenericWriter<>(IFC_ETHERNET_CONFIG_ID, new EthernetConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // RPF check
        writeRegistry.add(new GenericWriter<>(RPF_IID, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(RPF_IPV4_IID, new RpfCheckIpv4Writer(cli)));
        writeRegistry.add(new GenericWriter<>(RPF_IPV6_IID, new RpfCheckIpv6Writer(cli)));

    }

    private void provideReaders(ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES, SubinterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(SUBIFC_IPV4_AUG_ID, Subinterface1Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV4_ID, Ipv4Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV4_ADDRESSES_ID, AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(SUBIFC_IPV4_ADDRESS_ID, new Ipv4AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(SUBIFC_IPV4_CFG_ID, new Ipv4ConfigReader(cli)));

        readRegistry.addStructuralReader(SUBIFC_IPV6_AUG_ID, Subinterface2Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV6_ID, Ipv6Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV6_ADDRESSES_ID,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top
                        .ipv6.AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(SUBIFC_IPV6_ADDRESS_ID, new Ipv6AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(SUBIFC_IPV6_CFG_ID, new Ipv6ConfigReader(cli)));

        readRegistry.addStructuralReader(SUBIFC_IPV6_ROUTER_ADVERTIS, RouterAdvertisementBuilder.class);
        readRegistry.add(new GenericConfigReader<>(SUBIFC_IPV6_ROUTER_ADVERTIS_CONFIG,
                new Ipv6AdvertisementConfigReader(cli)));

        readRegistry.addStructuralReader(SUBIFC_VLAN_AUG_ID, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .vlan.rev170714.Subinterface1Builder.class);
        readRegistry.addStructuralReader(SUBIFC_VLAN_ID, VlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(SUBIFC_VLAN_CFG_ID, new SubinterfaceVlanConfigReader(cli)));

        // hold-time
        readRegistry.addStructuralReader(IIDs.IN_IN_HOLDTIME, HoldTimeBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_HO_CONFIG, new HoldTimeConfigReader(cli)));

        // if-aggregation
        readRegistry.addStructuralReader(IFC_AGGREGATION_AUG_ID, Interface1Builder.class);
        readRegistry.addStructuralReader(IFC_AGGREGATION_ID, AggregationBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_AGGREGATION_CFG_ID, new AggregateConfigReader(cli)));

        // bfd
        readRegistry.addStructuralReader(IFC_LAG_BFD_AUG_ID, IfLagBfdAugBuilder.class);
        readRegistry.addStructuralReader(IFC_LAG_BFD_ID, BfdBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_LAG_BFD_CFG_ID, new BfdConfigReader(cli)));

        // damping
        readRegistry.addStructuralReader(IFC_DAMPING_AUG_ID, IfDampAugBuilder.class);
        readRegistry.addStructuralReader(IFC_DAMPING_ID, DampingBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_DAMPING_CFG_ID, new InterfaceDampingConfigReader(cli)));

        // cisco if-extensions
        readRegistry.addStructuralReader(IFC_CISCO_EX_AUG_ID, IfCiscoStatsAugBuilder.class);
        readRegistry.addStructuralReader(IFC_CISCO_EX_STAT_ID, StatisticsBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IFC_CISCO_EX_STAT_CONFIG_ID,
                new InterfaceStatisticsConfigReader(cli)));

        // if-ethernet
        readRegistry.addStructuralReader(IFC_ETH_AUD_ID, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .interfaces.ethernet.rev161222.Interface1Builder.class);
        readRegistry.addStructuralReader(IFC_ETHERNET_ID, EthernetBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                IFC_ETH_CONFIG_ROOT_ID.augmentation(Config1.class),
                IFC_ETH_CONFIG_ROOT_ID.augmentation(LacpEthConfigAug.class)),
                new GenericConfigReader<>(IFC_ETHERNET_CONFIG_ID, new EthernetConfigReader(cli)));

        // RPF check
        readRegistry.addStructuralReader(RPF_AUG_IID, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .interfaces.cisco.rev171024.Interface1Builder.class);
        readRegistry.subtreeAdd(
                Sets.newHashSet(RPF_IPV4_SUBTREE_IID, RPF_IPV6_SUBTREE_IID),
                new GenericConfigReader<>(RPF_IID, new RpfCheckReader(cli))
        );
    }

    @Override
    public String toString() {
        return "IOS XR Interface (Openconfig) translate unit";
    }

}
