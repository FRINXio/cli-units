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

package io.frinx.cli.unit.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpNeighborState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborStateReader implements CliOperReader<State, StateBuilder> {

    private Cli cli;

    static final String SH_SUMM = "show bgp all summary | begin Neighbor";
    private static final String NEIGHBOR_LINE = "%s (?<ver>.+) (?<as>.+) (?<msgRcvd>.+) (?<msgSent>.+) (?<tblVer>.+) "
            + "(?<inQ>.+) (?<outQ>.+) (?<time>.+) (?<pfxRcd>.+)";

    public NeighborStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                             @NotNull StateBuilder stateBuilder,
                                             @NotNull ReadContext ctx) throws ReadFailedException {
        String neighborIp = NeighborWriter.getNeighborIp(instanceIdentifier);
        String output = blockingRead(SH_SUMM, cli, instanceIdentifier, ctx);
        readState(neighborIp, stateBuilder, output);
    }

    @VisibleForTesting
    public static void readState(String neighborIp, StateBuilder stateBuilder, String output) {
        String state = parseState(output, neighborIp);
        if (!StringUtils.isNumeric(state)) {
            stateBuilder.setSessionState(BgpNeighborState.SessionState.valueOf(state.toUpperCase(Locale.ROOT)));
        }
    }

    static String parseState(String output, String neighborIp) {
        // State/PfxRcd can be either Idle or number depending on whether the connection is/was established and
        // prefixes were received
        // parse first a string, then decide if it's status or number of prefixes received
        return ParsingUtils.parseField(output.replaceAll("\\h+", " "), 0,
                Pattern.compile(String.format(NEIGHBOR_LINE, neighborIp))::matcher,
            matcher -> matcher.group("pfxRcd")).orElse("");
    }
}