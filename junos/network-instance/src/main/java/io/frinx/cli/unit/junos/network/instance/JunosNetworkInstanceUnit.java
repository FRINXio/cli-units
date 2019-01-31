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
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.junos.JunosDevices;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
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
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterInstancePoliciesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.policy.forwarding.top.PolicyForwardingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class JunosNetworkInstanceUnit implements TranslateUnit {

    // IIDs.NE_NE_PO_IN_IN_CONFIG subtree
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy
        .forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config> NE_NE_PO_IN_IN_CONFIG_ROOT =
            InstanceIdentifier.create(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621
                .pf.interfaces.structural.interfaces._interface.Config.class);

    private static final Set<InstanceIdentifier<?>> NE_NE_PO_IN_IN_CONFIG_SUBTREE = Sets.newHashSet(
        RWUtils.cutIdFromStart(IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFJUNIPERAUG, NE_NE_PO_IN_IN_CONFIG_ROOT),
        RWUtils.cutIdFromStart(IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFJUNIPERAUG_CLASSIFIERS, NE_NE_PO_IN_IN_CONFIG_ROOT),
        RWUtils.cutIdFromStart(
            IIDs.NE_TO_NO_CO_NE_NE_PO_IN_IN_CO_AUG_NIPFIFJUNIPERAUG_CL_INETPRECEDENCE,
            NE_NE_PO_IN_IN_CONFIG_ROOT));

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public JunosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(JunosDevices.JUNOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli)));

        // Interface
        readRegistry.addStructuralReader(IIDs.NE_NE_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_IN_INTERFACE, new VrfInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_IN_IN_CONFIG, new VrfInterfaceConfigReader()));

        // Apply-Policy
        readRegistry.addStructuralReader(IIDs.NE_NE_INTERINSTANCEPOLICIES, InterInstancePoliciesBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_IN_APPLYPOLICY, ApplyPolicyBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_IN_AP_CONFIG, new ApplyPolicyConfigReader(cli)));

        // Policy-Forwarding
        readRegistry.addStructuralReader(IIDs.NE_NE_POLICYFORWARDING, PolicyForwardingBuilder.class);
        readRegistry.addStructuralReader(
            IIDs.NE_NE_PO_INTERFACES,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621
            .pf.interfaces.structural.InterfacesBuilder.class);
        readRegistry.add(
            new GenericConfigListReader<>(IIDs.NE_NE_PO_IN_INTERFACE, new PolicyForwardingInterfaceReader(cli)));
        readRegistry.subtreeAdd(
            NE_NE_PO_IN_IN_CONFIG_SUBTREE,
            new GenericConfigReader<>(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigReader(cli)));
    }

    private void provideWriters(@Nonnull ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NETWORKINSTANCE, new NoopCliWriter<>()));

        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli)),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        // Interface
        writeRegistry.addAfter(new GenericListWriter<>(IIDs.NE_NE_IN_INTERFACE, new NoopCliListWriter<>()),
            /*handle after sub ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_IN_CONFIG, new VrfInterfaceConfigWriter(cli)));

        // Apply-Policy
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_IN_AP_CONFIG, new ApplyPolicyConfigWriter(cli)));

        // Policy-Forwarding
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PO_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.subtreeAdd(
            NE_NE_PO_IN_IN_CONFIG_SUBTREE,
            new GenericWriter<>(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigWriter(cli)));

    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                        .rev170228.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types
                        .rev170228.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy
                        .rev170714.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding
                        .rev170621.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension
                        .juniper.rev171109.$YangModuleInfoImpl.getInstance()
            );
    }

    @Override
    public String toString() {
        return "Junos Network Instance (Openconfig) translate unit";
    }
}
