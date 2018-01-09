/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.routing.policy;

import static io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.defined.sets.top.DefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit implements TranslateUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    private static final InstanceIdentifier<DefinedSets2> DEFINED_SETS_1 =
            IIDs.RO_DEFINEDSETS.augmentation(DefinedSets2.class);
    private static final InstanceIdentifier<BgpDefinedSets> BGP_DEFINED_SETS =
            DEFINED_SETS_1.child(BgpDefinedSets.class);
    private static final InstanceIdentifier<ExtCommunitySets> EXT_COMMUNITY_SETS =
            BGP_DEFINED_SETS.child(ExtCommunitySets.class);
    private static final InstanceIdentifier<ExtCommunitySet> EXT_COMMUNITY_SET =
            EXT_COMMUNITY_SETS.child(ExtCommunitySet.class);
    private static final InstanceIdentifier<Config> EXT_CS_CONFIG = EXT_COMMUNITY_SET.child(Config.class);

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
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        // provide writers
        wRegistry.add(new GenericWriter<>(IIDs.ROUTINGPOLICY, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.RO_DEFINEDSETS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(DEFINED_SETS_1, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(BGP_DEFINED_SETS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(EXT_COMMUNITY_SETS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(EXT_COMMUNITY_SET, new NoopCliWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(EXT_CS_CONFIG,new ExtCommunitySetConfigWriter(cli)), NE_NE_CONFIG);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // provide readers
        rRegistry.addStructuralReader(IIDs.ROUTINGPOLICY, RoutingPolicyBuilder.class);
        rRegistry.addStructuralReader(IIDs.RO_DEFINEDSETS, DefinedSetsBuilder.class);
        rRegistry.addStructuralReader(DEFINED_SETS_1, DefinedSets2Builder.class);
        rRegistry.addStructuralReader(BGP_DEFINED_SETS, BgpDefinedSetsBuilder.class);
        rRegistry.addStructuralReader(EXT_COMMUNITY_SETS, ExtCommunitySetsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(EXT_COMMUNITY_SET, new ExtCommunitySetReader(cli)));
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
