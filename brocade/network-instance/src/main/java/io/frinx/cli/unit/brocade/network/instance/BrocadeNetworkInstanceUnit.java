/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.brocade.init.BrocadeDevices;
import io.frinx.cli.unit.brocade.network.instance.cp.ConnectionPointsReader;
import io.frinx.cli.unit.brocade.network.instance.cp.ConnectionPointsWriter;
import io.frinx.cli.unit.brocade.network.instance.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.brocade.network.instance.ifc.InterfaceReader;
import io.frinx.cli.unit.brocade.network.instance.ifc.InterfaceWriter;
import io.frinx.cli.unit.brocade.network.instance.policy.forwarding.PFInterfaceConfigReader;
import io.frinx.cli.unit.brocade.network.instance.policy.forwarding.PFInterfaceConfigWriter;
import io.frinx.cli.unit.brocade.network.instance.policy.forwarding.PFInterfaceReader;
import io.frinx.cli.unit.brocade.network.instance.vlan.VlanConfigReader;
import io.frinx.cli.unit.brocade.network.instance.vlan.VlanConfigWriter;
import io.frinx.cli.unit.brocade.network.instance.vlan.VlanReader;
import io.frinx.cli.unit.brocade.network.instance.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.brocade.network.instance.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.brocade.network.instance.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class BrocadeNetworkInstanceUnit extends AbstractUnit {

    public BrocadeNetworkInstanceUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return BrocadeDevices.BROCADE_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Ironware Network Instance (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE,
                IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE_TYPES,
                io.frinx.openconfig.openconfig.policy.IIDs.FRINX_OPENCONFIG_ROUTING_POLICY,
                io.frinx.openconfig.openconfig.policy.forwarding.IIDs.FRINX_OPENCONFIG_POLICY_FORWARDING,
                io.frinx.openconfig.openconfig.network.instance.IIDs.FRINX_BROCADE_PF_INTERFACES_EXTENSION,
                io.frinx.openconfig.openconfig.network.instance.IIDs.FRINX_BROCADE_CP_EXTENSION);
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
        writeRegistry.addAfter(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_NE_VL_VLAN);
        writeRegistry.addAfter(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigWriter(cli), IIDs.NE_NE_CONFIG);

        // protocol
        writeRegistry.addNoop(IIDs.NE_NE_PR_PROTOCOL);
        writeRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli));

        // Interfaces for VRF
        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.addAfter(IIDs.NE_NE_IN_INTERFACE, new InterfaceWriter(cli),
                IIDs.NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_IN_IN_CONFIG);

        // Policy-Forwarding
        writeRegistry.addNoop(IIDs.NE_NE_PO_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.NE_NE_PO_IN_IN_CONFIG, new PFInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFBROCADEAUG));

        writeRegistry.subtreeAddAfter(IIDs.NE_NE_CONNECTIONPOINTS, new ConnectionPointsWriter(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_CO_CONNECTIONPOINT,
                        IIDs.NE_NE_CO_CO_CONFIG,
                        IIDs.NE_NE_CO_CO_ENDPOINTS,
                        IIDs.NE_NE_CO_CO_EN_ENDPOINT,
                        IIDs.NE_NE_CO_CO_EN_EN_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_LOCAL,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_CO_AUG_NICPBROCADEAUG,
                        IIDs.NE_NE_CO_CO_EN_EN_REMOTE,
                        IIDs.NE_NE_CO_CO_EN_EN_RE_CONFIG),
                /*handle after network instance configuration*/ IIDs.NE_NE_CONFIG, IIDs.NE_NE_VL_VL_CONFIG);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs, L2P2P
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));

        // protocol
        readRegistry.add(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader());

        readRegistry.add(IIDs.NE_NE_VL_VLAN, new VlanReader(cli));
        readRegistry.add(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigReader(cli));

        // Policy-Forwarding
        readRegistry.add(IIDs.NE_NE_PO_IN_INTERFACE, new PFInterfaceReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PO_IN_IN_CONFIG, new PFInterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFBROCADEAUG));

        // Interfaces for VRF
        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_IN_IN_CONFIG, new InterfaceConfigReader());

        // Connection points for L2P2p
        readRegistry.subtreeAdd(IIDs.NE_NE_CONNECTIONPOINTS, new ConnectionPointsReader(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_CO_CONNECTIONPOINT,
                        IIDs.NE_NE_CO_CO_CONFIG,
                        IIDs.NE_NE_CO_CO_STATE,
                        IIDs.NE_NE_CO_CO_ENDPOINTS,
                        IIDs.NE_NE_CO_CO_EN_ENDPOINT,
                        IIDs.NE_NE_CO_CO_EN_EN_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_STATE,
                        IIDs.NE_NE_CO_CO_EN_EN_LOCAL,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_STATE,
                        IIDs.NE_NE_CO_CO_EN_EN_LO_CO_AUG_NICPBROCADEAUG,
                        IIDs.NE_NE_CO_CO_EN_EN_REMOTE,
                        IIDs.NE_NE_CO_CO_EN_EN_RE_CONFIG,
                        IIDs.NE_NE_CO_CO_EN_EN_RE_STATE));
    }
}