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

package io.frinx.cli.iosxr.routing.policy.handler.community;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunitySetReader implements CliConfigListReader<CommunitySet, CommunitySetKey, CommunitySetBuilder> {

    private static final String SH_ALL_PREFIX_SETS = "show running-config | include ^community-set";
    private static final Pattern ID_PATTERN = Pattern.compile("community-set (?<id>\\S+)");

    private final Cli cli;

    public CommunitySetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<CommunitySetKey> getAllIds(@Nonnull InstanceIdentifier<CommunitySet> id, @Nonnull ReadContext
            context) throws ReadFailedException {
        String output = blockingRead(SH_ALL_PREFIX_SETS, cli, id, context);
        return parseAllIds(output);
    }

    @VisibleForTesting
    static List<CommunitySetKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                ID_PATTERN::matcher,
            m -> m.group("id"),
                CommunitySetKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<CommunitySet> id, @Nonnull CommunitySetBuilder
            builder, @Nonnull ReadContext ctx) throws ReadFailedException {
        CommunitySetKey communitySetKey = id.firstKeyOf(CommunitySet.class);
        builder.setCommunitySetName(communitySetKey.getCommunitySetName());
    }
}
