/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements CliConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    public static final String SH_SUMM = "show running-config | include ^router bgp|^ address-family|^ *neighbor";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("neighbor (?<id>[0-9A-F.:]*) (remote-as|peer-group) "
            + "\\S+");
    private static final Function<String, NeighborKey> TO_NEIGH_KEY = (String value) -> new NeighborKey(new IpAddress(
            value.toCharArray()));

    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<NeighborKey> getAllIds(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {

        String networkInstanceName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(networkInstanceName)) {
            return getDefaultNeighborKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext));
        } else {
            return getVrfNeighborKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext),
                    networkInstanceName);
        }
    }

    @VisibleForTesting
    public static List<NeighborKey> getVrfNeighborKeys(String output, String vrfName) {
        return parseKeys(Arrays.stream(splitOutput(output))
                        .filter(value -> value.contains(vrfName))
                        .findFirst(),
                TO_NEIGH_KEY, NEIGHBOR_LINE);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                             @Nonnull NeighborBuilder neighborBuilder, @Nonnull ReadContext
                                                         readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
    }

    @VisibleForTesting
    public static List<NeighborKey> getDefaultNeighborKeys(String output) {
        return parseKeys(Arrays.stream(splitOutput(output))
                        .filter(value -> !value.contains("vrf"))
                        .findFirst(),
                TO_NEIGH_KEY, NEIGHBOR_LINE);
    }

    public static <T> List<T> parseKeys(Optional<String> optionalVrfOutput, Function<String, T> transform, Pattern
            neighborLine) {
        if (optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll(" neighbor", "\n neighbor"), 0,
                neighborLine::matcher,
                matcher -> matcher.group("id"),
                transform);
        }
        return new ArrayList<>();
    }

    public static String[] splitOutput(String output) {
        // Skip any output before "router bgp" such as address-family definitions for VRFs or OSPF
        output = output.substring(output.indexOf("router bgp"));
        return output.replaceAll(Cli.NEWLINE, "")
                .replaceAll("\r", "")
                .replaceAll(" address-family", "\n address-family")
                .split("\\n");
    }
}
