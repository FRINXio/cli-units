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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefixBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixReader implements CliConfigListReader<Prefix, PrefixKey, PrefixBuilder> {

    public static final String SH_ALL_PREFIX_SETS = "show running-config | include ^ip.* prefix-list";
    private static final String PREFIX_LINE = "ip(v6)? prefix-list %s seq \\d+ \\S+ (?<network>\\S+).*";

    private final Cli cli;

    public PrefixReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<PrefixKey> getAllIds(@Nonnull InstanceIdentifier<Prefix> id,
                                     @Nonnull ReadContext context) throws ReadFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        String output = blockingRead(SH_ALL_PREFIX_SETS, cli, id, context);
        return parseIds(output, prefixSetKey.getName());
    }

    @VisibleForTesting
    static List<PrefixKey> parseIds(String output, String name) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
            .map(Pattern.compile(String.format(PREFIX_LINE, name))::matcher)
            .filter(Matcher::matches)
            .map(PrefixReader::toKey)
            .distinct()
            .collect(Collectors.toList());
    }

    private static PrefixKey toKey(Matcher matcher) {
        String network = matcher.group("network");
        IpPrefix ipPrefix = IpPrefixBuilder.getDefaultInstance(network);
        return new PrefixKey(ipPrefix, "exact");
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Prefix> id,
                                      @Nonnull PrefixBuilder builder,
                                      @Nonnull ReadContext context) throws ReadFailedException {
        PrefixKey prefixKey = id.firstKeyOf(Prefix.class);
        builder.setIpPrefix(prefixKey.getIpPrefix());
        builder.setMasklengthRange(prefixKey.getMasklengthRange());
    }

}