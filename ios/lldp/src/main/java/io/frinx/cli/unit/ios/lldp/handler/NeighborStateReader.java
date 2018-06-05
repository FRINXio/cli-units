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

package io.frinx.cli.unit.ios.lldp.handler;

import static io.frinx.cli.unit.ios.lldp.handler.NeighborReader.KEY_FORMAT;
import static io.frinx.cli.unit.ios.lldp.handler.NeighborReader.SHOW_LLDP_NEIGHBOR;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborStateReader implements CliOperReader<State, StateBuilder> {

    private static final Pattern PORT_DESC = Pattern.compile("Port Description: (?<portDescr>.+)");
    private static final Pattern NAME = Pattern.compile("System Name: (?<name>\\S+)");
    private static final Pattern DESCR = Pattern.compile("System Description:\\s*\\r?\\n(?<descr>.+?)\\r?\\n\\r?\\n", Pattern.DOTALL);
    private static final Pattern IP = Pattern.compile("(IP|IPv4 address): (?<ip>\\S+)");
    private static final Pattern IPV6 = Pattern.compile("(IPV6|IPv6 address): (?<ip>\\S+)");
    private static final Pattern SEPARATOR = Pattern.compile("----------");

    private Cli cli;

    public NeighborStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder stateBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String neighborId = instanceIdentifier.firstKeyOf(Neighbor.class).getId();
        String cmd = String.format(SHOW_LLDP_NEIGHBOR, interfaceId);
        String output = blockingRead(cmd, cli, instanceIdentifier, readContext);

        parseNeighborStateFields(extractSingleNeighbor(output, neighborId), neighborId, stateBuilder);
    }

    @VisibleForTesting
    static String extractSingleNeighbor(String output, String neighborId) {
        String[] split = splitId(neighborId);
        return SEPARATOR.splitAsStream(output)
                .filter(chunk -> chunk.contains(split[0]) && chunk.contains(split[1]))
                .findFirst()
                .orElse("");
    }

    @VisibleForTesting
    static void parseNeighborStateFields(String output, String neighborId, StateBuilder builder) {
        String[] split = splitId(neighborId);

        builder.setId(neighborId);
        builder.setChassisId(split[0]);
        builder.setPortId(split[1]);

        // Optional TLVs
        parseField(output, PORT_DESC::matcher, m -> m.group("portDescr"), builder::setPortDescription);
        parseField(output, NAME::matcher, m -> m.group("name"), builder::setSystemName);

        Matcher descrMatcher = DESCR.matcher(output);
        if (descrMatcher.find()) {
            String descr = descrMatcher.group(1);
            builder.setSystemDescription(descr.length() >= 255 ? descr.substring(0, 254): descr);
        }

        Optional<String> mgmtIp4 = parseField(output, 0, IP::matcher, m -> m.group("ip"));
        String mgmtIp = mgmtIp4
                .orElse(parseField(output, 0, IPV6::matcher, m -> m.group("ip")).orElse(null));
        if (mgmtIp != null) {
            builder.setManagementAddress(mgmtIp);
        }
    }

    private static String[] splitId(String neighborId) {
        String[] split = neighborId.split(" Port:");
        Preconditions.checkArgument(split.length == 2,
                "Invalid neighbor id format, expected: %s", KEY_FORMAT);
        return split;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State state) {
        ((NeighborBuilder) builder).setState(state);
    }
}
