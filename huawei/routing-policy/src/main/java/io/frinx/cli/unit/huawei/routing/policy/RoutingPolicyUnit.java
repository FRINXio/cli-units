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

package io.frinx.cli.unit.huawei.routing.policy;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.huawei.routing.policy.handler.ExtCommunitySetConfigWriter;
import io.frinx.cli.unit.huawei.routing.policy.handler.ExtCommunitySetReader;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.defined.sets.top.DefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit implements TranslateUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public RoutingPolicyUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(HUAWEI, this);
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
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // provide writers
        writeRegistry.add(new GenericWriter<>(IIDs.ROUTINGPOLICY, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DEFINEDSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BGPDEFINEDSETS,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EXTCOMMUNITYSETS,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EX_EXTCOMMUNITYSET,
                new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EX_EX_CONFIG,
                        new ExtCommunitySetConfigWriter(cli)),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // provide readers
        readRegistry.addStructuralReader(IIDs.ROUTINGPOLICY, RoutingPolicyBuilder.class);
        readRegistry.addStructuralReader(IIDs.RO_DEFINEDSETS, DefinedSetsBuilder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2,
                DefinedSets2Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BGPDEFINEDSETS,
                BgpDefinedSetsBuilder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EXTCOMMUNITYSETS,
                ExtCommunitySetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EX_EXTCOMMUNITYSET,
                new ExtCommunitySetReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                $YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.$YangModuleInfoImpl
                        .getInstance());
    }

    @Override
    public String toString() {
        return "VRP Routing policy (Openconfig) translate unit";
    }
}
