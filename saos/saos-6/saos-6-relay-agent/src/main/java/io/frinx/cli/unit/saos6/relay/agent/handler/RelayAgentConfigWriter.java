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
package io.frinx.cli.unit.saos6.relay.agent.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentConfigWriter implements CliWriter<Config> {

    private static final String WRITE_RELAY_AGENT =
            """
                    {% if ($enable) %}dhcp l2-relay-agent {$enable}
                    {% endif %}{% if ($remoteIdType) %}dhcp l2-relay-agent set remote-id-type {$remoteIdType}
                    {% endif %}{% if ($replaceOption82) %}dhcp l2-relay-agent set replace-option82 {$replaceOption82}
                    {% endif %}""";

    private static final String DELETE_RELAY_AGENT =
            """
                    {% if ($enable) %}dhcp l2-relay-agent disable
                    {% endif %}{% if ($remoteIdType) %}dhcp l2-relay-agent unset remote-id-type
                    {% endif %}{% if ($replaceOption82) %}dhcp l2-relay-agent unset replace-option82
                    {% endif %}""";

    private final Cli cli;

    public RelayAgentConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config));
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        return fT(WRITE_RELAY_AGENT, "data", config,
                "enable", setEnable(config),
                "remoteIdType", config.getRemoteIdType() != null ? config.getRemoteIdType().getName() : null,
                "replaceOption82", setReplaceOption82(config));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingDelete(deleteTemplate(config), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_RELAY_AGENT, "data", config,
                "enable", setEnable(config),
                "remoteIdType", config.getRemoteIdType() != null ? config.getRemoteIdType().getName() : null,
                "replaceOption82", setReplaceOption82(config));
    }

    private String setEnable(Config config) {
        if (config.isEnable() != null) {
            return config.isEnable() ? "enable" : "disable";
        }
        return null;
    }

    private String setReplaceOption82(Config config) {
        if (config.isReplaceOption82() != null) {
            return config.isReplaceOption82() ? "on" : "off";
        }
        return null;
    }
}