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

package io.frinx.cli.unit.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.commons.net.util.SubnetUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopReader implements CliConfigListReader<NextHop, NextHopKey, NextHopBuilder> {

    private static final String SH_IP_STATIC_ROUTE = "show running-config | include route %s";
    private static final String SH_IP_STATIC_ROUTE_VRF = "show running-config | include route vrf %s %s";

    private Cli cli;

    public NextHopReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<NextHopKey> getAllIds(@NotNull InstanceIdentifier<NextHop> instanceIdentifier,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        StaticKey staticRouteKey = instanceIdentifier.firstKeyOf(Static.class);
        String ipPrefix = getDevicePrefix(staticRouteKey);

        String cmd = vrfKey.equals(NetworInstance.DEFAULT_NETWORK)
                ?
                String.format(SH_IP_STATIC_ROUTE, ipPrefix) :
                String.format(SH_IP_STATIC_ROUTE_VRF, vrfKey.getName(), ipPrefix);

        return parseNextHopPrefixes(blockingRead(cmd, cli, instanceIdentifier, readContext),
                ipPrefix, vrfKey);
    }

    static String getDevicePrefix(StaticKey staticRouteKey) {
        if (staticRouteKey.getPrefix()
                .getIpv4Prefix() != null) {
            SubnetUtils.SubnetInfo info = new SubnetUtils(staticRouteKey.getPrefix()
                    .getIpv4Prefix()
                    .getValue()).getInfo();
            return String.format("%s %s", info.getNetworkAddress(), info.getNetmask());
        } else {
            return staticRouteKey.getPrefix()
                    .getIpv6Prefix()
                    .getValue();
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

    private static String extractNextHopId(Matcher matcher) {
        String ip = matcher.group("ip");
        String ifc = matcher.group("ifc");

        if (ip != null) {
            return ifc == null ? ip : String.format("%s %s", ip, ifc);
        } else {
            return ifc;
        }
    }

    private static Matcher ipv4Matcher(String string) {
        return StaticReader.ROUTE_LINE_IP.matcher(string);
    }

    private static Matcher ipv6Matcher(String string) {
        return StaticReader.ROUTE_LINE_IP6.matcher(string);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<NextHop> instanceIdentifier,
                                             @NotNull NextHopBuilder nextHopBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        nextHopBuilder.setIndex(instanceIdentifier.firstKeyOf(NextHop.class)
                .getIndex());
    }
}