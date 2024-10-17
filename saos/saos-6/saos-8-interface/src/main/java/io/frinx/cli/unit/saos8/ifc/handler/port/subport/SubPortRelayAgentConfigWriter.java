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
package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortRelayAgentConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_RELAY_AGENT = """
            {% if ($cidStringValue) %}dhcp l2-relay-agent set sub-port {$subPortName} vs {$virtualSwitchName} cid-string {$cidStringValue}
            {% endif %}{% if ($trustMode) %}dhcp l2-relay-agent set sub-port {$subPortName} vs {$virtualSwitchName} trust-mode {$trustMode}
            {% endif %}""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String DELETE_RELAY_AGENT = """
            {% if ($cidStringValue) %}dhcp l2-relay-agent unset sub-port {$subPortName} vs {$virtualSwitchName} cid-string
            {% endif %}{% if ($trustMode) %}dhcp l2-relay-agent unset sub-port {$subPortName} vs {$virtualSwitchName} trust-mode
            {% endif %}""";

    private final Cli cli;

    public SubPortRelayAgentConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext
                    .readAfter(instanceIdentifier.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final var subPortName = subPort.get().getConfig().getName();

                blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, subPortName));
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String subPortName) {
        return fT(WRITE_RELAY_AGENT, "data", config,
                "subPortName", subPortName,
                "virtualSwitchName", config.getVirtualSwitchName(),
                "cidStringValue", config.getCidString(),
                "trustMode", config.getTrustMode() != null ? config.getTrustMode().getName() : null);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext
                    .readAfter(instanceIdentifier.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final var subPortName = subPort.get().getConfig().getName();
                blockingDelete(deleteTemplate(config, subPortName), cli, instanceIdentifier);
            }
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String subPortName) {
        return fT(DELETE_RELAY_AGENT, "data", config,
                "subPortName", subPortName,
                "virtualSwitchName", config.getVirtualSwitchName(),
                "cidStringValue", config.getCidString(),
                "trustMode", config.getTrustMode() != null ? config.getTrustMode().getName() : null);
    }

}