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

package io.frinx.cli.unit.nexus.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceStatisticsConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceStatisticsConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.TpIdInterfaceReader;
import io.frinx.cli.unit.nexus.ifc.handler.TpIdInterfaceWriter;
import io.frinx.cli.unit.nexus.ifc.handler.ethernet.EthernetConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.ethernet.EthernetConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.SubinterfaceVlanConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.SubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv4.Ipv4AddressReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv4.Ipv4ConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv4.Ipv4ConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6.Ipv6AddressReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6.Ipv6AdvertisementConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6.Ipv6AdvertisementConfigWriter;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6.Ipv6ConfigReader;
import io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6.Ipv6ConfigWriter;
import io.frinx.cli.unit.nexus.init.NexusDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class NexusInterfaceUnit extends AbstractUnit {

    public NexusInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return NexusDevices.NEXUS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Nexus Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                io.frinx.openconfig.openconfig.lacp.IIDs.FRINX_LACP_LAG_MEMBER,
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
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        readRegistry.addCheckRegistry(CHECK_REGISTRY);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli));
        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);

        //subifc
        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli), IIDs.IN_IN_CONFIG);

        //vlan
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigWriter(cli), IIDs.IN_IN_SU_SU_CONFIG);

        //tpid
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_CO_AUG_CONFIG1,
                new TpIdInterfaceWriter(cli), IIDs.IN_IN_CONFIG);

        //ipv4
        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli), IIDs.IN_IN_CONFIG);

        //ipv6
        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS);
        writeRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigWriter(cli));
        writeRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_RO_CONFIG,
                new Ipv6AdvertisementConfigWriter(cli));

        //ethernet
        writeRegistry.addNoop(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_ET_CO_AUG_CONFIG1,
                io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG),
                IIDs.IN_IN_CONFIG);

        //statistics
        writeRegistry.addAfter(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG, new InterfaceStatisticsConfigWriter(cli),
                IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        //subifc
        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli));

        //vlan
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigReader(cli));

        //tpid
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_CO_AUG_CONFIG1, new TpIdInterfaceReader(cli));

        //ipv4
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigReader(cli));

        //ipv6
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_RO_CONFIG,
                new Ipv6AdvertisementConfigReader(cli));

        //ethernet
        readRegistry.subtreeAdd(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigReader(cli),
                Sets.newHashSet(IIDs.IN_IN_ET_CO_AUG_CONFIG1,
                io.frinx.openconfig.openconfig.lacp.IIDs.IN_IN_ET_CO_AUG_LACPETHCONFIGAUG));

        //statistics
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG, new InterfaceStatisticsConfigReader(cli));
    }
}
