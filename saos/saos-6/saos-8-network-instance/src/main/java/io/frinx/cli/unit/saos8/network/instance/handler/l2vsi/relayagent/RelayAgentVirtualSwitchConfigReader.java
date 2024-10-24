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
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev220626.saos.ra.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev220626.saos.ra.extension.relay.agent.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentVirtualSwitchConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SHOW_COMMAND = "configuration search string \"dhcp l2-relay-agent\"";

    private final Cli cli;

    public RelayAgentVirtualSwitchConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String virtualSwitchName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        parseRelayAgentVSConfig(output, configBuilder, virtualSwitchName);
    }

    @VisibleForTesting
    static void parseRelayAgentVSConfig(String output, ConfigBuilder builder, String virtualSwitchName) {
        setEnable(output, builder, virtualSwitchName);
    }

    private static void setEnable(String output, ConfigBuilder builder, String virtualSwitchName) {
        Pattern pattern = Pattern.compile("dhcp l2-relay-agent enable vs " + virtualSwitchName);

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> true,
            builder::setEnable);
    }
}