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

package io.frinx.cli.unit.junos.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.junos.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.junos.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.junos.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.junos.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceConfigReader;
import io.frinx.cli.unit.junos.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceConfigWriter;
import io.frinx.cli.unit.junos.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceReader;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.applypolicy.ApplyPolicyConfigReader;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.applypolicy.ApplyPolicyConfigWriter;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc.VrfInterfaceConfigReader;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc.VrfInterfaceConfigWriter;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc.VrfInterfaceReader;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.junos.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class JunosNetworkInstanceUnit extends AbstractUnit {

    private static final Set<InstanceIdentifier<?>> NE_NE_PO_IN_IN_CONFIG_SUBTREE = Sets.newHashSet(
            IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFJUNIPERAUG,
            IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFJUNIPERAUG_CLASSIFIERS,
            IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFJUNIPERAUG_CL_INETPRECEDENCE);

    public JunosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return JunosDevices.JUNOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Junos Network Instance (Openconfig) translate unit";
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));

        // Interface
        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_IN_IN_CONFIG, new VrfInterfaceConfigReader());

        // Apply-Policy
        readRegistry.add(IIDs.NE_NE_IN_AP_CONFIG, new ApplyPolicyConfigReader(cli));

        // Policy-Forwarding
        readRegistry.add(IIDs.NE_NE_PO_IN_INTERFACE, new PolicyForwardingInterfaceReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigReader(cli),
                NE_NE_PO_IN_IN_CONFIG_SUBTREE);

        // Protocol
        readRegistry.add(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_CO_AUG_PROTOCOLCONFAUG));
    }

    private void provideWriters(@Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);

        writeRegistry.addAfter(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        // Interface
        writeRegistry.addAfter(IIDs.NE_NE_IN_INTERFACE, new NoopCliListWriter<>(),
            /*handle after sub ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);
        writeRegistry.add(IIDs.NE_NE_IN_IN_CONFIG, new VrfInterfaceConfigWriter(cli));

        // Apply-Policy
        writeRegistry.add(IIDs.NE_NE_IN_AP_CONFIG, new ApplyPolicyConfigWriter(cli));

        // Policy-Forwarding
        writeRegistry.addNoop(IIDs.NE_NE_PO_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigWriter(cli),
            NE_NE_PO_IN_IN_CONFIG_SUBTREE);

        // Protocol
        writeRegistry.addNoop(IIDs.NE_NE_PR_PROTOCOL);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_CO_AUG_PROTOCOLCONFAUG),
                IIDs.NE_NE_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE,
                IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE_TYPES,
                io.frinx.openconfig.openconfig.policy.IIDs.FRINX_OPENCONFIG_ROUTING_POLICY,
                io.frinx.openconfig.openconfig.policy.forwarding.IIDs.FRINX_OPENCONFIG_POLICY_FORWARDING,
                io.frinx.openconfig.openconfig.network.instance.IIDs.FRINX_JUNIPER_PF_INTERFACES_EXTENSION);
    }
}