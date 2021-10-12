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

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements CliConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    private static final String DISPLAY_PEER_CONFIG =
            "display current-configuration configuration bgp | include |^ ipv4-family vpn instance|^* peer";
    private static final Pattern PEER_LINE = Pattern.compile("peer (?<neighborIp>\\S*) as-number .*");
    private static final Pattern BASE_PATTERN = Pattern.compile("\\n\\s#\n");
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\n");
    private final Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<NeighborKey> getAllIds(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                       @Nonnull ReadContext readContext) throws ReadFailedException {

        String networkInstanceName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(networkInstanceName)) {
            return getDefaultNeighborKeys(blockingRead(DISPLAY_PEER_CONFIG, cli, instanceIdentifier, readContext));
        } else {
            return getVrfNeighborKeys(blockingRead(DISPLAY_PEER_CONFIG, cli, instanceIdentifier, readContext),
                    networkInstanceName);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list).build();
    }

    @VisibleForTesting
    public static List<NeighborKey> getVrfNeighborKeys(String output, String vrfName) {
        List<NeighborKey> keys = parsingKeys(output,
            line -> line.contains("ipv4-family vpn-instance " + vrfName + "\n"),
            value -> new NeighborKey(new IpAddress(new Ipv4Address(value))));

        keys.addAll(parsingKeys(output, line -> line.contains("ipv6-family vpn-instance " + vrfName + "\n"),
            value -> new NeighborKey(new IpAddress(new Ipv6Address(value)))));
        return keys;
    }

    @VisibleForTesting
    public static List<NeighborKey> getDefaultNeighborKeys(String output) {
        return parsingKeys(output, value -> !value.contains("vpn-instance"),
            value -> new NeighborKey(new IpAddress(new Ipv4Address(value))));
    }

    private static List<NeighborKey> parsingKeys(String output, Predicate<String> filter,
                                                 Function<String, NeighborKey> mapper) {
        return BASE_PATTERN.splitAsStream(output)
                .filter(filter)
                .flatMap(NEW_LINE_PATTERN::splitAsStream)
                .map(String::trim)
                .map(PEER_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("neighborIp"))
                .map(mapper)
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                      @Nonnull NeighborBuilder neighborBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
    }
}
