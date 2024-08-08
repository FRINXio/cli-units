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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern NEIGHBOR_POLICY_IN_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) route-map (?<updateSource>\\S*) in.*");
    private static final Pattern NEIGHBOR_POLICY_OUT_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) route-map (?<updateSource>\\S*) out.*");

    private final Cli cli;

    public NeighborPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                             @NotNull ConfigBuilder configBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborWriter.getNeighborIp(instanceIdentifier);

        String output = blockingRead(String.format(NeighborConfigReader.SH_SUMM, ipAddress), cli, instanceIdentifier,
                readContext);
        String[] outputLines = NeighborReader.splitOutput(output);
        Stream<String> outputStream = Arrays.stream(outputLines)
                .filter(line -> !line.contains("address-family"));

        parseConfigAttributes(configBuilder, vrfName, outputStream);
    }

    @VisibleForTesting
    public static void parseConfigAttributes(ConfigBuilder configBuilder, String vrfName, Stream<String> vrfSplit) {
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            parseDefault(configBuilder, vrfSplit);
        } else {
            parseVrf(configBuilder, vrfName, vrfSplit);
        }
    }

    private static void parseDefault(ConfigBuilder configBuilder, Stream<String> output) {
        Optional<String> defaultNetworkNeighbors = output
                .filter(value -> !value.contains("vrf"))
                .reduce((s1, s2) -> s1 + s2);

        setAttributes(configBuilder, defaultNetworkNeighbors.orElse(""));
    }

    private static void setAttributes(ConfigBuilder configBuilder, String output) {
        setPolicies(configBuilder, output);
    }

    private static void setPolicies(ConfigBuilder configBuilder, String defaultInstance) {
        String processed = defaultInstance.replaceAll(" neighbor", "\n neighbor");

        List<String> inPolicies = ParsingUtils.parseFields(processed, 0, NEIGHBOR_POLICY_IN_PATTERN::matcher,
            m -> m.group("updateSource"),
            Function.identity());

        if (!inPolicies.isEmpty()) {
            configBuilder.setImportPolicy(inPolicies);
        }

        List<String> outPolicies = ParsingUtils.parseFields(processed, 0, NEIGHBOR_POLICY_OUT_PATTERN::matcher,
            m -> m.group("updateSource"),
            Function.identity());

        if (!outPolicies.isEmpty()) {
            configBuilder.setExportPolicy(outPolicies);
        }
    }

    private static void parseVrf(ConfigBuilder configBuilder, String vrfName, Stream<String> output) {
        Optional<String> optionalVrfOutput = output
                .filter(value -> value.contains(vrfName))
                .findFirst();

        setAttributes(configBuilder, optionalVrfOutput.orElse(""));
    }
}