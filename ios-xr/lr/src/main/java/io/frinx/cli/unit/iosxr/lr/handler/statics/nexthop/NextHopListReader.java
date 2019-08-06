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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRef;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRefBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.SetTagAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.SetTagAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.TagType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopListReader implements CliConfigListReader<NextHop, NextHopKey, NextHopBuilder> {

    private final Cli cli;
    private static final Pattern NEXT_HOPS_PATTERN =
            Pattern.compile("^(?<prefix>[\\d\\./]+) (?<nexthop>.+)$");
    private static final String SH_READ_NEXT_HOP_IPV4_UNICAST =
            "show running-config router static address-family ipv4 unicast | include %s";
    private static final Pattern NEXT_HOP_IPV4_UNICAST_PATTERN =
            Pattern.compile("^(?<ifcName>[a-zA-Z][\\S]*)? ?(?<peerip>[\\d\\.]+)?( tag (?<tagid>.+))?$");

    private static final String SH_READ_NEXT_HOP_IPV6_UNICAST =
            "show running-config router static address-family ipv6 unicast | include %s";
    private static final Pattern NEXT_HOP_IPV6_UNICAST_PATTERN =
            Pattern.compile("^(?<ifcName>[a-zA-Z][\\S]*)? ?(?<peerip>[\\p{XDigit}\\:]*)?( tag (?<tagid>.+))?$");

    public NextHopListReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NextHopKey> getAllIds(@Nonnull InstanceIdentifier<NextHop> id, @Nonnull ReadContext context)
            throws ReadFailedException {

        if (!NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return Collections.emptyList();
        }
        //
        List<NextHopKey> rtn = new ArrayList<>();
        StaticKey key = id.firstKeyOf(Static.class);
        IpPrefix pf = key.getPrefix();

        AtomicInteger count = new AtomicInteger();
        if (pf.getIpv4Prefix() != null) {
            String output = blockingRead(f(SH_READ_NEXT_HOP_IPV4_UNICAST, pf.getIpv4Prefix().getValue()),
                cli, id, context);
            rtn = ParsingUtils.parseFields(output, 0,
                    NEXT_HOPS_PATTERN::matcher, m -> m.group("nexthop"),
                value -> value).stream().map(value -> {
                    count.incrementAndGet();
                    return new NextHopKey(String.valueOf(count.intValue()));
                }).collect(Collectors.toList());
        } else {
            String output = blockingRead(f(SH_READ_NEXT_HOP_IPV6_UNICAST, pf.getIpv6Prefix().getValue()),
                cli, id, context);
            rtn = ParsingUtils.parseFields(output, 0,
                    NEXT_HOPS_PATTERN::matcher, m -> m.group("nexthop"),
                value -> value).stream().map(value -> {
                    count.incrementAndGet();
                    return new NextHopKey(String.valueOf(count.intValue()));
                }).collect(Collectors.toList());
        }
        return rtn;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NextHop> id, @Nonnull NextHopBuilder builder,
                                      @Nonnull ReadContext context) throws ReadFailedException {
        NextHopKey key = id.firstKeyOf(NextHop.class);
        builder.setKey(key);
        IpPrefix pf = id.firstKeyOf(Static.class).getPrefix();
        fillNextHopBuilder(id, key.getIndex(), context, pf, builder);
    }

    private void fillNextHopBuilder(
        @Nonnull InstanceIdentifier<NextHop> id,
        @Nonnull String index,
        @Nonnull ReadContext context,
        @Nonnull IpPrefix pf,
        @Nonnull NextHopBuilder builder
    )throws ReadFailedException {
        boolean isIpv4 = pf.getIpv4Prefix() != null;
        Pattern pat = isIpv4 ? NEXT_HOP_IPV4_UNICAST_PATTERN : NEXT_HOP_IPV6_UNICAST_PATTERN;
        String cmdF = isIpv4 ? SH_READ_NEXT_HOP_IPV4_UNICAST : SH_READ_NEXT_HOP_IPV6_UNICAST;
        String ip = isIpv4 ?  pf.getIpv4Prefix().getValue() : pf.getIpv6Prefix().getValue();
        String output = blockingRead(f(cmdF, ip), cli, id, context);
        List<String> nexthops = ParsingUtils.parseFields(
            output, 0,
            NEXT_HOPS_PATTERN::matcher,
            m -> m.group("nexthop"),
            value -> value);
        String nexthop = nexthops.get(Integer.valueOf(index) - 1);
        ParsingUtils.parseFields(nexthop, 0, pat::matcher, m -> {
            Pattern ifcPtn = Pattern.compile("^(?<name>.*?)(\\.(?<subIdx>\\d+))?$");
            InterfaceRef ref = null;
            if (m.group("ifcName") != null) {
                Matcher m1 = ifcPtn.matcher(m.group("ifcName"));
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref
                    ._interface.ref.ConfigBuilder ifcRefCfgBuilder =
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                            .net.yang.interfaces.rev161222._interface.ref._interface.ref.ConfigBuilder();
                m1.find();
                ifcRefCfgBuilder.setInterface(m1.group("name"));
                if (m1.group("subIdx") != null) {
                    ifcRefCfgBuilder.setSubinterface(Long.valueOf(m1.group("subIdx")));
                }
                ref = new InterfaceRefBuilder()
                    .setConfig(ifcRefCfgBuilder.build())
                    .build();
            }
            ConfigBuilder cnfBuilder = new ConfigBuilder().setIndex(index);
            if (m.group("peerip") != null) {
                IpAddress nextHopIp1 = isIpv4 ? new IpAddress(new Ipv4Address(m.group("peerip")))
                    : new IpAddress(new Ipv6Address(m.group("peerip")));
                cnfBuilder.setNextHop(nextHopIp1);
            }
            if (m.group("tagid") != null) {
                cnfBuilder.addAugmentation(SetTagAug.class,
                      new SetTagAugBuilder().setSetTag(new TagType(Long.valueOf(m.group("tagid")))).build());
            }
            Config config = cnfBuilder.build();

            return builder.setConfig(config).setInterfaceRef(ref);
        }, value -> value);
    }
}