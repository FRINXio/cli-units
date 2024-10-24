/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.junos.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigInteger;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MaxMetricConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_RUN_OSPF = "show configuration protocols ospf overload timeout";
    private static final Pattern MAX_METRIC = Pattern.compile("(?<inactive>.*\\s)?timeout (?<value>.*);");

    private Cli cli;

    public MaxMetricConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_RUN_OSPF, cli, instanceIdentifier, readContext);
        parseMaxMetric(output, configBuilder);
    }

    @VisibleForTesting
    static void parseMaxMetric(String output, ConfigBuilder configBuilder) {
        if (!output.isEmpty()) {
            configBuilder.setSet(true);

            ParsingUtils.parseField(output,
                MAX_METRIC::matcher,
                matcher -> matcher.group("inactive"),
                setter -> configBuilder.setSet(false));

            ParsingUtils.parseField(output,
                MAX_METRIC::matcher,
                matcher -> matcher.group("value"),
                value -> configBuilder.setTimeout(new BigInteger(value)));
        }
    }
}