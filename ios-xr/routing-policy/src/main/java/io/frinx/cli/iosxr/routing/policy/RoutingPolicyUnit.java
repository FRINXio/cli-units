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
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
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
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.bgp.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.AsPathSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.CommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSetsBuilder;
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

    public static final InstanceIdentifier<Statements> STATEMENTS_ID = InstanceIdentifier.create(Statements.class);

    public static final Set<InstanceIdentifier<?>> STATEMENTS_CONDITIONS_SUBTREES = Sets.newHashSet(
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CONDITIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CO_CONFIG, STATEMENTS_ID),

            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CO_MATCHPREFIXSET,
                    STATEMENTS_ID),
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.ROUT_POLI_POLI_STAT_STAT_COND_MATC_CONFIG,
                    STATEMENTS_ID),

            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BGPCONDITIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_CONFIG, STATEMENTS_ID),

            // As path length
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_ASPATHLENGTH, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_AS_CONFIG, STATEMENTS_ID),

            // Match As path set
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_MATCHASPATHSET, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_MA_CONFIG, STATEMENTS_ID),

            // Match community set
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_MATCHCOMMUNITYSET, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.ROU_POL_POL_STA_STA_CON_AUG_CONDITIONS2_BGP_MAT_CONFIG, STATEMENTS_ID)
    );

    public static final Set<InstanceIdentifier<?>> STATEMENTS_ACTIONS_SUBTREES = Sets.newHashSet(
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_ACTIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_AC_CONFIG, STATEMENTS_ID),

            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BGPACTIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_CONFIG, STATEMENTS_ID),

            // As path prepend
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SETASPATHPREPEND, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_CONFIG, STATEMENTS_ID),

            // Community set
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SETCOMMUNITY, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.ROU_POL_POL_STA_STA_ACT_AUG_ACTIONS2_BGP_SET_CONFIG, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_REFERENCE, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_RE_CONFIG, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_INLINE, STATEMENTS_ID),
            RWUtils.cutIdFromStart(IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_IN_CONFIG, STATEMENTS_ID)
    );

    // Statements
    public static final Set<InstanceIdentifier<?>> STATEMENTS_SUBTREES = Sets.newHashSet(
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CONFIG, STATEMENTS_ID),
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_ACTIONS, STATEMENTS_ID),
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_AC_CONFIG, STATEMENTS_ID),
            RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_STATEMENT, STATEMENTS_ID));

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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        // Prefix sets
        writeRegistry.add(new GenericWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PREFIXSETS,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PREFIXSET,
                new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG,
                new PrefixSetConfigWriter(cli)));
        // Handle all prefixes at once
        writeRegistry.subtreeAddAfter(
                Sets.newHashSet(
                        RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PREFIX,
                                InstanceIdentifier.create(Prefixes.class)),
                        RWUtils.cutIdFromStart(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PR_CONFIG,
                                InstanceIdentifier.create(Prefixes.class))),
                new GenericWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PREFIXES,
                        new PrefixesWriter(cli)),
                io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG);

        // Community sets
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BGPDEFINEDSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_COMMUNITYSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_COMMUNITYSET,
                new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG,
                new CommunitySetConfigWriter(cli)));

        // As path sets
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_ASPATHSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_ASPATHSET,
                new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_AS_CONFIG,
                new AsPathSetConfigWriter(cli)));

        // Policy definition
        writeRegistry.add(new GenericListWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_POLICYDEFINITION,
                new NoopCliListWriter<>()));
        writeRegistry.addAfter(
                new GenericWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_CONFIG,
                        new PolicyConfigWriter(cli)),
                Sets.newHashSet(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG,
                        IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_AS_CONFIG,
                        IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG));
        writeRegistry.subtreeAddAfter(STATEMENTS_SUBTREES,
                new GenericWriter<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_STATEMENTS,
                        new StatementsWriter(cli)), io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_POLICYDEFINITION);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.policy.IIDs.ROUTINGPOLICY,
                RoutingPolicyBuilder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.policy.IIDs.RO_DEFINEDSETS,
                DefinedSetsBuilder.class);

        // Prefix sets
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PREFIXSETS,
                PrefixSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PREFIXSET,
                new PrefixSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG,
                new PrefixSetConfigReader()));
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PREFIXES,
                PrefixesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PREFIX,
                new PrefixReader(cli)));
        readRegistry.add(new GenericConfigReader<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PR_CONFIG,
                new PrefixConfigReader()));

        // Community sets
        readRegistry.addStructuralReader(IIDs.RO_DE_AUG_DEFINEDSETS2, DefinedSets2Builder.class);
        readRegistry.addStructuralReader(IIDs.RO_DE_AUG_DEFINEDSETS2_BGPDEFINEDSETS, BgpDefinedSetsBuilder.class);
        readRegistry.addStructuralReader(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_COMMUNITYSETS, CommunitySetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_COMMUNITYSET,
                new CommunitySetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG,
                new CommunitySetConfigReader(cli)));

        // As path sets
        readRegistry.addStructuralReader(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_ASPATHSETS, AsPathSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_ASPATHSET,
                new AsPathSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_AS_CONFIG,
                new AsPathSetConfigReader(cli)));

        // Policy definition
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.policy.IIDs.RO_POLICYDEFINITIONS,
                PolicyDefinitionsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_POLICYDEFINITION, new PolicyReader(cli)));
        readRegistry.add(new GenericConfigReader<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_CONFIG,
                new PolicyConfigReader()));
        readRegistry.subtreeAdd(STATEMENTS_SUBTREES,
                new GenericConfigReader<>(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_STATEMENTS,
                        new StatementsReader(cli)));
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
