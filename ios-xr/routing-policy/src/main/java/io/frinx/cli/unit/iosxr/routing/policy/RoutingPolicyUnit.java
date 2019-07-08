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

package io.frinx.cli.unit.iosxr.routing.policy;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.routing.policy.handler.aspath.AsPathSetConfigReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.aspath.AsPathSetConfigWriter;
import io.frinx.cli.unit.iosxr.routing.policy.handler.aspath.AsPathSetReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.community.CommunitySetConfigReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.community.CommunitySetConfigWriter;
import io.frinx.cli.unit.iosxr.routing.policy.handler.community.CommunitySetReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.policy.PolicyConfigReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.policy.PolicyConfigWriter;
import io.frinx.cli.unit.iosxr.routing.policy.handler.policy.PolicyReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.policy.StatementsReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.policy.StatementsWriter;
import io.frinx.cli.unit.iosxr.routing.policy.handler.prefix.PrefixConfigReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.prefix.PrefixReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.prefix.PrefixSetConfigReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.prefix.PrefixSetConfigWriter;
import io.frinx.cli.unit.iosxr.routing.policy.handler.prefix.PrefixSetReader;
import io.frinx.cli.unit.iosxr.routing.policy.handler.prefix.PrefixesWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.bgp.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit extends AbstractUnit {

    public static final Set<InstanceIdentifier<?>> STATEMENTS_CONDITIONS_SUBTREES = Sets.newHashSet(
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CONDITIONS,
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CO_CONFIG,
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CO_MATCHPREFIXSET,
            io.frinx.openconfig.openconfig.policy.IIDs.ROUT_POLI_POLI_STAT_STAT_COND_MATC_CONFIG,
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2,
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BGPCONDITIONS,
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_CONFIG,

            // As path length
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_ASPATHLENGTH,
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_AS_CONFIG,

            // Match As path set
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_MATCHASPATHSET,
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_MA_CONFIG,

            // Match community set
            IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_BG_MATCHCOMMUNITYSET,
            IIDs.ROU_POL_POL_STA_STA_CON_AUG_CONDITIONS2_BGP_MAT_CONFIG);

    public static final Set<InstanceIdentifier<?>> STATEMENTS_ACTIONS_SUBTREES = Sets.newHashSet(
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_ACTIONS,
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_AC_CONFIG,

            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BGPACTIONS,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_CONFIG,

            // As path prepend
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SETASPATHPREPEND,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_CONFIG,

            // Community set
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SETCOMMUNITY,
            IIDs.ROU_POL_POL_STA_STA_ACT_AUG_ACTIONS2_BGP_SET_CONFIG,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_REFERENCE,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_RE_CONFIG,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_INLINE,
            IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_IN_CONFIG);

    // Statements
    public static final Set<InstanceIdentifier<?>> STATEMENTS_SUBTREES = Sets.newHashSet(
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_CONFIG,
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_ACTIONS,
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_ST_AC_CONFIG,
            io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_ST_STATEMENT);

    static {
        STATEMENTS_SUBTREES.addAll(STATEMENTS_CONDITIONS_SUBTREES);
        STATEMENTS_SUBTREES.addAll(STATEMENTS_ACTIONS_SUBTREES);
    }

    public RoutingPolicyUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR Routing policy (OpenConfig) translate unit";
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
        writeRegistry.addNoop(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PREFIXSETS);
        writeRegistry.addNoop(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PREFIXSET);
        writeRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG,
                new PrefixSetConfigWriter(cli));
        // Handle all prefixes at once
        writeRegistry.subtreeAddAfter(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PREFIXES,
                new PrefixesWriter(cli),
                Sets.newHashSet(
                        io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PREFIX,
                        io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PR_CONFIG),
                io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG);

        // Community sets
        writeRegistry.addNoop(IIDs.RO_DE_AUG_DEFINEDSETS2);
        writeRegistry.addNoop(IIDs.RO_DE_AUG_DEFINEDSETS2_BGPDEFINEDSETS);
        writeRegistry.addNoop(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_COMMUNITYSETS);
        writeRegistry.addNoop(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_COMMUNITYSET);
        writeRegistry.add(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG, new CommunitySetConfigWriter(cli));

        // As path sets
        writeRegistry.addNoop(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_ASPATHSETS);
        writeRegistry.addNoop(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_ASPATHSET);
        writeRegistry.add(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_AS_CONFIG, new AsPathSetConfigWriter(cli));

        // Policy definition
        writeRegistry.addNoop(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_POLICYDEFINITION);
        writeRegistry.addAfter(
                io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_CONFIG,
                        new PolicyConfigWriter(cli),
                io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG,
                        IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_AS_CONFIG,
                        IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG);
        writeRegistry.subtreeAddAfter(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_STATEMENTS,
                        new StatementsWriter(cli), STATEMENTS_SUBTREES,
                io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_POLICYDEFINITION);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PREFIXSET, new PrefixSetReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG, new PrefixSetConfigReader());
        readRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PREFIX, new PrefixReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PR_CONFIG, new PrefixConfigReader());

        // Community sets
        readRegistry.add(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_COMMUNITYSET, new CommunitySetReader(cli));
        readRegistry.add(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG, new CommunitySetConfigReader(cli));

        // As path sets
        readRegistry.add(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_ASPATHSET, new AsPathSetReader(cli));
        readRegistry.add(IIDs.RO_DE_AUG_DEFINEDSETS2_BG_AS_AS_CONFIG, new AsPathSetConfigReader(cli));

        // Policy definition
        readRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_POLICYDEFINITION, new PolicyReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_CONFIG, new PolicyConfigReader());
        readRegistry.subtreeAdd(io.frinx.openconfig.openconfig.policy.IIDs.RO_PO_PO_STATEMENTS,
                        new StatementsReader(cli), STATEMENTS_SUBTREES);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                IIDs.FRINX_OPENCONFIG_BGP_POLICY);
    }
}
