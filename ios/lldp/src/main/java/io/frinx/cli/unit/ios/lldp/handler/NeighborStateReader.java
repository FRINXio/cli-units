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

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
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

    private static final String SHOW_LLDP_NEIGHBOR_DETAIL = "sh lldp neighbor %s detail | include Port id|System Name";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile(".*Port id: (?<portId>.+) System Name:.*");

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
        String showLldpEntryCommand = String.format(SHOW_LLDP_NEIGHBOR_DETAIL, interfaceId);

        parseNeighborStateFields(
                blockingRead(showLldpEntryCommand, cli, instanceIdentifier, readContext), neighborId, stateBuilder);

    }

    // TODO document this method
    @VisibleForTesting
    static void parseNeighborStateFields(String output, String neighborId, StateBuilder builder) {
        String withoutNewLines = output.replaceAll(NEWLINE.pattern(), " ");

        String linePerNeighborOutput = withoutNewLines.replaceAll("Port id:", "\nPort id:");

        NEWLINE.splitAsStream(linePerNeighborOutput)
                .map(String::trim)
                .filter(s -> s.endsWith(neighborId))
                .map(NEIGHBOR_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("portId"))
                .findFirst()
                .ifPresent(builder::setPortId);

        builder.setId(neighborId);
        // TODO set also other fields
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State state) {
        ((NeighborBuilder) builder).setState(state);
    }
}
