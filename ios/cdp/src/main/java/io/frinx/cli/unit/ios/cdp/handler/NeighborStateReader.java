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

package io.frinx.cli.unit.ios.cdp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborStateReader implements CliOperReader<State, StateBuilder> {

    private Cli cli;

    public NeighborStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String neighborId = instanceIdentifier.firstKeyOf(Neighbor.class).getId();

        String output = blockingRead(String.format(NeighborReader.SH_CDP_NEIGH, interfaceId), cli,
                instanceIdentifier, readContext);
        parseNeighborStateFields(stateBuilder, output, neighborId);
    }

    // Relying on at least 3 spaces to appear between columns
    private static final Pattern CDP_NEIGH_PORT_LINE = Pattern.compile(".*Port ID \\(outgoing port\\): (?<remotePort>"
            + ".+).*", Pattern.DOTALL);

    @VisibleForTesting
    static void parseNeighborStateFields(StateBuilder stateBuilder, String output, String neighborId) {
        // The output needs to be preprocessed, so put everything on 1 line
        String withoutNewlines = output.replaceAll(ParsingUtils.NEWLINE.pattern(), " ");
        // And then split per neighbor
        String linePerNeighborOutput = withoutNewlines.replace("Device ID: ", "\n");

        ParsingUtils.NEWLINE.splitAsStream(linePerNeighborOutput)
                .map(String::trim)
                .filter(s -> s.startsWith(neighborId))
                .map(CDP_NEIGH_PORT_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("remotePort"))
                .findFirst()
                .ifPresent(stateBuilder::setPortId);

        stateBuilder.setId(neighborId);

        // TODO set others
    }
}