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

package io.frinx.cli.unit.huawei.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.huawei.ifc.handler.ethernet.EthernetConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.ethernet.EthernetConfigWriter;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceStateReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.huawei.ifc.handler.vlan.InterfaceVlanReader;
import io.frinx.cli.unit.huawei.ifc.handler.vlan.InterfaceVlanWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpCliInterfaceUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpCliInterfaceUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.$YangModuleInfoImpl
                        .getInstance(),
                IIDs.FRINX_HUAWEI_IF_EXTENSION,
                IIDs.FRINX_CISCO_IF_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_CO_AUG_IFHUAWEIAUG,
                        IIDs.IN_IN_CO_AUG_IFHUAWEIAUG_TRAFFICFILTER,
                        IIDs.IN_IN_CO_AUG_IFHUAWEIAUG_TRAFFICPOLICY));

        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_SU_SU_CO_AUG_SUBIFHUAWEIAUG,
                        IIDs.IN_IN_SU_SU_CO_AUG_SUBIFHUAWEIAUG_TRAFFICFILTER,
                        IIDs.IN_IN_SU_SU_CO_AUG_SUBIFHUAWEIAUG_TRAFFICPOLICY));

        writeRegistry.addNoop(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                new InterfaceVlanWriter(cli),
                IIDs.IN_IN_CONFIG, io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_VL_VL_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                new Ipv4ConfigWriter(cli), IIDs.IN_IN_CONFIG, IIDs.IN_IN_SU_SU_CONFIG,
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.IN_IN_AUG_INTERFACE1_ETHERNET);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_ET_CO_AUG_CONFIG1),
                IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_STATE, new InterfaceStateReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_STATE, new SubinterfaceStateReader());

        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                    new Ipv4AddressReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                    new Ipv4ConfigReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                new InterfaceVlanReader(cli));
        readRegistry.subtreeAdd(IIDs.IN_IN_AUG_INTERFACE1_ET_CONFIG, new EthernetConfigReader(cli),
                Sets.newHashSet(IIDs.IN_IN_ET_CO_AUG_CONFIG1));
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP Interface (Openconfig) translate unit";
    }
}