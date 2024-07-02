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

package io.frinx.cli.unit.junos.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceVlanTpidConfigReader;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceVlanTpidConfigWriter;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceVlanConfigReader;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.junos.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.junos.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.junos.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class JunosInterfaceUnit extends AbstractUnit {

    public JunosInterfaceUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return JunosDevices.JUNOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Junos Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_DAMPING,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                io.frinx.openconfig.openconfig.lacp.IIDs.FRINX_LACP_LAG_MEMBER,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
        @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry, @NotNull final Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli));

        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_CO_AUG_CONFIG1,
                new InterfaceVlanTpidConfigWriter(cli), IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli), IIDs.IN_IN_CONFIG);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigWriter(cli), IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli), IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_CO_AUG_CONFIG1,
                new InterfaceVlanTpidConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubinterfaceVlanConfigReader(cli));
    }
}