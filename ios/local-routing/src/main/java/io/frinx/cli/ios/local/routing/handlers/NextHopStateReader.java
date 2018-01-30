/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.common.LrReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopStateReader implements LrReader.LrOperReader<State, StateBuilder> {

    private static final String SHOW_IP_STATIC_ROUTE_NETWORK = "sh ip static route %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_NETWORK_IP6 = "sh ipv6 static %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_VRF_NETWORK = "sh ip static route vrf %s %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_VRF_NETWORK_IP6 = "sh ipv6 static vrf %s %s | include %s";

    private static final Pattern METRIC_LINE = Pattern.compile(".*\\[(?<metric>\\d+)/\\d+].*");
    private static final Pattern DISTANCE_LINE = Pattern.compile(".*distance (?<metric>\\d+).*");

    private Cli cli;

    public NextHopStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> id, @Nonnull StateBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        StaticKey staticRouteKey = id.firstKeyOf(Static.class);
        String ipPrefix = NextHopReader.getDevicePrefix(staticRouteKey);

        NextHopKey nextHopKey = id.firstKeyOf(NextHop.class);
        String index = nextHopKey.getIndex();
        String nextHop = NextHopConfigReader.switchIndex(index);


        String showCommand = vrfKey.equals(DEFAULT_NETWORK)
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

    private static Matcher getMatcher(String s) {
        Matcher matcher = METRIC_LINE.matcher(s);
        return matcher.matches() ? matcher : DISTANCE_LINE.matcher(s);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull State readValue) {
        ((NextHopBuilder) parentBuilder).setState(readValue);
    }
}
