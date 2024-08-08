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
package io.frinx.cli.unit.saos8.relay.agent.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SHOW_COMMAND = "configuration search string \"dhcp l2-relay-agent\"";

    private final Cli cli;

    public RelayAgentConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        parseRelayAgentConfig(output, configBuilder);
    }

    @VisibleForTesting
    static void parseRelayAgentConfig(String output, ConfigBuilder builder) {
        setEnable(output, builder);
        setCircuitIdType(output, builder);
        setRemoteIdType(output, builder);
    }

    private static void setEnable(String output, ConfigBuilder builder) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent enable");

        builder.setEnable(false);
        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> true,
            builder::setEnable);
    }

    private static void setCircuitIdType(String output, ConfigBuilder builder) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent set circuit-id-type (?<circuitIdType>\\S+).*");

        Optional<String> circuitIdTypeValue = ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("circuitIdType"));

        if (circuitIdTypeValue.isPresent()) {
            Config.CircuitIdType circuitIdType = switch (circuitIdTypeValue.get()) {
                case "cid-string" -> Config.CircuitIdType.CidString;
                case "li-vs" -> Config.CircuitIdType.LiVs;
                default -> throw new IllegalArgumentException(
                        "Cannot parse circuit id type value: " + circuitIdTypeValue.get());
            };
            builder.setCircuitIdType(circuitIdType);
        }
    }

    private static void setRemoteIdType(String output, ConfigBuilder builder) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent set remote-id-type (?<remoteIdType>\\S+).*");

        Optional<String> remoteIdTypeValue = ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("remoteIdType"));

        if (remoteIdTypeValue.isPresent()) {
            Config.RemoteIdType remoteIdType = switch (remoteIdTypeValue.get()) {
                case "rid-string" -> Config.RemoteIdType.RidString;
                case "device-hostname" -> Config.RemoteIdType.DeviceHostname;
                case "device-mac" -> Config.RemoteIdType.DeviceMac;
                default ->
                        throw new IllegalArgumentException("Cannot parse remote id type value: "
                                + remoteIdTypeValue.get());
            };
            builder.setRemoteIdType(remoteIdType);
        }
    }
}