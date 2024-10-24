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

package io.frinx.cli.unit.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.timers.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.timers.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborTimersConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern TIMERS_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) timers (?<timer1>\\S+) (?<timer2>\\S+)"
                    + "(\\s(?<timer3>\\S+))?");

    private final Cli cli;

    public NeighborTimersConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborWriter.getNeighborIp(instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress());

        String output = blockingRead(f(NeighborConfigReader.SH_SUMM, ipAddress), cli, instanceIdentifier, readContext);

        parseConfig(output, configBuilder, vrfName);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder, String vrfName) {
        if (!NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            String[] vrfSplit = NeighborReader.splitOutput(output);
            parseTimers(configBuilder, vrfName, vrfSplit);
        }
    }

    private static void parseTimers(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
                Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();

        if (optionalVrfOutput.isPresent()) {
            String defaultInstance = optionalVrfOutput.get();
            setKeepalive(configBuilder, defaultInstance);
            setHoldTime(configBuilder, defaultInstance);
            setMinHoldTime(configBuilder, defaultInstance);
        }
    }

    private static void setKeepalive(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, TIMERS_PATTERN::matcher,
            matcher -> matcher.group("timer1"),
            v -> configBuilder.setKeepaliveInterval(new BigDecimal(v)));
    }

    private static void setHoldTime(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, TIMERS_PATTERN::matcher,
            matcher -> matcher.group("timer2"),
            v -> configBuilder.setHoldTime(new BigDecimal(v)));
    }

    private static void setMinHoldTime(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseField(preprocessOutput(defaultInstance), 0, TIMERS_PATTERN::matcher,
            matcher -> matcher.group("timer3"),
            v -> configBuilder.setMinimumAdvertisementInterval(new BigDecimal(v)));
    }

    private static String preprocessOutput(String defaultInstance) {
        return defaultInstance
            .replaceAll(" neighbor", "\n neighbor")
            .replaceAll(" address-family", "\n address-family");
    }

}