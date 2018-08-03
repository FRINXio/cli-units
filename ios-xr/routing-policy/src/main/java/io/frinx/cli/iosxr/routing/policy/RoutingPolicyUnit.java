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

package io.frinx.cli.iosxr.routing.policy;

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
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.iosxr.routing.policy.handler.aspath.AsPathSetConfigReader;
import io.frinx.cli.iosxr.routing.policy.handler.aspath.AsPathSetConfigWriter;
import io.frinx.cli.iosxr.routing.policy.handler.aspath.AsPathSetReader;
import io.frinx.cli.iosxr.routing.policy.handler.community.CommunitySetConfigReader;
import io.frinx.cli.iosxr.routing.policy.handler.community.CommunitySetConfigWriter;
import io.frinx.cli.iosxr.routing.policy.handler.community.CommunitySetReader;
import io.frinx.cli.iosxr.routing.policy.handler.policy.PolicyConfigReader;
import io.frinx.cli.iosxr.routing.policy.handler.policy.PolicyConfigWriter;
import io.frinx.cli.iosxr.routing.policy.handler.policy.PolicyReader;
import io.frinx.cli.iosxr.routing.policy.handler.policy.StatementsReader;
import io.frinx.cli.iosxr.routing.policy.handler.policy.StatementsWriter;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixConfigReader;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixReader;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixSetConfigReader;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixSetConfigWriter;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixSetReader;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixesWriter;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.length.top.AsPathLength;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrepend;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.AsPathSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.AsPathSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.CommunitySets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.CommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.as.path.top.MatchAsPathSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.MatchCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.SetCommunity;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.Inline;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.reference.top.Reference;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.defined.sets.top.DefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.PrefixSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.Prefixes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.PrefixesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicyBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit implements TranslateUnit {

    public static final InstanceIdentifier<DefinedSets2> IIDS_DS_BGP_AUG
            = IIDs.RO_DEFINEDSETS.augmentation(DefinedSets2.class);
    public static final InstanceIdentifier<BgpDefinedSets> IIDS_BGP_DEF_SETS = IIDS_DS_BGP_AUG.child(BgpDefinedSets
            .class);
    public static final InstanceIdentifier<CommunitySets> IIDS_BGP_COMM_SETS = IIDS_BGP_DEF_SETS.child(CommunitySets
            .class);
    public static final InstanceIdentifier<CommunitySet> IIDS_BGP_COMM_SET = IIDS_BGP_COMM_SETS.child(CommunitySet
            .class);
    public static final InstanceIdentifier<Config> IIDS_BGP_COMM_SET_CFG = IIDS_BGP_COMM_SET.child(Config.class);

    public static final InstanceIdentifier<AsPathSets> IIDS_AS_PATH_SETS = IIDS_BGP_DEF_SETS.child(AsPathSets.class);
    public static final InstanceIdentifier<AsPathSet> IIDS_AS_PATH_SET = IIDS_AS_PATH_SETS.child(AsPathSet.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.as.path.set.top.as.path.sets.as.path.set.Config> IIDS_AS_PATH_SET_CFG =
            IIDS_AS_PATH_SET.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.as.path.set.top.as.path.sets.as.path.set.Config.class);

    public static final InstanceIdentifier<Statements> STATEMENTS_ID = InstanceIdentifier.create(Statements.class);

    // Conditions
    public static final InstanceIdentifier<Conditions2> BGP_CONDITIONS_AUG_ID = IIDs.RO_PO_PO_ST_ST_CONDITIONS
            .augmentation(Conditions2.class);
    public static final InstanceIdentifier<BgpConditions> BGP_CONDITIONS_ID
            = BGP_CONDITIONS_AUG_ID.child(BgpConditions.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.bgp.conditions.top.bgp.conditions.Config> BGP_CONDITIONS_CFG_ID =
            BGP_CONDITIONS_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.bgp.conditions.top.bgp.conditions.Config.class);

    public static final InstanceIdentifier<AsPathLength> AS_PATH_COND_ID = BGP_CONDITIONS_ID.child(AsPathLength.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.as.path.length.top.as.path.length.Config> AS_PATH_COND_CFG_ID =
            AS_PATH_COND_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as
                    .path.length.top.as.path.length.Config.class);

    public static final InstanceIdentifier<MatchAsPathSet> MATCH_AS_PATH_COND_ID
            = BGP_CONDITIONS_ID.child(MatchAsPathSet.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.match.as.path.top.match.as.path.set.Config> MATCH_AS_PATH_COND_CFG_ID =
            MATCH_AS_PATH_COND_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.match.as.path.top.match.as.path.set.Config.class);

    public static final InstanceIdentifier<MatchCommunitySet> MATCH_COMMUNITY_COND_ID
            = BGP_CONDITIONS_ID.child(MatchCommunitySet.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.match.community.top.match.community.set.Config> MATCH_COMMUNITY_COND_CFG_ID =
            MATCH_COMMUNITY_COND_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.match.community.top.match.community.set.Config.class);

    public static final Set<InstanceIdentifier<?>> STATEMENTS_CONDITIONS_SUBTREES = Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CONDITIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_CONFIG, STATEMENTS_ID),

            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_MATCHPREFIXSET, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.ROUT_POLI_POLI_STAT_STAT_COND_MATC_CONFIG, STATEMENTS_ID),

            RWUtils.cutIdFromStart(BGP_CONDITIONS_AUG_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(BGP_CONDITIONS_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(BGP_CONDITIONS_CFG_ID, STATEMENTS_ID),

            // As path length
            RWUtils.cutIdFromStart(AS_PATH_COND_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(AS_PATH_COND_CFG_ID, STATEMENTS_ID),

            // Match As path set
            RWUtils.cutIdFromStart(MATCH_AS_PATH_COND_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(MATCH_AS_PATH_COND_CFG_ID, STATEMENTS_ID),

            // Match community set
            RWUtils.cutIdFromStart(MATCH_COMMUNITY_COND_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(MATCH_COMMUNITY_COND_CFG_ID, STATEMENTS_ID)
    );

    // Actions
    public static final InstanceIdentifier<Actions2> BGP_ACTIONS_AUG_ID
            = IIDs.RO_PO_PO_ST_ST_ACTIONS.augmentation(Actions2.class);
    public static final InstanceIdentifier<BgpActions> BGP_ACTIONS_ID = BGP_ACTIONS_AUG_ID.child(BgpActions.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.bgp.actions.top.bgp.actions.Config> BGP_ACTIONS_CFG_ID =
            BGP_ACTIONS_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp
                    .actions.top.bgp.actions.Config.class);

    public static final InstanceIdentifier<SetAsPathPrepend> AS_PATH_PREPEND_ACT_ID
            = BGP_ACTIONS_ID.child(SetAsPathPrepend.class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.as.path.prepend.top.set.as.path.prepend.Config> AS_PATH_PREPAND_ACT_CFG_ID =
            AS_PATH_PREPEND_ACT_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.as.path.prepend.top.set.as.path.prepend.Config.class);

    public static final InstanceIdentifier<SetCommunity> SET_COMMUNITY_ACT_ID = BGP_ACTIONS_ID.child(SetCommunity
            .class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.set.community.action.top.set.community.Config> SET_COMMUNITY_ACT_CFG_ID =
            SET_COMMUNITY_ACT_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.set.community.action.top.set.community.Config.class);
    public static final InstanceIdentifier<Reference> SET_COMMUNITY_ACT_REF_ID = SET_COMMUNITY_ACT_ID.child(Reference
            .class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.set.community.reference.top.reference.Config> SET_COMMUNITY_ACT_REF_CFG_ID =
            SET_COMMUNITY_ACT_REF_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.set.community.reference.top.reference.Config.class);
    public static final InstanceIdentifier<Inline> SET_COMMUNITY_ACT_INLINE_ID = SET_COMMUNITY_ACT_ID.child(Inline
            .class);
    public static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
            .rev170730.set.community.inline.top.inline.Config> SET_COMMUNITY_ACT_INLINE_CFG_ID =
            SET_COMMUNITY_ACT_INLINE_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.set.community.inline.top.inline.Config.class);

    public static final Set<InstanceIdentifier<?>> STATEMENTS_ACTIONS_SUBTREES = Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_ACTIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_CONFIG, STATEMENTS_ID),

            RWUtils.cutIdFromStart(BGP_ACTIONS_AUG_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(BGP_ACTIONS_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(BGP_ACTIONS_CFG_ID, STATEMENTS_ID),

            // As path prepend
            RWUtils.cutIdFromStart(AS_PATH_PREPEND_ACT_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(AS_PATH_PREPAND_ACT_CFG_ID, STATEMENTS_ID),

            // Community set
            RWUtils.cutIdFromStart(SET_COMMUNITY_ACT_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(SET_COMMUNITY_ACT_CFG_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(SET_COMMUNITY_ACT_REF_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(SET_COMMUNITY_ACT_REF_CFG_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(SET_COMMUNITY_ACT_INLINE_ID, STATEMENTS_ID),
            RWUtils.cutIdFromStart(SET_COMMUNITY_ACT_INLINE_CFG_ID, STATEMENTS_ID)
    );

    // Statements
    public static final Set<InstanceIdentifier<?>> STATEMENTS_SUBTREES = Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CONFIG, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_ACTIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_CONFIG, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_STATEMENT, STATEMENTS_ID));

    static {
        STATEMENTS_SUBTREES.addAll(STATEMENTS_CONDITIONS_SUBTREES);
        STATEMENTS_SUBTREES.addAll(STATEMENTS_ACTIONS_SUBTREES);
    }

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public RoutingPolicyUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        // Prefix sets
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_PREFIXSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.RO_DE_PR_PREFIXSET, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_PR_PR_CONFIG, new PrefixSetConfigWriter(cli)));
        // Handle all prefixes at once
        writeRegistry.subtreeAddAfter(
                Sets.newHashSet(
                        RWUtils.cutIdFromStart(IIDs.RO_DE_PR_PR_PR_PREFIX, InstanceIdentifier.create(Prefixes.class)),
                        RWUtils.cutIdFromStart(IIDs.RO_DE_PR_PR_PR_PR_CONFIG,
                                InstanceIdentifier.create(Prefixes.class))),
                new GenericWriter<>(IIDs.RO_DE_PR_PR_PREFIXES, new PrefixesWriter(cli)),
                IIDs.RO_DE_PR_PR_CONFIG);

        // Community sets
        writeRegistry.add(new GenericWriter<>(IIDS_DS_BGP_AUG, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDS_BGP_DEF_SETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDS_BGP_COMM_SETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDS_BGP_COMM_SET, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDS_BGP_COMM_SET_CFG, new CommunitySetConfigWriter(cli)));

        // As path sets
        writeRegistry.add(new GenericWriter<>(IIDS_AS_PATH_SETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDS_AS_PATH_SET, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDS_AS_PATH_SET_CFG, new AsPathSetConfigWriter(cli)));

        // Policy definition
        writeRegistry.add(new GenericListWriter<>(IIDs.RO_PO_POLICYDEFINITION, new NoopCliListWriter<>()));
        writeRegistry.addAfter(
                new GenericWriter<>(IIDs.RO_PO_PO_CONFIG, new PolicyConfigWriter(cli)),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_CONFIG, IIDS_AS_PATH_SET_CFG, IIDS_BGP_COMM_SET_CFG));
        writeRegistry.subtreeAddAfter(STATEMENTS_SUBTREES,
                new GenericWriter<>(IIDs.RO_PO_PO_STATEMENTS, new StatementsWriter(cli)),
                IIDs.RO_PO_POLICYDEFINITION);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.ROUTINGPOLICY, RoutingPolicyBuilder.class);
        readRegistry.addStructuralReader(IIDs.RO_DEFINEDSETS, DefinedSetsBuilder.class);

        // Prefix sets
        readRegistry.addStructuralReader(IIDs.RO_DE_PREFIXSETS, PrefixSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.RO_DE_PR_PREFIXSET, new PrefixSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_DE_PR_PR_CONFIG, new PrefixSetConfigReader()));
        readRegistry.addStructuralReader(IIDs.RO_DE_PR_PR_PREFIXES, PrefixesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.RO_DE_PR_PR_PR_PREFIX, new PrefixReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_DE_PR_PR_PR_PR_CONFIG, new PrefixConfigReader()));

        // Community sets
        readRegistry.addStructuralReader(IIDS_DS_BGP_AUG, DefinedSets2Builder.class);
        readRegistry.addStructuralReader(IIDS_BGP_DEF_SETS, BgpDefinedSetsBuilder.class);
        readRegistry.addStructuralReader(IIDS_BGP_COMM_SETS, CommunitySetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDS_BGP_COMM_SET, new CommunitySetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDS_BGP_COMM_SET_CFG, new CommunitySetConfigReader(cli)));

        // As path sets
        readRegistry.addStructuralReader(IIDS_AS_PATH_SETS, AsPathSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDS_AS_PATH_SET, new AsPathSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDS_AS_PATH_SET_CFG, new AsPathSetConfigReader(cli)));

        // Policy definition
        readRegistry.addStructuralReader(IIDs.RO_POLICYDEFINITIONS, PolicyDefinitionsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.RO_PO_POLICYDEFINITION, new PolicyReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_PO_PO_CONFIG, new PolicyConfigReader()));
        readRegistry.subtreeAdd(STATEMENTS_SUBTREES,
                new GenericConfigReader<>(IIDs.RO_PO_PO_STATEMENTS, new StatementsReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.$YangModuleInfoImpl
                        .getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR Routing policy (OpenConfig) translate unit";
    }
}
