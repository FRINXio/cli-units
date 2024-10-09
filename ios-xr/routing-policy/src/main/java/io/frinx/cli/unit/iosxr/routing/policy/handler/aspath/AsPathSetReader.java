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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AsPathSetReader implements CliConfigListReader<AsPathSet, AsPathSetKey, AsPathSetBuilder> {

    private static final String SH_ALL_PREFIX_SETS = "show running-config | include ^as-path-set";
    private static final Pattern ID_PATTERN = Pattern.compile("as-path-set (?<id>\\S+)");

    private final Cli cli;

    public AsPathSetReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AsPathSetKey> getAllIds(@NotNull InstanceIdentifier<AsPathSet> id, @NotNull ReadContext context)
            throws ReadFailedException {
        String output = blockingRead(SH_ALL_PREFIX_SETS, cli, id, context);
        return parseAllIds(output);
    }

    @VisibleForTesting
    static List<AsPathSetKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                ID_PATTERN::matcher,
            m -> m.group("id"),
                AsPathSetKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AsPathSet> id, @NotNull AsPathSetBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        AsPathSetKey communitySetKey = id.firstKeyOf(AsPathSet.class);
        builder.setAsPathSetName(communitySetKey.getAsPathSetName());
    }
}