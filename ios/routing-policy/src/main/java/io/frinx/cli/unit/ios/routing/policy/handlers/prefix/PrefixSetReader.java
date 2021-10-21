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
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixSetReader implements CliConfigListReader<PrefixSet, PrefixSetKey, PrefixSetBuilder> {

    public static final String SH_IPV4_PREFIX_SETS = "show ip prefix-list";
    public static final String SH_IPV6_PREFIX_SETS = "show ipv6 prefix-list";
    private static final Pattern ID_PATTERN = Pattern.compile("ip(v6)? prefix-list (?<name>\\S+):.*");

    private final Cli cli;

    public PrefixSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<PrefixSetKey> getAllIds(@Nonnull InstanceIdentifier<PrefixSet> id,
                                        @Nonnull ReadContext context) throws ReadFailedException {
        String outputIpv4 = blockingRead(SH_IPV4_PREFIX_SETS, cli, id, context);
        String outputIpv6 = blockingRead(SH_IPV6_PREFIX_SETS, cli, id, context);
        List<PrefixSetKey> allPrefixLists = parseAllIds(outputIpv4);
        allPrefixLists.addAll(parseAllIds(outputIpv6));
        return allPrefixLists;
    }

    @VisibleForTesting
    static List<PrefixSetKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            ID_PATTERN::matcher,
            m -> m.group("name"),
            PrefixSetKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<PrefixSet> id,
                                      @Nonnull PrefixSetBuilder builder,
                                      @Nonnull ReadContext context) throws ReadFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        builder.setName(prefixSetKey.getName());
    }

}