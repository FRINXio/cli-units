/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private Cli cli;

    public OspfProtocolReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_RUN_INCLUDE_OSPF = "show running-config | include ospf";
    private static final Pattern OSPF_NO_VRF = Pattern.compile("\\s*router ospf (?<id>[^\\s]+)\\s*");
    private static final Pattern OSPF_VRF = Pattern.compile("\\s*router ospf (?<id>[^\\s]+) vrf (?<vrf>[^\\s]+)\\s*");

    @NotNull
    @Override
    public List<ProtocolKey> getAllIds(@NotNull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @NotNull ReadContext readContext)
            throws ReadFailedException {
        String output = blockingRead(SH_RUN_INCLUDE_OSPF, cli, instanceIdentifier, readContext);
        String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        if (vrfId.equals(NetworInstance.DEFAULT_NETWORK_NAME)) {
            return ParsingUtils.parseFields(output, 0,
                    OSPF_NO_VRF::matcher,
                matcher -> matcher.group("id"),
                s -> new ProtocolKey(OSPF.class, s));
        } else {
            return ParsingUtils.NEWLINE.splitAsStream(output)
                    .map(String::trim)
                    // Only include those from VRF
                    .filter(s -> s.contains("vrf " + vrfId))
                    .map(OSPF_VRF::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group("id"))
                    .map(s -> new ProtocolKey(OSPF.class, s))
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Protocol> instanceIdentifier,
                                             @NotNull ProtocolBuilder protocolBuilder,
                                             @NotNull ReadContext readContext) {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        protocolBuilder.setName(key.getName());
        protocolBuilder.setIdentifier(key.getIdentifier());
    }

    @Override
    public Check getCheck() {
        return ChecksMap.PathCheck.Protocol.OSPF;
    }
}