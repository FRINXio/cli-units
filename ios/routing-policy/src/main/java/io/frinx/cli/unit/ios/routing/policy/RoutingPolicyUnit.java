/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.routing.policy.handlers.PolicyConfigReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.PolicyReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.PolicyWriter;
import io.frinx.cli.unit.ios.routing.policy.handlers.StatementConfigReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.StatementReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.action.BgpActionsConfigReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.aspath.AsPathPrependConfigReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.community.ExtCommunitySetConfigWriter;
import io.frinx.cli.unit.ios.routing.policy.handlers.community.ExtCommunitySetReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.prefix.PrefixConfigReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.prefix.PrefixConfigWriter;
import io.frinx.cli.unit.ios.routing.policy.handlers.prefix.PrefixReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.prefix.PrefixSetConfigReader;
import io.frinx.cli.unit.ios.routing.policy.handlers.prefix.PrefixSetReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit extends AbstractUnit {

    public RoutingPolicyUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return new HashSet<Device>() {
            {
                add(IosDevices.IOS_12);
                add(IosDevices.IOS_15);
                add(IosDevices.IOS_XE_16);
            }
        };
    }

    @Override
    protected String getUnitName() {
        return "IOS Routing policy (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                $YangModuleInfoImpl.getInstance(),
                io.frinx.openconfig.openconfig.bgp.IIDs.FRINX_OPENCONFIG_BGP_POLICY,
                IIDs.FRINX_CISCO_ROUTING_POLICY_EXTENSION);
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readerRegistryBuilder,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writerRegistryBuilder,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readerRegistryBuilder, cli);
        provideWriters(writerRegistryBuilder, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writerRegistryBuilder, Cli cli) {
        // Prefix sets
        writerRegistryBuilder.addNoop(IIDs.RO_DE_PREFIXSETS);
        writerRegistryBuilder.addNoop(IIDs.RO_DE_PR_PREFIXSET);
        writerRegistryBuilder.addNoop(IIDs.RO_DE_PR_PR_PREFIXES);
        writerRegistryBuilder.addNoop(IIDs.RO_DE_PR_PR_PR_PREFIX);
        writerRegistryBuilder.addNoop(IIDs.RO_DE_PR_PR_CONFIG);
        writerRegistryBuilder.subtreeAdd(IIDs.RO_DE_PR_PR_PR_PR_CONFIG, new PrefixConfigWriter(cli),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_PR_PR_CO_AUG_PREFIXCONFIGAUG));

        writerRegistryBuilder.addNoop(IIDs.ROUTINGPOLICY);
        writerRegistryBuilder.addNoop(IIDs.RO_DEFINEDSETS);
        writerRegistryBuilder.addNoop(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2);
        writerRegistryBuilder.addNoop(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BGPDEFINEDSETS);
        writerRegistryBuilder.addNoop(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EXTCOMMUNITYSETS);
        writerRegistryBuilder.addNoop(
                io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EX_EXTCOMMUNITYSET);
        writerRegistryBuilder.addAfter(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EX_EX_CONFIG,
                new ExtCommunitySetConfigWriter(cli),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);

        writerRegistryBuilder.addNoop(IIDs.RO_POLICYDEFINITIONS);
        writerRegistryBuilder.subtreeAdd(IIDs.RO_PO_POLICYDEFINITION, new PolicyWriter(cli),
                Sets.newHashSet(IIDs.RO_PO_PO_CONFIG,
                        IIDs.RO_PO_PO_STATEMENTS,
                        IIDs.RO_PO_PO_ST_STATEMENT,
                        IIDs.RO_PO_PO_ST_ST_CONFIG,
                        IIDs.RO_PO_PO_ST_ST_CO_AUG_PREFIXLISTAUG,
                        IIDs.RO_PO_PO_ST_ST_ACTIONS,
                        io.frinx.openconfig.openconfig.bgp.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2,
                        io.frinx.openconfig.openconfig.bgp.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BGPACTIONS,
                        io.frinx.openconfig.openconfig.bgp.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_CONFIG,
                        io.frinx.openconfig.openconfig.bgp.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SETASPATHPREPEND,
                        io.frinx.openconfig.openconfig.bgp.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_BG_SE_CONFIG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readerRegistryBuilder, Cli cli) {
        readerRegistryBuilder
                .add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PREFIXSET, new PrefixSetReader(cli));
        readerRegistryBuilder.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_CONFIG,
                new PrefixSetConfigReader());
        readerRegistryBuilder
                .add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PREFIX, new PrefixReader(cli));
        readerRegistryBuilder.add(io.frinx.openconfig.openconfig.policy.IIDs.RO_DE_PR_PR_PR_PR_CONFIG,
                new PrefixConfigReader(cli));

        readerRegistryBuilder.add(IIDs.RO_PO_POLICYDEFINITION, new PolicyReader(cli));
        readerRegistryBuilder.add(IIDs.RO_PO_PO_CONFIG, new PolicyConfigReader());
        readerRegistryBuilder.add(IIDs.RO_PO_PO_ST_STATEMENT, new StatementReader(cli));
        readerRegistryBuilder.add(IIDs.RO_PO_PO_ST_ST_CONFIG, new StatementConfigReader(cli));
        readerRegistryBuilder.add(io.frinx.openconfig.openconfig.bgp.IIDs
                .RO_PO_PO_ST_ST_AC_AUG_ACTIONS1_BG_CONFIG, new BgpActionsConfigReader(cli));
        readerRegistryBuilder.add(io.frinx.openconfig.openconfig.bgp.IIDs
                .RO_PO_PO_ST_ST_AC_AUG_ACTIONS1_BG_SE_CONFIG, new AsPathPrependConfigReader(cli));

        readerRegistryBuilder.add(io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EX_EXTCOMMUNITYSET,
                new ExtCommunitySetReader(cli));
    }
}
