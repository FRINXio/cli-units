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
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.handlers.def.DefaultConfigWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceStateReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding.PolicyForwardingInterfaceReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolLocalAggregateConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolLocalAggregateReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolStateReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.LocalAggregatesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.Aggregate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.policy.forwarding.top.PolicyForwardingBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRNetworkInstanceUnit implements TranslateUnit {

    private static final InstanceIdentifier<Config> PF_IFC_CFG_ROOT_ID = InstanceIdentifier.create(Config.class);
    private static final InstanceIdentifier<Aggregate> AGGREGATE_IID = InstanceIdentifier.create(Aggregate.class);

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosXRNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        CheckRegistry checkRegistry = ChecksMap.getOpenconfigCheckRegistry();
        readRegistry.setCheckRegistry(checkRegistry);
        provideReaders(readRegistry, cli);
        writeRegistry.setCheckRegistry(checkRegistry);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_STATE, new NetworkInstanceStateReader(cli)));

        readRegistry.addStructuralReader(IIDs.NE_NE_PROTOCOLS, ProtocolsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader()));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader()));

        // PF
        readRegistry.addStructuralReader(IIDs.NE_NE_POLICYFORWARDING, PolicyForwardingBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_PO_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(
                new GenericConfigListReader<>(IIDs.NE_NE_PO_IN_INTERFACE, new PolicyForwardingInterfaceReader(cli)));
        readRegistry.add(
                new GenericConfigReader<>(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigReader(cli)));


        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_LOCALAGGREGATES, LocalAggregatesBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, AGGREGATE_IID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_LO_AG_CO_AUG_NIPROTAGGAUG, AGGREGATE_IID)),
                new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new ProtocolLocalAggregateReader(cli)));
    }

    private void provideWriters(@Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NETWORKINSTANCE, new NoopCliWriter<>()));

        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_CONFIG,
                        new CompositeWriter<>(Lists.newArrayList(
                                new DefaultConfigWriter()))),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PROTOCOL, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_LOCALAGGREGATES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new NoopCliListWriter<>()));
        writeRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_LO_AG_CO_AUG_NIPROTAGGAUG,
            InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx
            .openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.aggregate.Config.class))),
                new GenericWriter<>(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new ProtocolLocalAggregateConfigWriter(cli)));
        // PF
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PO_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.subtreeAddAfter(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PO_IN_IN_CO_AUG_NIPFIFCISCOAUG, PF_IFC_CFG_ROOT_ID)),
                new GenericWriter<>(IIDs.NE_NE_PO_IN_IN_CONFIG, new PolicyForwardingInterfaceConfigWriter(cli)),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                        .rev170228.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding
                        .rev170621.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension
                        .cisco.rev171109.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.$YangModuleInfoImpl
                        .getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR Network Instance (Openconfig) translate unit";
    }
}
