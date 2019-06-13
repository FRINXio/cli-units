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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborTransportConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern NEIGHBOR_UPDATE_SOURCE_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) update-source (?<updateSource>\\S*).*");
    private static final Pattern NEIGHBOR_PASSIVE =
            Pattern.compile("neighbor (?<neighborIp>\\S*) transport connection-mode passive.*");

    private final Cli cli;

    public NeighborTransportConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborWriter.getNeighborIp(instanceIdentifier);

        parseConfigAttributes(blockingRead(String.format(NeighborConfigReader.SH_SUMM, ipAddress), cli,
                instanceIdentifier, readContext),
                configBuilder, vrfName);
    }

    @VisibleForTesting
    public static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String vrfName) {

        String[] vrfSplit = NeighborReader.splitOutput(output);

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            parseDefault(configBuilder, vrfSplit);
        } else {
            parseVrf(configBuilder, vrfName, vrfSplit);
        }
    }

    private static void parseDefault(ConfigBuilder configBuilder, String[] output) {
        Optional<String> defaultNetworkNeighbors = Arrays.stream(output)
                .filter(value -> !value.contains("vrf"))
                .reduce((s1, s2) -> s1 + s2);

        setAttributes(configBuilder, defaultNetworkNeighbors.orElse(""));
    }

    private static void setAttributes(ConfigBuilder configBuilder, String output) {
        setUpdateSource(configBuilder, output);
        setPassive(configBuilder, output);
    }

    private static void setUpdateSource(ConfigBuilder configBuilder, String defaultInstance) {
        String processed = defaultInstance.replaceAll(" neighbor", "\n neighbor");

        ParsingUtils.parseField(processed, 0, NEIGHBOR_UPDATE_SOURCE_PATTERN::matcher,
            m -> m.group("updateSource"),
            updateSource -> configBuilder.setLocalAddress(parseLocalAddress(updateSource)));
    }

    private static void setPassive(ConfigBuilder configBuilder, String defaultInstance) {
        // Default
        configBuilder.setPassiveMode(false);

        String processed = defaultInstance.replaceAll(" neighbor", "\n neighbor");
        ParsingUtils.parseField(processed, 0, NEIGHBOR_PASSIVE::matcher,
            m -> true,
            configBuilder::setPassiveMode);
    }

    private static BgpCommonNeighborGroupTransportConfig.LocalAddress parseLocalAddress(String updateSource) {
        return new BgpCommonNeighborGroupTransportConfig.LocalAddress(updateSource);
    }

    private static void parseVrf(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
                Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();

        setAttributes(configBuilder, optionalVrfOutput.orElse(""));
    }
}
