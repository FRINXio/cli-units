/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.evpn.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evpn.rev181112.evpn.top.evpn.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evpn.rev181112.evpn.top.evpn.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EvpnConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public static final String SH_EVPN =
            "show running-config evpn | utility egrep '^ cost\\-out|^ startup\\-cost\\-in'";
    private static final String COST_OUT = "cost-out";
    private static final Pattern START_COST_IN = Pattern.compile("startup\\-cost\\-in (?<value>[0-9]+)");

    public EvpnConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> iid,
            @NotNull ConfigBuilder builder, @NotNull ReadContext context) throws ReadFailedException {
        String output = blockingRead(SH_EVPN, cli, iid, context);
        builder.setCostOut(output.contains(COST_OUT));
        ParsingUtils.parseFields(output, 0, START_COST_IN::matcher, matcher -> matcher.group("value"),
            value -> builder.setStartupCostIn(Long.valueOf(value)));
    }
}