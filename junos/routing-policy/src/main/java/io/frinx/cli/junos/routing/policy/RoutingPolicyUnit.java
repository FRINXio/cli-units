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
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
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
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.policy.IIDs;
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
        return JunosDevices.JUNOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Junos Routing policy (OpenConfig) translate unit";
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
        // Policy definition
        writeRegistry.addNoop(IIDs.RO_PO_POLICYDEFINITION);
        writeRegistry.addAfter(IIDs.RO_PO_PO_CONFIG, new PolicyConfigWriter(cli), IIDs.RO_DE_PR_PR_CONFIG);

        // statement
        writeRegistry.addNoop(IIDs.RO_PO_PO_ST_STATEMENT);
        writeRegistry.addAfter(IIDs.RO_PO_PO_ST_ST_CONFIG, new StatementConfigWriter(cli),
                IIDs.RO_DE_PR_PR_CONFIG,
                IIDs.RO_PO_PO_CONFIG);

        // protocol type and instance name in conditions
        writeRegistry.addAfter(
                io.frinx.openconfig.openconfig.network.instance.IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS2_MA_CONFIG,
                new ProtocolConfigWriter(cli),
                IIDs.RO_DE_PR_PR_CONFIG,
                IIDs.RO_PO_PO_CONFIG,
                IIDs.RO_PO_PO_ST_ST_CONFIG);

        // action config
        writeRegistry.addAfter(IIDs.RO_PO_PO_ST_ST_AC_CONFIG, new ActionsConfigWriter(cli),
                IIDs.RO_DE_PR_PR_CONFIG,
                IIDs.RO_PO_PO_CONFIG,
                IIDs.RO_PO_PO_ST_ST_CONFIG);

        // metric in actions
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS2_OS_SE_CONFIG,
                new SetMetricConfigWriter(cli),
                IIDs.RO_DE_PR_PR_CONFIG,
                IIDs.RO_PO_PO_CONFIG,
                IIDs.RO_PO_PO_ST_ST_CONFIG,
                IIDs.RO_PO_PO_ST_ST_AC_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // Policy definition
        readRegistry.add(IIDs.RO_PO_POLICYDEFINITION, new PolicyListReader(cli));
        readRegistry.add(IIDs.RO_PO_PO_CONFIG, new PolicyConfigReader());

        // statements
        readRegistry.add(IIDs.RO_PO_PO_ST_STATEMENT, new StatementListReader(cli));
        readRegistry.add(IIDs.RO_PO_PO_ST_ST_CONFIG, new StatementConfigReader());

        // conditions -- protocol type and instance name in conditions
        readRegistry.add(
                io.frinx.openconfig.openconfig.network.instance.IIDs.RO_PO_PO_ST_ST_CO_AUG_CONDITIONS1_MA_CONFIG,
                new ProtocolConfigReader(cli));

        // actions -- configure
        readRegistry.add(IIDs.RO_PO_PO_ST_ST_AC_CONFIG, new ActionsConfigReader(cli));

        // actions/ospf-actions/set-metric
        readRegistry.add(io.frinx.openconfig.openconfig.ospf.IIDs.RO_PO_PO_ST_ST_AC_AUG_ACTIONS1_OS_SE_CONFIG,
                new SetMetricConfigReader(cli));

    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                io.frinx.openconfig.openconfig.network.instance.IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE_POLICY,
                io.frinx.openconfig.openconfig.ospf.IIDs.FRINX_OPENCONFIG_OSPF_POLICY);
    }
}
