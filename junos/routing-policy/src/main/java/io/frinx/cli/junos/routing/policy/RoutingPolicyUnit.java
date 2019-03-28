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

package io.frinx.cli.junos.routing.policy;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.junos.routing.policy.handler.actions.ActionsConfigReader;
import io.frinx.cli.junos.routing.policy.handler.actions.ActionsConfigWriter;
import io.frinx.cli.junos.routing.policy.handler.actions.ospf.actions.SetMetricConfigReader;
import io.frinx.cli.junos.routing.policy.handler.actions.ospf.actions.SetMetricConfigWriter;
import io.frinx.cli.junos.routing.policy.handler.conditions.protocol.ProtocolConfigReader;
import io.frinx.cli.junos.routing.policy.handler.conditions.protocol.ProtocolConfigWriter;
import io.frinx.cli.junos.routing.policy.handler.policy.PolicyConfigReader;
import io.frinx.cli.junos.routing.policy.handler.policy.PolicyConfigWriter;
import io.frinx.cli.junos.routing.policy.handler.policy.PolicyListReader;
import io.frinx.cli.junos.routing.policy.handler.policy.StatementConfigReader;
import io.frinx.cli.junos.routing.policy.handler.policy.StatementConfigWriter;
import io.frinx.cli.junos.routing.policy.handler.policy.StatementListReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.Conditions1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.MatchProtocolInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.Actions1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.OspfActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.SetMetricBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.defined.sets.top.DefinedSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.routing.policy.top.RoutingPolicyBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RoutingPolicyUnit implements TranslateUnit {
    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public RoutingPolicyUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        // Policy definition
        writeRegistry.add(new GenericListWriter<>(IIDs.RO_PO_POLICYDEFINITION,
                new NoopCliListWriter<>()));
        writeRegistry.addAfter(
                new GenericWriter<>(IIDs.RO_PO_PO_CONFIG,
                new PolicyConfigWriter(cli)),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_CONFIG));

        // statement
        writeRegistry.add(new GenericListWriter<>(IIDs.RO_PO_PO_ST_STATEMENT,
                new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.RO_PO_PO_ST_ST_CONFIG,
                new StatementConfigWriter(cli)),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_CONFIG, IIDs.RO_PO_PO_CONFIG));

        // protocol type and instance name in conditions
        writeRegistry.addAfter(new GenericWriter<>(
                io.frinx.openconfig.openconfig.network.instance.IIDs
                .RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_MA_CONFIG,
                new ProtocolConfigWriter(cli)),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_CONFIG,
                        IIDs.RO_PO_PO_CONFIG,
                        IIDs.RO_PO_PO_ST_ST_CONFIG));

        // action config
        writeRegistry.addAfter(new GenericWriter<>(
                IIDs.RO_PO_PO_ST_ST_AC_CONFIG,
                new ActionsConfigWriter(cli)),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_CONFIG,
                        IIDs.RO_PO_PO_CONFIG,
                        IIDs.RO_PO_PO_ST_ST_CONFIG));

        // metric in actions
        writeRegistry.addAfter(new GenericWriter<>(
                io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_OS_SE_CONFIG,
                new SetMetricConfigWriter(cli)),
                Sets.newHashSet(IIDs.RO_DE_PR_PR_CONFIG,
                        IIDs.RO_PO_PO_CONFIG,
                        IIDs.RO_PO_PO_ST_ST_CONFIG,
                        IIDs.RO_PO_PO_ST_ST_AC_CONFIG));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.ROUTINGPOLICY,
                RoutingPolicyBuilder.class);
        readRegistry.addStructuralReader(IIDs.RO_DEFINEDSETS,
                DefinedSetsBuilder.class);

        // Policy definition
        readRegistry.addStructuralReader(IIDs.RO_POLICYDEFINITIONS,
                PolicyDefinitionsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                IIDs.RO_PO_POLICYDEFINITION, new PolicyListReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_PO_PO_CONFIG,
                new PolicyConfigReader()));

        // statements
        readRegistry.addStructuralReader(IIDs.RO_PO_PO_STATEMENTS,
                StatementsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                IIDs.RO_PO_PO_ST_STATEMENT,
                new StatementListReader(cli)));
        readRegistry.add(new GenericConfigReader<>(
                IIDs.RO_PO_PO_ST_ST_CONFIG,
                new StatementConfigReader()));

        // conditions
        readRegistry.addStructuralReader(
                IIDs.RO_PO_PO_ST_ST_CONDITIONS,
                ConditionsBuilder.class);

        // conditions -- protocol type and instance name in conditions
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.network.instance.IIDs
                .RO_PO_PO_ST_ST_CO_AUG_CONDITIONS1,
                Conditions1Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.network.instance.IIDs
                .RO_PO_PO_ST_ST_CO_AUG_CONDITIONS1_MATCHPROTOCOLINSTANCE,
                MatchProtocolInstanceBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig.network.instance.IIDs
                .RO_PO_PO_ST_ST_CO_AUG_CONDITIONS1_MA_CONFIG,
                new ProtocolConfigReader(cli)));

        // actions
        readRegistry.addStructuralReader(IIDs.RO_PO_PO_ST_ST_ACTIONS, ActionsBuilder.class);

        // actions -- configure
        readRegistry.add(new GenericConfigReader<>(IIDs.RO_PO_PO_ST_ST_AC_CONFIG,
                new ActionsConfigReader(cli)));

        // actions/ospf-actions/set-metric
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS1,
                Actions1Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS1_OSPFACTIONS,
                OspfActionsBuilder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS1_OS_SETMETRIC,
                SetMetricBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS1_OS_SE_CONFIG,
                new SetMetricConfigReader(cli)));

    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy
                        .rev170215.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy
                        .rev160822.$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "Junos Routing policy (OpenConfig) translate unit";
    }
}
