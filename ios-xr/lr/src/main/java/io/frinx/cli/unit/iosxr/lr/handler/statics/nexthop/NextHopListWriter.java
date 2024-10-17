/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lr.handler.statics.nexthop;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV4MULTICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV6MULTICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.SetTagAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopListWriter implements CliListWriter<NextHop, NextHopKey> {

    private Cli cli;

    private static final String COMMAND_TEMPLATE = """
            router static
            address-family {$afisafi}
            {% if $delete %}no{% endif %} {$ip} {$etherIp} {$nexthopIp}{% if (!$delete) %} {$tagValue}{% endif %}
            root""";

    public NextHopListWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<NextHop> iid, @NotNull NextHop nexthop,
                                       @NotNull WriteContext context) throws WriteFailedException {
        IpPrefix pf = iid.firstKeyOf(Static.class).getPrefix();
        Optional<Static> staticOptional = context.readAfter(RWUtils.cutId(iid, Static.class));
        Preconditions.checkArgument(staticOptional.isPresent());
        Config staticConfig = Preconditions.checkNotNull(staticOptional.get().getConfig());
        AfiSafiAug afisafi = Preconditions.checkNotNull(staticConfig.getAugmentation(AfiSafiAug.class));
        Preconditions.checkNotNull(afisafi.getAfiSafiType());

        blockingWriteAndRead(cli, iid, nexthop,
            fT(COMMAND_TEMPLATE,
                "afisafi", getAfiSafiCmd(afisafi),
                "ip", isIpv6(afisafi) ? pf.getIpv6Prefix().getValue() : pf.getIpv4Prefix().getValue(),
                "etherIp", getNextHopInterfaceRef(nexthop),
                "nexthopIp", getNextHopDestIp(nexthop.getConfig(), afisafi),
                "tagValue", getNextHopTag(nexthop.getConfig())));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<NextHop> iid,
                                        @NotNull NextHop before,
                                        @NotNull NextHop after,
                                        @NotNull WriteContext context) throws WriteFailedException {
        deleteCurrentAttributes(iid,before,context);
        writeCurrentAttributes(iid,after,context);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<NextHop> iid, @NotNull NextHop before,
                                        @NotNull WriteContext context) throws WriteFailedException {
        Optional<Static> staticOptional = context.readBefore(RWUtils.cutId(iid, Static.class));
        Preconditions.checkArgument(staticOptional.isPresent());
        Config staticConfig = Preconditions.checkNotNull(staticOptional.get().getConfig());
        AfiSafiAug afisafi = staticConfig.getAugmentation(AfiSafiAug.class);
        IpPrefix pf = iid.firstKeyOf(Static.class).getPrefix();

        blockingWriteAndRead(cli, iid, before,
            fT(COMMAND_TEMPLATE,
                "delete", true,
                "afisafi", getAfiSafiCmd(afisafi),
                "ip", isIpv6(afisafi) ? pf.getIpv6Prefix().getValue() : pf.getIpv4Prefix().getValue(),
                "etherIp", getNextHopInterfaceRef(before),
                "nexthopIp", getNextHopDestIp(before.getConfig(), afisafi),
                "tagValue", ""));
    }

    private static boolean isIpv6(AfiSafiAug afiSafi) {
        Class<?> type = afiSafi.getAfiSafiType();
        return type == IPV6UNICAST.class || type == IPV6MULTICAST.class;
    }

    private static String getAfiSafiCmd(AfiSafiAug afiSafi) {
        Class<?> type = afiSafi.getAfiSafiType();
        if (type == IPV6UNICAST.class) {
            return "ipv6 unicast";
        } else if (type == IPV6MULTICAST.class) {
            return "ipv6 multicast";
        } else if (type == IPV4MULTICAST.class) {
            return "ipv4 multicast";
        } else {
            return "ipv4 unicast";
        }
    }

    private static String getNextHopDestIp(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
        .local.routing.rev170515.local._static.top._static.routes._static
        .next.hops.next.hop.Config config, AfiSafiAug afisafi) {
        String nexthopIp = "";
        if (config.getNextHop() != null) {
            nexthopIp = isIpv6(afisafi) ? config.getNextHop().getIpv6Address().getValue() :
                config.getNextHop().getIpv4Address().getValue();
        }
        return nexthopIp;
    }

    private static String getNextHopInterfaceRef(NextHop nh) {
        String ifcRef = "";
        if (nh.getInterfaceRef() != null) {
            ifcRef = nh.getInterfaceRef().getConfig().getInterface()
                + "."
                + nh.getInterfaceRef().getConfig().getSubinterface();
        }
        return ifcRef;
    }

    private static String getNextHopTag(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
        .local.routing.rev170515.local._static.top._static.routes._static
        .next.hops.next.hop.Config config) {
        String tag = "";
        if (config.getAugmentation(SetTagAug.class) != null) {
            tag =  "tag " + config.getAugmentation(SetTagAug.class).getSetTag().getUint32();
        }
        return tag;
    }
}