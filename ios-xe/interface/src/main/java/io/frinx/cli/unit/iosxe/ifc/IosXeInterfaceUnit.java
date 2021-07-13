/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceStatisticsConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceStatisticsConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.cfm.CfmMipReader;
import io.frinx.cli.unit.iosxe.ifc.handler.cfm.CfmMipWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.L2protocolConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.L2protocolReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.ServiceInstanceBridgeDomainReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.ServiceInstanceConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.ServiceInstanceEncapsulationReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.ServiceInstanceReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.ServiceInstanceRewriteReader;
import io.frinx.cli.unit.iosxe.ifc.handler.service.instance.ServiceInstanceWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.SubinterfaceStateReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4.Ipv4VrrpGroupConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4.Ipv4VrrpGroupConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4.Ipv4VrrpGroupReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6.Ipv6ConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6.Ipv6VrrpGroupConfigReader;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6.Ipv6VrrpGroupConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6.Ipv6VrrpGroupReader;
import io.frinx.cli.unit.iosxe.init.IosXeDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXeInterfaceUnit extends AbstractUnit {

    public IosXeInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXeDevices.IOS_XE_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS-XE Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                IIDs.FRINX_CISCO_IF_EXTENSION,
                IIDs.FRINX_SAOS_IF_EXTENSION,
                IIDs.FRINX_IF_ETHERNET_EXTENSION,
                io.frinx.openconfig.openconfig._if.ip.IIDs.FRINX_OPENCONFIG_IF_IP,
                io.frinx.openconfig.openconfig._if.ip.IIDs.FRINX_CISCO_VRRP_EXTENSION,
                io.frinx.openconfig.openconfig._if.ip.IIDs.FRINX_OPENCONFIG_IF_IP_EXT,
                io.frinx.openconfig.openconfig.lacp.IIDs.FRINX_LACP_LAG_MEMBER,
                io.frinx.openconfig.openconfig.oam.IIDs.FRINX_OAM,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(@Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_CO_AUG_IFCISCOEXTAUG,
                        IIDs.IN_IN_CO_AUG_IFCISCOEXTAUG_SERVICEPOLICY,
                        IIDs.IN_IN_CO_AUG_IFCISCOEXTAUG_STORMCONTROL,
                        IIDs.IN_IN_CO_AUG_IFSAOSAUG));

        writeRegistry.subtreeAdd(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG, new ServiceInstanceWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SERVICEINSTANCES,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SERVICEINSTANCE,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_CONFIG,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_ENCAPSULATION,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_BRIDGEDOMAIN,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_L2PROTOCOLS,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_L2_SERVICEINSTANCEL2PROTOCOL,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_L2_SE_L2PROTOCOL,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_L2_SE_L2_CONFIG,
                        IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_REWRITE));

        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli), IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli),
                IIDs.IN_IN_CONFIG, io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip
                .IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV4VRRPAUG_VR_VRRPGROUP);
        writeRegistry.subtreeAdd(io.frinx.openconfig.openconfig._if.ip
                .IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV4VRRPAUG_VR_VR_CONFIG,
                new Ipv4VrrpGroupConfigWriter(cli),
                Sets.newHashSet(
                    io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV4VRRPAUG_VR_VR_CONFIG
                        .augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco
                                .vrrp.ext.rev210521.Config1.class),
                    io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV4VRRPAUG_VR_VR_CONFIG
                        .augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco
                                .vrrp.ext.rev210521.Config1.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext
                                .rev210521.ipv4.vrrp.group.config.TrackedObjects.class)));

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigWriter(cli),
                IIDs.IN_IN_CONFIG, io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip
                .IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV6VRRPAUG_VR_VRRPGROUP);
        writeRegistry.subtreeAdd(io.frinx.openconfig.openconfig._if.ip
                        .IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV6VRRPAUG_VR_VR_CONFIG,
                new Ipv6VrrpGroupConfigWriter(cli),
                Sets.newHashSet(
                    io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV6VRRPAUG_VR_VR_CONFIG
                        .augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco
                            .vrrp.ext.rev210521.Config2.class),
                    io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV6VRRPAUG_VR_VR_CONFIG
                        .augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco
                            .vrrp.ext.rev210521.Config2.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext
                            .rev210521.ipv6.vrrp.group.config.TrackedObjects.class)));

        writeRegistry.addAfter(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG,
                new InterfaceStatisticsConfigWriter(cli), IIDs.IN_IN_CONFIG);

        // cfm
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_AUG_IFCFMAUG_CF_MI_LEVEL,
                new CfmMipWriter(cli), IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_STATE, new InterfaceStateReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SERVICEINSTANCE,
                new ServiceInstanceReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_CONFIG,
                new ServiceInstanceConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_ENCAPSULATION,
                new ServiceInstanceEncapsulationReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_BRIDGEDOMAIN,
                new ServiceInstanceBridgeDomainReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_L2_SE_L2PROTOCOL, new L2protocolReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_L2_SE_L2_CONFIG,
                new L2protocolConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSERVICEINSTANCEAUG_SE_SE_REWRITE, new ServiceInstanceRewriteReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_STATE, new SubinterfaceStateReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigReader(cli));

        readRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip
                .IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV4VRRPAUG_VR_VRRPGROUP,
                new Ipv4VrrpGroupReader(cli),
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV4VRRPAUG_VR_VR_CONFIG,
                new Ipv4VrrpGroupConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip
                        .IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV6VRRPAUG_VR_VRRPGROUP,
                new Ipv6VrrpGroupReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_IP_AD_AD_AUG_IFIPV6VRRPAUG_VR_VR_CONFIG,
                new Ipv6VrrpGroupConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_AUG_IFCISCOSTATSAUG_ST_CONFIG, new InterfaceStatisticsConfigReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.oam.IIDs.IN_IN_AUG_IFCFMAUG_CF_MI_LEVEL, new CfmMipReader(cli));
    }

}
