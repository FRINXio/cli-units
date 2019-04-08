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

package io.frinx.cli.ios.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalStateReader implements CliOperReader<State, StateBuilder> {

    private Cli cli;
    static final String SH_BGP = "show bgp summary";
    static final Pattern CONFIG_LINE = Pattern.compile("BGP router identifier (?<id>.+), local AS number (?<as>.+)");

    public GlobalStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                             @Nonnull StateBuilder stateBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        parseGlobal(blockingRead(SH_BGP, cli, instanceIdentifier, ctx), stateBuilder);
    }

    @VisibleForTesting
    public static void parseGlobal(String output, StateBuilder stateBuilder) {
        ParsingUtils.parseField(output, 0,
                CONFIG_LINE::matcher,
            matcher -> matcher.group("id"),
            value -> stateBuilder.setRouterId(new DottedQuad(value)));

        ParsingUtils.parseField(output, 0,
                CONFIG_LINE::matcher,
            matcher -> matcher.group("as"),
            value -> stateBuilder.setAs(new AsNumber(Long.valueOf(value))));
    }
}
