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
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.config.Ports;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.config.PortsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.config.PortsKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RelayAgentPortReader implements CliConfigListReader<Ports, PortsKey, PortsBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"dhcp l2-relay-agent\"";

    private Cli cli;

    public RelayAgentPortReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<PortsKey> getAllIds(@NotNull InstanceIdentifier<Ports> instanceIdentifier,
                                    @NotNull ReadContext readContext) throws ReadFailedException {
        var vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId().getValue().toString();
        var output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        return getAllIds(output, vlanId);
    }

    @VisibleForTesting
    static List<PortsKey> getAllIds(String output, String vlanId) {
        var portPattern = Pattern.compile("dhcp l2-relay-agent set vlan " + vlanId
                + " port (?<portName>\\S+).*");
        return ParsingUtils.parseFields(output, 0,
            portPattern::matcher,
            matcher -> matcher.group("portName"),
            PortsKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Ports> instanceIdentifier,
                                      @NotNull PortsBuilder portsBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        portsBuilder.setKey(instanceIdentifier.firstKeyOf(Ports.class));
    }
}