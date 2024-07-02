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

package io.frinx.cli.unit.saos.network.instance.handler.vlan.relayagent;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentVlanConfigWriter implements CliWriter<Config> {

    private static final String WRITE_RELAY_AGENT_VLAN =
            """
                    dhcp l2-relay-agent create vlan {$vlanId}
                    {% if($enable) %}dhcp l2-relay-agent {$enable} vlan {$vlanId}
                    {% endif %}""";

    private static final String UPDATE_RELAY_AGENT_VLAN =
            "{% if($enable) %}dhcp l2-relay-agent {$enable} vlan {$vlanId}\n{% endif %}";

    private static final String DELETE_RELAY_AGENT_VLAN =
            "dhcp l2-relay-agent delete vlan {$vlanId}";

    private final Cli cli;

    public RelayAgentVlanConfigWriter(Cli cli) {
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
                "enable", config.isEnable() ? "enable" : "disable");
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
                "enable", dataAfter.isEnable() ? "enable" : "disable");
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
                "vlanId", vlanId);
    }
}