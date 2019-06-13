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

package io.frinx.cli.unit.huawei.bgp.handler.local.aggregates;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.Aggregate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpLocalAggregateReader implements CliConfigListReader<Aggregate, AggregateKey,
        AggregateBuilder> {

    private static final String GROUP_IP = "ip";
    private static final String GROUP_MASK = "mask";
    private static final String DISPLAY_BGP_NETWORK_CONFIG = "display current-configuration configuration bgp | "
            + "include ^ ipv4-family|^ *network";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("network (?<ip>\\S*) (?<mask>\\S*).*");
    private final Cli cli;

    public BgpLocalAggregateReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AggregateKey> getAllIds(@Nonnull InstanceIdentifier<Aggregate> instanceIdentifier, @Nonnull
            ReadContext readContext) throws ReadFailedException {
        String niName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        if (BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(niName)) {
            return getDefaultAggregateKeys(blockingRead(DISPLAY_BGP_NETWORK_CONFIG, cli, instanceIdentifier,
                    readContext));
        } else {
            return getVrfAggregateKeys(blockingRead(DISPLAY_BGP_NETWORK_CONFIG, cli, instanceIdentifier, readContext),
                    niName);
        }
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Aggregate> instanceIdentifier, @Nonnull
            AggregateBuilder aggregateBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        aggregateBuilder.setKey(instanceIdentifier.firstKeyOf(Aggregate.class));
    }

    @VisibleForTesting
    static List<AggregateKey> getVrfAggregateKeys(String output, String vrfName) {
        Optional<String> optionalVrfOutput = Arrays.stream(NeighborReader.getSplitedOutput(output)).filter(value ->
                value.contains(vrfName)).reduce((s1, s2) -> s1
                + s2);

        if (optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll("network", "\nnetwork"), 0,
                    NEIGHBOR_LINE::matcher, BgpLocalAggregateReader::resolveGroups, value -> new AggregateKey(
                    parsePrefix(parseNetworkPrefix(value))));
        }
        return new ArrayList<>();
    }

    private static IpPrefix parsePrefix(String string) {
        return new IpPrefix(string.toCharArray());
    }

    @VisibleForTesting
    static List<AggregateKey> getDefaultAggregateKeys(String output) {
        Optional<String> optionalVrfOutput = Arrays.stream(NeighborReader.getSplitedOutput(output)).filter(value ->
                !value.contains("vpn-instance")).reduce((s1, s2) -> s1
                + s2);

        if (optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll("network", "\nnetwork"), 0,
                    NEIGHBOR_LINE::matcher, BgpLocalAggregateReader::resolveGroups, value -> new AggregateKey(
                    parsePrefix(parseNetworkPrefix(value))));
        }
        return new ArrayList<>();
    }

    private static String parseNetworkPrefix(HashMap<String, String> params) {
        SubnetUtils utils = new SubnetUtils(params.get(GROUP_IP), params.get(GROUP_MASK));
        return utils.getInfo().getCidrSignature();
    }

    private static HashMap<String, String> resolveGroups(Matcher matcher) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(GROUP_IP, matcher.group(GROUP_IP));
        hashMap.put(GROUP_MASK, matcher.group(GROUP_MASK));

        return hashMap;
    }
}
