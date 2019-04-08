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

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SHOW_IP_STATIC_ROUTE_NETWORK = "show ip static route %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_NETWORK_IP6 = "show ipv6 static %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_VRF_NETWORK = "show ip static route vrf %s %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_VRF_NETWORK_IP6 = "show ipv6 static vrf %s %s | include %s";

    private static final Pattern METRIC_LINE = Pattern.compile(".*\\[(?<metric>\\d+)/\\d+].*");
    private static final Pattern DISTANCE_LINE = Pattern.compile(".*distance (?<metric>\\d+).*");

    private Cli cli;

    public NextHopStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> id, @Nonnull StateBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        StaticKey staticRouteKey = id.firstKeyOf(Static.class);
        String ipPrefix = NextHopReader.getDevicePrefix(staticRouteKey);

        NextHopKey nextHopKey = id.firstKeyOf(NextHop.class);
        String index = nextHopKey.getIndex();
        String nextHop = NextHopConfigReader.switchIndex(index);


        String showCommand = vrfKey.equals(NetworInstance.DEFAULT_NETWORK)
                ? String.format(getCommand(staticRouteKey), ipPrefix, nextHop)
                : String.format(getVrfCommand(staticRouteKey), vrfKey.getName(), ipPrefix, nextHop);

        String output = blockingRead(showCommand, cli, id, ctx);

        if (output.contains(nextHop)) {
            builder.setIndex(index);
        }

        parseMetric(output, builder);
    }

    private String getCommand(StaticKey staticRouteKey) {
        if (staticRouteKey.getPrefix().getIpv4Prefix() != null) {
            return SHOW_IP_STATIC_ROUTE_NETWORK;
        } else {
            return SHOW_IP_STATIC_ROUTE_NETWORK_IP6;
        }
    }

    private String getVrfCommand(StaticKey staticRouteKey) {
        if (staticRouteKey.getPrefix().getIpv4Prefix() != null) {
            return SHOW_IP_STATIC_ROUTE_VRF_NETWORK;
        } else {
            return SHOW_IP_STATIC_ROUTE_VRF_NETWORK_IP6;
        }
    }

    @VisibleForTesting
    static void parseMetric(String output, StateBuilder stateBuilder) {
        ParsingUtils.parseField(output, 0,
                NextHopStateReader::getMatcher,
            matcher -> Long.valueOf(matcher.group("metric")),
                stateBuilder::setMetric);
    }

    private static Matcher getMatcher(String string) {
        Matcher matcher = METRIC_LINE.matcher(string);
        return matcher.matches() ? matcher : DISTANCE_LINE.matcher(string);
    }

}
