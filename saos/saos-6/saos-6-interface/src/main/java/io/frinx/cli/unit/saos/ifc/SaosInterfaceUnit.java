/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.saos.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.saos.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.saos.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.saos.ifc.handler.l2cft.InterfaceCftProfileConfigReader;
import io.frinx.cli.unit.saos.ifc.handler.l2cft.InterfaceCftProfileConfigWriter;
import io.frinx.cli.unit.saos.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.saos.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.saos.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.saos.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.saos.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.saos.ifc.handler.vlan.InterfaceVlanReader;
import io.frinx.cli.unit.saos.ifc.handler.vlan.InterfaceVlanWriter;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosInterfaceUnit extends AbstractUnit {

    public SaosInterfaceUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return new HashSet<Device>() {
            {
                add(SaosDevices.SAOS_6);
            }
        };
    }

    @Override
    protected String getUnitName() {
        return "SAOS Interface unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_SAOS_IF_EXTENSION,
                IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.addAfter(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);
        readRegistry.add(IIDs.IN_IN_ET_CO_AUG_CONFIG1, new AggregateConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                new InterfaceVlanReader(cli));

        readRegistry.add(IIDs.IN_IN_AUG_SAOS6IFCFTAUG_CF_CONFIG, new InterfaceCftProfileConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli));

    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Collections.singleton(IIDs.IN_IN_CO_AUG_IFSAOSAUG),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);

        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                new InterfaceVlanWriter(cli), io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_VL_VL_CONFIG,
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);

        writeRegistry.addNoop(IIDs.IN_IN_AUG_SAOS6IFCFTAUG_CFTPROFILE);
        writeRegistry.add(IIDs.IN_IN_AUG_SAOS6IFCFTAUG_CF_CONFIG, new InterfaceCftProfileConfigWriter(cli));
    }
}
