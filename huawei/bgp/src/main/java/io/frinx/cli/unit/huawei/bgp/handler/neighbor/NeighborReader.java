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
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements CliConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    private static final String DISPLAY_PEER_CONFIG =
            "display current-configuration configuration bgp | include |^ ipv4-family vpn instance|^* peer";
    private static final Pattern PEER_LINE = Pattern.compile("peer (?<neighborIp>\\S*) as-number .*");
    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override public List<NeighborKey> getAllIds(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
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


        Optional<String> optionalVrfOutput =
            Arrays.stream(getSplitedOutput(output)).filter(value -> value.contains(vrfName)).findFirst();

        if (optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll(" peer", "\n peer"), 0,
                PEER_LINE::matcher,
                matcher -> matcher.group("neighborIp"),
                value -> new NeighborKey(new IpAddress(new Ipv4Address(value))));
        }

        return Lists.newArrayList();
    }

    @Override public void readCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                                       @Nonnull NeighborBuilder neighborBuilder,
                                                       @Nonnull ReadContext readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
    }

    @VisibleForTesting
    public static List<NeighborKey> getDefaultNeighborKeys(String output) {
        Optional<String> optionalVrfOutput =
            Arrays.stream(getSplitedOutput(output)).filter(value -> !value.contains("vpn-instance")).findFirst();

        if (optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll(" peer", "\n peer"), 0,
                PEER_LINE::matcher,
                matcher -> matcher.group("neighborIp"),
                value -> new NeighborKey(new IpAddress(new Ipv4Address(value))));
        }

        return Lists.newArrayList();
    }

    public static String[] getSplitedOutput(String output) {
        return output.replaceAll(Cli.NEWLINE, "").replaceAll("\r", "")
                .replaceAll(" ipv4-family", "\n ipv4-family")
                .split("\\n");
    }
}
