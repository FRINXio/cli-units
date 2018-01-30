/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.common.LrListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopReader implements LrListReader.LrConfigListReader<NextHop, NextHopKey, NextHopBuilder> {

    private static final String SH_IP_STATIC_ROUTE = "sh run | include route %s";
    private static final String SH_IP_STATIC_ROUTE_VRF = "sh run | include route vrf %s %s";

    private Cli cli;

    public NextHopReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NextHopKey> getAllIdsForType(@Nonnull InstanceIdentifier<NextHop> instanceIdentifier,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        StaticKey staticRouteKey = instanceIdentifier.firstKeyOf(Static.class);
        String ipPrefix = getDevicePrefix(staticRouteKey);

        String cmd = vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ?
                String.format(SH_IP_STATIC_ROUTE, ipPrefix) :
                String.format(SH_IP_STATIC_ROUTE_VRF, vrfKey.getName(), ipPrefix);

        return parseNextHopPrefixes(blockingRead(cmd, cli, instanceIdentifier, readContext),
                ipPrefix, vrfKey);
    }

    static String getDevicePrefix(StaticKey staticRouteKey) {
        if (staticRouteKey.getPrefix().getIpv4Prefix() != null) {
            SubnetUtils.SubnetInfo info = new SubnetUtils(staticRouteKey.getPrefix().getIpv4Prefix().getValue()).getInfo();
            return String.format("%s %s", info.getNetworkAddress(), info.getNetmask());
        } else {
            return staticRouteKey.getPrefix().getIpv6Prefix().getValue();
        }
    }

    @VisibleForTesting
    static List<NextHopKey> parseNextHopPrefixes(String output, String ipPrefix, NetworkInstanceKey vrfKey) {
        List<NextHopKey> nextHopKeyes = new ArrayList<>();

        nextHopKeyes.addAll(
                ParsingUtils.parseFields(output, 0,
                        NextHopReader::ipv4Matcher,
                        NextHopReader::extractNextHopId,
                        NextHopKey::new));

        nextHopKeyes.addAll(
                ParsingUtils.parseFields(output, 0,
                        NextHopReader::ipv6Matcher,
                        NextHopReader::extractNextHopId,
                        NextHopKey::new));

        return nextHopKeyes;
    }

    private static String extractNextHopId(Matcher m) {
        String ip = m.group("ip");
        String ifc = m.group("ifc");

        if (ip != null) {
            return ifc == null ? ip : String.format("%s %s", ip, ifc);
        } else {
            return ifc;
        }
    }

    private static Matcher ipv4Matcher(String s) {
        return StaticReader.ROUTE_LINE_IP.matcher(s);
    }

    private static Matcher ipv6Matcher(String s) {
        return StaticReader.ROUTE_LINE_IP6.matcher(s);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<NextHop> list) {
        ((NextHopsBuilder) builder).setNextHop(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<NextHop> instanceIdentifier,
                                             @Nonnull NextHopBuilder nextHopBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        nextHopBuilder.setIndex(instanceIdentifier.firstKeyOf(NextHop.class).getIndex());
    }
}
