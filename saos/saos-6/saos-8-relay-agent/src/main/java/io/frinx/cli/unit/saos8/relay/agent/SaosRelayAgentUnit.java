/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.saos8.relay.agent;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos8.relay.agent.handler.RelayAgentConfigReader;
import io.frinx.cli.unit.saos8.relay.agent.handler.RelayAgentConfigWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.relay.agent.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosRelayAgentUnit extends AbstractUnit {

    public SaosRelayAgentUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Set.of(SaosDevices.SAOS_8);
    }

    @Override
    protected String getUnitName() {
        return "Saos-8 Relay Agent (Openconfig) translate unit";
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_RELAY_AGENT,
                IIDs.FRINX_SAOS_RELAY_AGENT_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.RELAYAGENT);
        writeRegistry.add(IIDs.RE_DH_AUG_SAOSRELAYAGENTAUG_RE_CONFIG, new RelayAgentConfigWriter(cli));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.RE_DH_AUG_SAOSRELAYAGENTAUG_RE_CONFIG, new RelayAgentConfigReader(cli));
    }
}