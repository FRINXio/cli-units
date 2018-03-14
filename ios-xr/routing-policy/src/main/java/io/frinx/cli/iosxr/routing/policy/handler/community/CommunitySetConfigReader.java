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

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.CommunitySetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunitySetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_AS_PATH_SET = "do show running-config community-set %s";
    private static final Pattern AS_PATH_PATTERN = Pattern.compile("\\s+(?<communityRule>[^,]+),?");

    private final Cli cli;

    public CommunitySetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        CommunitySetKey communitySetKey = id.firstKeyOf(CommunitySet.class);

        String output = blockingRead(f(SH_AS_PATH_SET, communitySetKey.getCommunitySetName()), cli, id, ctx);
        parseMembers(builder, communitySetKey, output);
    }

    @VisibleForTesting
    static void parseMembers(@Nonnull ConfigBuilder builder, CommunitySetKey communitySetKey, String output) {
        List<CommunitySetConfig.CommunityMember> comms = NEWLINE.splitAsStream(output)
                .map(AS_PATH_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("communityRule"))
                .map(CommunitySetConfigReader::parseMember)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        builder.setCommunityMember(comms);
        builder.setCommunitySetName(communitySetKey.getCommunitySetName());
    }

    private static final Pattern NO_EXPORT = Pattern.compile("no-export");
    private static final Pattern NO_ADVERTISE = Pattern.compile("no-advertise");

    private static CommunitySetConfig.CommunityMember parseMember(String s) {
        // TODO finish parse members
        return null;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((CommunitySetBuilder) parentBuilder).setConfig(readValue);
    }
}
