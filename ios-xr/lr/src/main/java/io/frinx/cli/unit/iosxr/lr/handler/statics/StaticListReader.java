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

package io.frinx.cli.unit.iosxr.lr.handler.statics;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticListReader implements CliConfigListReader<Static, StaticKey, StaticBuilder> {

    private final Cli cli;
    public static final String SH_READ_STATIC_ROUTE_IPV4_UNICAST =
            "show running-config router static address-family ipv4 unicast";
    private static final Pattern STATIC_ROUTE_IPV4_UNICAST_PATTERN =
            Pattern.compile("^(?<prefix>[\\d\\./]+) (?<ifc>[a-zA-Z][\\S]*)?"
            + " ?(?<peerip>[\\d\\.]*)? ?(tag (?<tagid>.+))?$");
    private static final String SH_READ_STATIC_ROUTE_IPV6_UNICAST =
            "show running-config router static address-family ipv6 unicast";
    public static final Pattern STATIC_ROUTE_IPV6_UNICAST_PATTERN =
            Pattern.compile("^(?<prefix>[\\p{XDigit}\\:/]+) (?<ifc>[a-zA-Z][\\S]*)?"
            + " ?(?<peerip>[\\p{XDigit}\\:]*)? ?(tag (?<tagid>.+))?$");


    public StaticListReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<StaticKey> getAllIds(@Nonnull InstanceIdentifier<Static> id, @Nonnull ReadContext context)
            throws ReadFailedException {

        if (!NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return Collections.emptyList();
        }

        // read all ipv4-unicast static routes
        String output = blockingRead(SH_READ_STATIC_ROUTE_IPV4_UNICAST, cli, id, context);
        List<StaticKey> rtn = ParsingUtils.parseFields(output,
            0,
            STATIC_ROUTE_IPV4_UNICAST_PATTERN::matcher,
            matcher -> new StaticKey(new IpPrefix(new Ipv4Prefix(matcher.group("prefix")))),
            StaticKey::new);

        // read all ipv6-unicast static routes
        output = blockingRead(SH_READ_STATIC_ROUTE_IPV6_UNICAST, cli, id, context);
        List<StaticKey> rtnv6 = ParsingUtils.parseFields(output,
            0,
            STATIC_ROUTE_IPV6_UNICAST_PATTERN::matcher,
            matcher -> new StaticKey(new IpPrefix(new Ipv6Prefix(matcher.group("prefix")))),
            StaticKey::new);

        //
        rtn.addAll(rtnv6);
        return rtn;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Static> id, @Nonnull StaticBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        StaticKey key = id.firstKeyOf(Static.class);
        IpPrefix pf = key.getPrefix();
        builder.setPrefix(pf);
        builder.setKey(key);
    }
}
