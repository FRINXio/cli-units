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

package io.frinx.cli.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpNeighborState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborStateReader implements BgpReader.BgpOperReader<State, StateBuilder> {

    private Cli cli;

    static final String SH_SUMM = "show bgp all summary | begin Neighbor";
    private static final String NEIGHBOR_LINE = "%s (?<ver>.+) (?<as>.+) (?<msgRcvd>.+) (?<msgSent>.+) (?<tblVer>.+) (?<inQ>.+) (?<outQ>.+) (?<time>.+) (?<pfxRcd>.+)";

    public NeighborStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                             @Nonnull StateBuilder stateBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        String neighborIp = NeighborWriter.getNeighborIp(instanceIdentifier);
        String output = blockingRead(SH_SUMM, cli, instanceIdentifier, ctx);
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
