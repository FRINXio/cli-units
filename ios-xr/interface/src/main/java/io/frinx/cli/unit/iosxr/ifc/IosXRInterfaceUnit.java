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
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
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
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceArpConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceArpConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceStatisticsConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceStatisticsConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceVlanConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm.CfmConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm.CfmConfigWriter;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm.CfmDomainReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm.CfmDomainWriter;
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
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXRInterfaceUnit extends AbstractUnit {

    public IosXRInterfaceUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                IIDs.FRINX_OPENCONFIG_IF_AGGREGATE,
                IIDs.FRINX_BFD,
                IIDs.FRINX_DAMPING,
                IIDs.FRINX_CISCO_IF_EXTENSION,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                IIDs.FRINX_IF_ETHERNET_EXTENSION,
                io.frinx.openconfig.openconfig.lacp.IIDs.FRINX_LACP_LAG_MEMBER,
                io.frinx.openconfig.openconfig.oam.IIDs.FRINX_OAM,
                $YangModuleInfoImpl.getInstance());
    }

    private static final CheckRegistry CHECK_REGISTRY;

    static {
        CheckRegistry.Builder builder = new CheckRegistry.Builder();
        builder.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_ROUTERADVERTISEMENT,
                BasicCheck.checkData(ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
                        ChecksMap.DataCheck.InterfaceConfig.TYPE_ETHERNET_CSMACD)
                        .or(BasicCheck.checkData(ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
                                ChecksMap.DataCheck.InterfaceConfig.TYPE_IEEE802AD_LAG)));
        CHECK_REGISTRY = builder.build();
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        Cli cli = context.getTransport();
        readRegistry.addCheckRegistry(CHECK_REGISTRY);
        // TODO lot of handlers are exactly the same as the handlers from
        // ios-interface unit. Extract common logic and reuse it
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli));

        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli),
                IIDs.IN_IN_CONFIG);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigWriter(cli), IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli), IIDs.IN_IN_CONFIG);

        // TODO implement IPv6 writers, NOOP writers are just workaround so we
        // provide at least some writers for IPv6 data
        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS);
        writeRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigWriter(cli));

        writeRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_RO_CONFIG,
                new Ipv6AdvertisementConfigWriter(cli));

        // hold-time
        writeRegistry.addAfter(IIDs.IN_IN_HO_CONFIG, new HoldTimeConfigWriter(cli),
                IIDs.IN_IN_CONFIG);

        // if-aggregation
        writeRegistry.addNoop(IIDs.IN_IN_AUG_INTERFACE1_AGGREGATION);
        writeRegistry.addAfter(IIDs.IN_IN_AUG_INTERFACE1_AG_CONFIG, new AggregateConfigWriter(cli),
                IIDs.IN_IN_CONFIG);

        // bfd
        writeRegistry.addNoop(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BFD);
        writeRegistry.addAfter(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BF_CONFIG, new BfdConfigWriter(cli), IIDs.IN_IN_CONFIG);

        // damping
        writeRegistry.addAfter(IIDs.IN_IN_AUG_IFDAMPAUG_DA_CONFIG,
                new InterfaceDampingConfigWriter(cli), IIDs.IN_IN_CONFIG);

        // cisco-if extensions
        writeRegistry.addAfter(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG,
                new InterfaceStatisticsConfigWriter(cli), IIDs.IN_IN_CONFIG);
        writeRegistry.addNoop(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCISCOSTATSAUG_STATISTICS);
        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCISCOSTATSAUG_ST_CONFIG,
            new SubinterfaceStatisticsConfigWriter(cli), IIDs.IN_IN_SU_SU_CONFIG);

        // if-ethernet
        writeRegistry.addNoop(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_ET_CO_AUG_CONFIG1,
                io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG),
                IIDs.IN_IN_CONFIG);

        // RPF check
        writeRegistry.addNoop(IIDs.IN_IN_AUG_INTERFACE1_VERIFYUNICASTSOURCEREACHABLEVIA);
        writeRegistry.add(IIDs.IN_IN_AUG_INTERFACE1_VE_IPV4, new RpfCheckIpv4Writer(cli));
        writeRegistry.add(IIDs.IN_IN_AUG_INTERFACE1_VE_IPV6, new RpfCheckIpv6Writer(cli));

        // cfm
        writeRegistry.addNoop(io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CFM);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_CONFIG,
                new CfmConfigWriter(cli), IIDs.IN_IN_SU_SU_CONFIG);
        writeRegistry.addNoop(io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DOMAINS);
        writeRegistry.subtreeAddAfter(
            io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DOMAIN,
            new CfmDomainWriter(cli), Sets.newHashSet(
                io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DO_CONFIG,
                io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DO_MEP,
                io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DO_ME_CONFIG),
            io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_CONFIG);

        // if-ethernet extension
        writeRegistry.addNoop(IIDs.IN_IN_SU_SU_AUG_SUBIFAUGETHEXT_ARP);
        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_AUG_SUBIFAUGETHEXT_AR_CONFIG, new SubinterfaceArpConfigWriter(cli),
            IIDs.IN_IN_SU_SU_CONFIG);

    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_RO_CONFIG,
                new Ipv6AdvertisementConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigReader(cli));

        // hold-time
        readRegistry.add(IIDs.IN_IN_HO_CONFIG, new HoldTimeConfigReader(cli));

        // if-aggregation
        readRegistry.add(IIDs.IN_IN_AUG_INTERFACE1_AG_CONFIG, new AggregateConfigReader(cli));

        // bfd
        readRegistry.add(IIDs.IN_IN_AG_AUG_IFLAGBFDAUG_BF_CONFIG, new BfdConfigReader(cli));

        // damping
        readRegistry.add(IIDs.IN_IN_AUG_IFDAMPAUG_DA_CONFIG, new InterfaceDampingConfigReader(cli));

        // cisco if-extensions
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG, new InterfaceStatisticsConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCISCOSTATSAUG_ST_CONFIG,
            new SubinterfaceStatisticsConfigReader(cli));

        // if-ethernet
        readRegistry.subtreeAdd(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigReader(cli),
                Sets.newHashSet(IIDs.IN_IN_ET_CO_AUG_CONFIG1,
                io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG));

        // RPF check
        readRegistry.subtreeAdd(IIDs.IN_IN_AUG_INTERFACE1_VERIFYUNICASTSOURCEREACHABLEVIA, new RpfCheckReader(cli),
                Sets.newHashSet(IIDs.IN_IN_AUG_INTERFACE1_VE_IPV4,
                        IIDs.IN_IN_AUG_INTERFACE1_VE_IPV6));

        // cfm
        readRegistry.add(io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_CONFIG,
                new CfmConfigReader(cli));
        readRegistry.subtreeAdd(
            io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DOMAIN,
            new CfmDomainReader(cli), Sets.newHashSet(
                io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DO_CONFIG,
                io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DO_MEP,
                io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DO_ME_CONFIG));

        // if-ethernet extension
        readRegistry.add(IIDs.IN_IN_SU_SU_AUG_SUBIFAUGETHEXT_AR_CONFIG, new SubinterfaceArpConfigReader(cli));
    }
}