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

package io.frinx.cli.unit.iosxr.routing.policy.handler.prefix;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixSetReader implements CliConfigListReader<PrefixSet, PrefixSetKey, PrefixSetBuilder> {

    private static final String SH_ALL_PREFIX_SETS = "show running-config | include ^prefix-set";
    private static final Pattern ID_PATTERN = Pattern.compile("prefix-set (?<id>\\S+)");

    private final Cli cli;

    public PrefixSetReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<PrefixSetKey> getAllIds(@NotNull InstanceIdentifier<PrefixSet> id, @NotNull ReadContext context)
            throws ReadFailedException {
        String output = blockingRead(SH_ALL_PREFIX_SETS, cli, id, context);
        return parseAllIds(output);
    }

    @VisibleForTesting
    static List<PrefixSetKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                ID_PATTERN::matcher,
            m -> m.group("id"),
                PrefixSetKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<PrefixSet> id, @NotNull PrefixSetBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        builder.setName(prefixSetKey.getName());
    }
}