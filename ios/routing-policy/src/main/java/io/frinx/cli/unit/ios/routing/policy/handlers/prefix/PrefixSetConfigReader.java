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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.PrefixSetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.prefix.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.prefix.set.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixSetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String IPV4_LINE = "ip prefix-list %s";
    private static final String IPV6_LINE = "ipv6 prefix-list %s";
    private static final String PREFIX_LIST = "show ip prefix-list %s";
    private static final String PREFIX_LIST_V6 = "show ipv6 prefix-list %s";

    private final Cli cli;

    public PrefixSetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        String outputIpv4 = blockingRead(f(PREFIX_LIST, prefixSetKey.getName()), cli, id, ctx);
        String outputIpv6 = blockingRead(f(PREFIX_LIST_V6, prefixSetKey.getName()), cli, id, ctx);
        parseConfig(prefixSetKey.getName(), outputIpv4 + outputIpv6, builder);
    }

    @VisibleForTesting
    static void parseConfig(String prefixSetName, String output, ConfigBuilder builder) {
        builder.setName(prefixSetName);

        String ipv4PrefixList = String.format(IPV4_LINE, prefixSetName);
        String ipv6PrefixList = String.format(IPV6_LINE, prefixSetName);

        if (output.contains(ipv4PrefixList) && output.contains(ipv6PrefixList)) {
            builder.setMode(PrefixSetConfig.Mode.MIXED);
        } else if (output.contains(ipv4PrefixList)) {
            builder.setMode(PrefixSetConfig.Mode.IPV4);
        } else if (output.contains(ipv6PrefixList)) {
            builder.setMode(PrefixSetConfig.Mode.IPV6);
        }
    }
}