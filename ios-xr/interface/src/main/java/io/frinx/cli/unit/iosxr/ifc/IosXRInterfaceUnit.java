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

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.IosXrDevices;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.IfDampAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.DampingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Interface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.AggregationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.IfLagBfdAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.BfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoStatsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.StatisticsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableVia;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.RouterAdvertisementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.HoldTimeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.VlanBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXRInterfaceUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosXRInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
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

    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .ethernet.rev161222.ethernet.top.ethernet.Config> IFC_ETH_CONFIG_ROOT_ID =
            InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                    .rev161222.ethernet.top.ethernet.Config.class);

    // RPF check IIDs
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
        writeRegistry.addAfter(new GenericWriter<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                        new SubinterfaceVlanConfigWriter(cli)), IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                        new Ipv4ConfigWriter(cli)), IIDs.IN_IN_CONFIG);

        // TODO implement IPv6 writers, NOOP writers are just workaround so we
        // provide at least some writers for IPv6 data
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigWriter(cli)));

        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_RO_CONFIG,
                new Ipv6AdvertisementConfigWriter(cli)));

        // hold-time
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_HO_CONFIG, new HoldTimeConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // if-aggregation
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_AGGREGATION, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_AG_CONFIG, new AggregateConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // bfd
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BFD, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BF_CONFIG,
                new BfdConfigWriter(cli)), IIDs.IN_IN_CONFIG);

        // damping
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_AUG_IFDAMPAUG_DA_CONFIG,
                        new InterfaceDampingConfigWriter(cli)), IIDs.IN_IN_CONFIG);

        // cisco-if extensions
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG,
                        new InterfaceStatisticsConfigWriter(cli)), IIDs.IN_IN_CONFIG);

        // if-ethernet
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET, new NoopCliWriter<>()));
        writeRegistry.subtreeAddAfter(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.IN_IN_ET_CO_AUG_CONFIG1, IFC_ETH_CONFIG_ROOT_ID),
                RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG,
                        IFC_ETH_CONFIG_ROOT_ID)),
                new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);

        // RPF check
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_VERIFYUNICASTSOURCEREACHABLEVIA,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_VE_IPV4, new RpfCheckIpv4Writer(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_AUG_INTERFACE1_VE_IPV6, new RpfCheckIpv6Writer(cli)));

    }

    private void provideReaders(ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES, SubinterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli)));

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
                new Ipv4ConfigReader(cli)));

        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2,
                Subinterface2Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IPV6,
                Ipv6Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_ADDRESSES,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top
                        .ipv6.AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli)));

        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_ROUTERADVERTISEMENT,
                RouterAdvertisementBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_RO_CONFIG,
                new Ipv6AdvertisementConfigReader(cli)));

        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Subinterface1Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VLAN,
                VlanBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigReader(cli)));

        // hold-time
        readRegistry.addStructuralReader(IIDs.IN_IN_HOLDTIME, HoldTimeBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_HO_CONFIG, new HoldTimeConfigReader(cli)));

        // if-aggregation
        readRegistry.addStructuralReader(IIDs.INTER_INTER_AUG_INTERFACE1, Interface1Builder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_INTERFACE1_AGGREGATION, AggregationBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AUG_INTERFACE1_AG_CONFIG,
                new AggregateConfigReader(cli)));

        // bfd
        readRegistry.addStructuralReader(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG, IfLagBfdAugBuilder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BFD, BfdBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BF_CONFIG,
                new BfdConfigReader(cli)));

        // damping
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_IFDAMPAUG, IfDampAugBuilder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_IFDAMPAUG_DAMPING, DampingBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AUG_IFDAMPAUG_DA_CONFIG,
                new InterfaceDampingConfigReader(cli)));

        // cisco if-extensions
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_IFCISCOSTATSAUG, IfCiscoStatsAugBuilder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_STATISTICS, StatisticsBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG,
                new InterfaceStatisticsConfigReader(cli)));

        // if-ethernet
        readRegistry.addStructuralReader(IIDs.INTE_INTE_AUG_INTERFACE1,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                        .rev161222.Interface1Builder.class);
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET, EthernetBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.IN_IN_ET_CO_AUG_CONFIG1, IFC_ETH_CONFIG_ROOT_ID),
                RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG,
                        IFC_ETH_CONFIG_ROOT_ID)),
                new GenericConfigReader<>(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigReader(cli)));

        // RPF check
        readRegistry.addStructuralReader(IIDs.IN_IN_AUG_INTERFACE1,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                        .rev171024.Interface1Builder.class);
        readRegistry.subtreeAdd(
                Sets.newHashSet(RPF_IPV4_SUBTREE_IID, RPF_IPV6_SUBTREE_IID),
                new GenericConfigReader<>(IIDs.IN_IN_AUG_INTERFACE1_VERIFYUNICASTSOURCEREACHABLEVIA,
                        new RpfCheckReader(cli))
        );
    }

    @Override
    public String toString() {
        return "IOS XR Interface (Openconfig) translate unit";
    }

}
