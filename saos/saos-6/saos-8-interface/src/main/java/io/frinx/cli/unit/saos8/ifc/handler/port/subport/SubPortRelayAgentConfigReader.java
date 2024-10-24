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
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortRelayAgentConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SHOW_COMMAND = "configuration search string \"dhcp l2-relay-agent set sub-port\"";

    private final Cli cli;

    public SubPortRelayAgentConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            var subPortName = findSubInterfaceNameInCache(instanceIdentifier.firstKeyOf(Subinterface.class),
                    readContext);
            var output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            parseRelayAgentConfig(output, configBuilder, subPortName);
        }
    }

    @VisibleForTesting
    static void parseRelayAgentConfig(String output, ConfigBuilder builder, String subPortName) {
        setVirtualSwitch(output, builder, subPortName);
        setCidString(output, builder, subPortName);
        setTrustMode(output, builder, subPortName);
    }

    private static void setVirtualSwitch(String output, ConfigBuilder builder, String subPortName) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent set sub-port " + subPortName
                + " vs (?<virtualSwitchName>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("virtualSwitchName"),
            builder::setVirtualSwitchName);
    }

    private static void setCidString(String output, ConfigBuilder builder, String subPortName) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent set sub-port " + subPortName
                + " .*cid-string (?<cidString>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("cidString"),
            builder::setCidString);
    }

    private static void setTrustMode(String output, ConfigBuilder builder, String subPortName) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent set sub-port " + subPortName
                + " .*trust-mode (?<trustMode>\\S+).*");

        Optional<String> trustModeValue = ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("trustMode"));

        if (trustModeValue.isPresent()) {
            Config.TrustMode trustMode;
            switch (trustModeValue.get()) {
                case "client-trusted":
                    trustMode = Config.TrustMode.ClientTrusted;
                    break;
                case "server-trusted":
                    trustMode = Config.TrustMode.ServerTrusted;
                    break;
                case "dualrole-trusted":
                    trustMode = Config.TrustMode.DualroleTrusted;
                    break;
                case "untrusted":
                    trustMode = Config.TrustMode.Untrusted;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse trust mode value: " + trustModeValue.get());
            }
            builder.setTrustMode(trustMode);
        }
    }

    private static String findSubInterfaceNameInCache(SubinterfaceKey subinterfaceKey,
                                                      @NotNull ReadContext readContext) {
        return SubPortReader.findConfigInCache(subinterfaceKey, readContext).getName();
    }

    private boolean isPort(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}