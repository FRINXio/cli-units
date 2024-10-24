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

package io.frinx.cli.unit.nexus.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceStatisticsConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public InterfaceStatisticsConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();
        String showRunIfcConfig = String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName);

        parseLoadInterval(blockingRead(showRunIfcConfig, cli, id, ctx), builder);
    }

    private static final Pattern LOAD_INTERVAL = Pattern.compile("\\s*load-interval counter 1 (?<loadInterval>\\d+)"
            + "\\s*");

    @VisibleForTesting
    static void parseLoadInterval(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0,
            LOAD_INTERVAL::matcher,
            matcher -> Long.valueOf(matcher.group("loadInterval")),
            builder::setLoadInterval);
    }
}