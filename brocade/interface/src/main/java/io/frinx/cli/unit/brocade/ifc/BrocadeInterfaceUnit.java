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

package io.frinx.cli.unit.brocade.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.brocade.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.brocade.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.brocade.ifc.handler.TpIdInterfaceReader;
import io.frinx.cli.unit.brocade.ifc.handler.TpIdInterfaceWriter;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.SubinterfaceStateReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.brocade.init.BrocadeDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class BrocadeInterfaceUnit extends AbstractUnit {

    public BrocadeInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return BrocadeDevices.BROCADE_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Ironware Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_IF_ETHERNET,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                io.frinx.openconfig.openconfig._if.ip.IIDs.FRINX_OPENCONFIG_IF_IP);
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writerRegistry, Cli cli) {
        writerRegistry.addNoop(IIDs.IN_INTERFACE);
        writerRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli));

        writerRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_CO_AUG_CONFIG1,
                new TpIdInterfaceWriter(cli), IIDs.IN_IN_CONFIG);

        writerRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writerRegistry.addNoop(IIDs.IN_IN_SU_SU_CONFIG);
        writerRegistry.addNoop(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG);

        writerRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writerRegistry.addAfter(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli), IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_STATE, new InterfaceStateReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_CO_AUG_CONFIG1, new TpIdInterfaceReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_STATE, new SubinterfaceStateReader(cli));

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
