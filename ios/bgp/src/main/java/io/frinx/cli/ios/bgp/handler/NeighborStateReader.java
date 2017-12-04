/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpNeighborState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class NeighborStateReader implements BgpReader.BgpOperReader<State, StateBuilder> {

    private Cli cli;

    private static final String NEIGHBOR_LINE = "%s (?<ver>.+) (?<as>.+) (?<msgRcvd>.+) (?<msgSent>.+) (?<tblVer>.+) (?<inQ>.+) (?<outQ>.+) (?<time>.+) (?<pfxRcd>.+)";

    public NeighborStateReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public StateBuilder getBuilder(@Nonnull InstanceIdentifier<State> instanceIdentifier) {
        return new StateBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                             @Nonnull StateBuilder stateBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        String neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress().getIpv4Address().getValue();
        String output = blockingRead(NeighborReader.SH_SUMM, cli, instanceIdentifier, ctx);
        readState(neighborIp,stateBuilder, output);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State config) {
        ((NeighborBuilder) builder).setState(config);
    }

    @VisibleForTesting
    public static void readState(String neighborIp, StateBuilder stateBuilder, String output) {
        String state = parseState(output, neighborIp);
        if (!StringUtils.isNumeric(state)) {
            stateBuilder.setSessionState(BgpNeighborState.SessionState.valueOf(state.toUpperCase()));
        }
    }

    static String parseState(String output, String neighborIp) {
        // State/PfxRcd can be either Idle or number depending on whether the connection is/was established and prefixes were received
        // parse first a string, then decide if it's status or number of prefixes received
        return ParsingUtils.parseField(output.replaceAll("\\h+", " "), 0,
                Pattern.compile(String.format(NEIGHBOR_LINE, neighborIp))::matcher,
                matcher -> matcher.group("pfxRcd")).orElse("");
    }
}
