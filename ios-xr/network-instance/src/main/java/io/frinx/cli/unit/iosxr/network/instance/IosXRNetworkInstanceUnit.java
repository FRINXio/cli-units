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

package io.frinx.cli.unit.iosxr.network.instance;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.handlers.def.DefaultConfigWriter;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceStateReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolStateReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRNetworkInstanceUnit extends AbstractUnit {

    public IosXRNetworkInstanceUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR Network Instance (Openconfig) translate unit";
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_STATE, new NetworkInstanceStateReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader());

        // PF
        readRegistry.add(IIDs.NE_NE_PO_IN_INTERFACE, new PolicyForwardingInterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigReader(cli));
    }

    private void provideWriters(@NotNull CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);

        writeRegistry.addAfter(IIDs.NE_NE_CONFIG,
                        new CompositeWriter<>(Lists.newArrayList(
                                new DefaultConfigWriter())),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PROTOCOL);
        writeRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli));

        // PF
        writeRegistry.addNoop(IIDs.NE_NE_PO_IN_INTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFCISCOAUG),
                /*handle after sub-ifc configuration*/
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE,
                io.frinx.openconfig.openconfig.policy.forwarding.IIDs.FRINX_OPENCONFIG_POLICY_FORWARDING,
                IIDs.FRINX_CISCO_PF_INTERFACES_EXTENSION,
                IIDs.FRINX_BGP_EXTENSION);
    }
}