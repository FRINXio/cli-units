/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.cer.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.cer.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.cer.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.cer.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip6.Ipv6ConfigWriter;
import io.frinx.cli.unit.cer.init.CerDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class CerInterfaceUnit extends AbstractUnit {

    public CerInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return CerDevices.CER_ALL;
    }

    @Override
    protected String getUnitName() {
        return "CER Interface (Openconfig) translate unit";
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                IIDs.FRINX_CER_IF_AGGREGATE_EXTENSION,
                IIDs.FRINX_IF_AGGREGATE_EXTENSION,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                IIDs.FRINX_IF_ETHERNET_EXTENSION,
                io.frinx.openconfig.openconfig._if.ip.IIDs.FRINX_OPENCONFIG_IF_IP,
                io.frinx.openconfig.openconfig._if.ip.IIDs.FRINX_OPENCONFIG_IF_IP_EXT,
                $YangModuleInfoImpl.getInstance());
    }

    private void provideWriters(@Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_CO_AUG_IFCISCOEXTAUG));

        writeRegistry.addNoop(IIDs.IN_IN_AUG_INTERFACE1);

        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_SU_SU_CO_AUG_CERIFAGGSUBIFAUG), IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli),
                IIDs.IN_IN_CONFIG, io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigWriter(cli),
                IIDs.IN_IN_CONFIG, io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.subtreeAddAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.IN_IN_SU_SU_CO_AUG_CERIFAGGSUBIFAUG), IIDs.IN_IN_CONFIG);

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli));
    }

}
