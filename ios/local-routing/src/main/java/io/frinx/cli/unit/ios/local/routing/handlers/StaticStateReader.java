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
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_IP_STATIC_ROUTE = "show ip route | include %s";
    private static final String SH_IPV6_STATIC_ROUTE = "show ipv6 route | include %s";

    private static final Pattern STATIC_IP_ROUTE =
            Pattern.compile("(?<state>[A-Z]+)\\s*(?<net>[\\d.]+)\\s*\\["
                    + "(?<metric>[\\d/]+)]\\s*via\\s*(?<ip>[\\d.]+).*");
    private static final Pattern STATIC_IPV6_ROUTE =
            Pattern.compile("(?<state>[A-Z]+)\\s*(?<net>[\\d:/A-F]+)\\s*\\[(?<metric>[\\d/]+)].*");
    private static final Pattern IP_ROUTE = Pattern.compile("(?<address>[\\d.]+)/(?<mask>[\\d.]+)");

    private Cli cli;

    public StaticStateReader(final Cli cli) {
        this.cli = cli;
    }

    private static Matcher getMatcher(String string) {
        Matcher matcher = STATIC_IP_ROUTE.matcher(string);
        return matcher.matches() ? matcher : STATIC_IPV6_ROUTE.matcher(string);
    }

    private static String getIpAddress(String fullAddress) {
        Optional<String> ipAddress = ParsingUtils.parseField(fullAddress, 0, IP_ROUTE::matcher,
            matcher -> matcher.group("address"));
        return ipAddress.orElse(fullAddress);
    }

    @VisibleForTesting
    static boolean isPrefixStatic(String output) {
        Optional<String> state = ParsingUtils.parseField(output, 0, StaticStateReader::getMatcher,
            matcher -> matcher.group("state"));
        return state.isPresent() && state.get().equals("S");
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                             @NotNull StateBuilder stateBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        IpPrefix ipPrefix = instanceIdentifier.firstKeyOf(Static.class).getPrefix();
        String cmd = ipPrefix.getIpv6Prefix() == null
                ? String.format(SH_IP_STATIC_ROUTE, getIpAddress(ipPrefix.getIpv4Prefix().getValue()))
                : String.format(SH_IPV6_STATIC_ROUTE, ipPrefix.getIpv6Prefix().getValue());
        if (isPrefixStatic(blockingRead(cmd, cli, instanceIdentifier, readContext))) {
            stateBuilder.setPrefix(ipPrefix);
        }
    }
}