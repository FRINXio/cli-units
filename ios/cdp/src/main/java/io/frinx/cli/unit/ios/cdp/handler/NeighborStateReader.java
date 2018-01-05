/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.cdp.handler;

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

    private Cli cli;

    public NeighborStateReader(Cli cli) {
        this.cli = cli;
    }

    // Cannot use "sh cdp neighbors" here, The output table lacs any form to rely on and thus cannot be parsed
    private static final String SH_CDP_INTER_NEIGHBOR = "sh cdp neighbors %s detail | include Device ID|Interface";

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder stateBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String neighborId = instanceIdentifier.firstKeyOf(Neighbor.class).getId();

        String output = blockingRead(String.format(SH_CDP_INTER_NEIGHBOR, interfaceId), cli, instanceIdentifier, readContext);
        parseNeighborStateFields(stateBuilder, output, neighborId);
    }

    // Relying on at least 3 spaces to appear between columns
    private static final Pattern CDP_NEIGH_PORT_LINE = Pattern.compile(".*Port ID \\(outgoing port\\): (?<remotePort>.+).*", Pattern.DOTALL);

    @VisibleForTesting
    static void parseNeighborStateFields(StateBuilder stateBuilder, String output, String neighborId) {
        // The output needs to be preprocessed, so put everything on 1 line
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), " ");
        // And then split per neighbor
        String linePerNeighborOutput = withoutNewlines.replace("Device ID: ", "\n");

        NEWLINE.splitAsStream(linePerNeighborOutput)
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

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull State state) {
        ((NeighborBuilder) builder).setState(state);
    }
}




