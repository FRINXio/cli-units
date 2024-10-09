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

package io.frinx.cli.unit.saos.network.instance.handler.vlan.relayagent.port;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.config.ports.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentPortConfigWriter implements CliWriter<Config> {

    private static final String WRITE_RELAY_AGENT_VLAN =
            "{% if($trustMode) %}dhcp l2-relay-agent set vlan {$vlanId} "
            + "port {$portName} trust-mode {$trustMode}\n{% endif %}";

    private static final String UPDATE_RELAY_AGENT_VLAN =
            """
                    {% if ($trustModeAfter) %}dhcp l2-relay-agent unset vlan {$vlanId} port {$portName} trust-mode
                    dhcp l2-relay-agent set vlan {$vlanId} port {$portName} trust-mode {$trustModeAfter}
                    {% else %}dhcp l2-relay-agent unset vlan {$vlanId} port {$portName} trust-mode
                    {% endif %}""";

    private static final String DELETE_RELAY_AGENT_VLAN =
            "dhcp l2-relay-agent unset vlan {$vlanId} port {$portName} trust-mode\n";

    private final Cli cli;

    public RelayAgentPortConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        var vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, writeTemplate(dataAfter, vlanId.getValue()));
    }

    @VisibleForTesting
    String writeTemplate(Config config, int vlanId) {
        return fT(WRITE_RELAY_AGENT_VLAN, "data", config,
                "vlanId", vlanId,
                "portName", config.getPortName(),
                "trustMode", config.getTrustMode() != null ? config.getTrustMode().getName() : null);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        var vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, updateTemplate(dataAfter, vlanId.getValue()));
    }

    @VisibleForTesting
    String updateTemplate(Config dataAfter, int vlanId) {
        return fT(UPDATE_RELAY_AGENT_VLAN, "data", dataAfter,
                "vlanId", vlanId,
                "portName", dataAfter.getPortName(),
                "trustModeAfter", dataAfter.getTrustMode() != null ? dataAfter.getTrustMode().getName() : null);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        var vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        blockingDelete(deleteTemplate(dataBefore, vlanId.getValue()), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String deleteTemplate(Config config, int vlanId) {
        return fT(DELETE_RELAY_AGENT_VLAN, "data", config,
                "vlanId", vlanId,
                "portName", config.getPortName());
    }
}