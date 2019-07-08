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

package io.frinx.cli.unit.iosxr.routing.policy.handler.aspath;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.as.path.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.as.path.set.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AsPathSetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_AS_PATH_SET = "show running-config as-path-set %s";
    private static final Pattern AS_PATH_PATTERN = Pattern.compile("\\s+(?<asPathRule>.+?),?\\s*");

    private final Cli cli;

    public AsPathSetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        AsPathSetKey prefixSetKey = id.firstKeyOf(AsPathSet.class);

        String output = blockingRead(f(SH_AS_PATH_SET, prefixSetKey.getAsPathSetName()), cli, id, ctx);
        parseMembers(builder, prefixSetKey, output);
    }

    @VisibleForTesting
    static void parseMembers(@Nonnull ConfigBuilder builder, AsPathSetKey prefixSetKey, String output) {
        List<String> asPathRule = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(AS_PATH_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("asPathRule"))
                .distinct()
                .collect(Collectors.toList());

        builder.setAsPathSetName(prefixSetKey.getAsPathSetName());
        builder.setAsPathSetMember(asPathRule);
    }
}
