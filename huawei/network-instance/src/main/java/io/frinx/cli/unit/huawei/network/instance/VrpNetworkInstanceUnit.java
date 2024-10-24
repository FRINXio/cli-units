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

package io.frinx.cli.unit.huawei.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.huawei.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vlan.VlanConfigReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vlan.VlanConfigWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.vlan.VlanReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.ifc.L3VrfInterfaceReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.ifc.L3VrfInterfaceWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpNetworkInstanceUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpNetworkInstanceUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_CO_AUG_HUAWEINIAUG),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_VL_VLAN);
        writeRegistry.subtreeAddBefore(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigWriter(cli),
                Sets.newHashSet(IIDs.NET_NET_VLA_VLA_CON_AUG_CONFIG1), IIDs.NE_NE_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PROTOCOL);
        writeRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter());

        // Interfaces for VRF
        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.add(IIDs.NE_NE_IN_INTERFACE, new L3VrfInterfaceWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_IN_IN_CONFIG);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli),
                Sets.newHashSet(IIDs.NE_NE_CO_AUG_HUAWEINIAUG));
        readRegistry.add(IIDs.NE_NE_VL_VLAN, new VlanReader(cli));
        readRegistry.add(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigReader(cli));

        // Interfaces for VRF
        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new L3VrfInterfaceReader(cli));

        // Protocols for VRF
        readRegistry.add(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader());
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_HUAWEI_NETWORK_INSTANCE_EXTENSION, IIDs.FRINX_SAOS_VLAN_EXTENSION,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228
                        .$YangModuleInfoImpl.getInstance());
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP Network Instance (Openconfig) translate unit";
    }
}