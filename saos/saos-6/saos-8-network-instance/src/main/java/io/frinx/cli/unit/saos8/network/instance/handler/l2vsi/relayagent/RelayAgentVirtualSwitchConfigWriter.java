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
package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.relayagent;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev220626.saos.ra.extension.relay.agent.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentVirtualSwitchConfigWriter implements CliWriter<Config> {

    private static final String WRITE_RELAY_AGENT_VIRTUAL_SWITCH =
            """
                    dhcp l2-relay-agent create vs {$virtualSwitchName}
                    {% if($enable) %}dhcp l2-relay-agent {$enable} vs {$virtualSwitchName}
                    {% endif %}""";

    private static final String UPDATE_RELAY_AGENT_VIRTUAL_SWITCH =
            "{% if($enable) %}dhcp l2-relay-agent {$enable} vs {$virtualSwitchName}\n{% endif %}";

    private static final String DELETE_RELAY_AGENT_VIRTUAL_SWITCH =
            "dhcp l2-relay-agent delete vs {$virtualSwitchName}";

    private final Cli cli;

    public RelayAgentVirtualSwitchConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String virtualSwitchName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, writeTemplate(dataAfter, virtualSwitchName));
    }

    @VisibleForTesting
    String writeTemplate(Config config, String virtualSwitchName) {
        return fT(WRITE_RELAY_AGENT_VIRTUAL_SWITCH, "data", config,
                "virtualSwitchName", virtualSwitchName,
                "enable", config.isEnable() ? "enable" : "disable");
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        String virtualSwitchName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, updateTemplate(dataAfter, virtualSwitchName));
    }

    @VisibleForTesting
    String updateTemplate(Config dataAfter, String virtualSwitchName) {
        return fT(UPDATE_RELAY_AGENT_VIRTUAL_SWITCH, "data", dataAfter,
                "virtualSwitchName", virtualSwitchName,
                "enable", dataAfter.isEnable() ? "enable" : "disable");
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String virtualSwitchName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        blockingDelete(deleteTemplate(dataBefore, virtualSwitchName), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String virtualSwitchName) {
        return fT(DELETE_RELAY_AGENT_VIRTUAL_SWITCH, "data", config,
                "virtualSwitchName", virtualSwitchName);
    }
}