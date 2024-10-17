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

package io.frinx.cli.unit.ios.routing.policy.handlers.community;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class CommunityListReader implements
        CliConfigListReader<CommunitySet, CommunitySetKey, CommunitySetBuilder> {

    private static final String SH_RUN_VRF_ID = "show running-config | include ^ip community-list";
    private static final Pattern COMMUNITY_LIST =
            Pattern.compile("ip community-list (standard|expanded) (?<name>[\\S]+) .*");

    private Cli cli;

    public CommunityListReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<CommunitySetKey> getAllIds(@NotNull InstanceIdentifier<CommunitySet> id,
                                           @NotNull ReadContext context) throws ReadFailedException {
        String output = blockingRead(SH_RUN_VRF_ID, cli, id, context);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<CommunitySetKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            COMMUNITY_LIST::matcher,
            matcher -> matcher.group("name"),
            CommunitySetKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<CommunitySet> id,
                                      @NotNull CommunitySetBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        builder.setKey(id.firstKeyOf(CommunitySet.class));
    }
}