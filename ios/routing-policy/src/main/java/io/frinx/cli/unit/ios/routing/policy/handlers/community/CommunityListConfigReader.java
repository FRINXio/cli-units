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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommunityMembers.CommunityMemberDeny;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.CommunitySetConfig.CommunityMember;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpCommunityRegexpType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityTypeString;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class CommunityListConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_RUN_VRF_ID = "show running-config | include ^ip community-list";
    private static final Pattern COMMUNITY_MEMBER_STANDARD =
            Pattern.compile("ip community-list standard (?<rt>[\\S]+) (permit|deny) (?<number>[\\d+\\:\\d]+)");
    private static final Pattern COMMUNITY_MEMBER_EXPANDED =
            Pattern.compile("ip community-list expanded (?<rt>[\\S]+) (permit|deny) (?<number>[\\S]+)");

    private final Cli cli;

    public CommunityListConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String communityName = instanceIdentifier.firstKeyOf(CommunitySet.class).getCommunitySetName();
        String output = blockingRead(SH_RUN_VRF_ID, cli, instanceIdentifier, readContext);
        parseCommunityConfig(output, configBuilder, communityName);
    }

    @VisibleForTesting
    static void parseCommunityConfig(String output, ConfigBuilder configBuilder, String communityName) {
        configBuilder.setCommunitySetName(communityName);
        List<CommunityMember> communityMembers;
        List<CommunityMemberDeny> communityMembersDeny;
        if (output.contains("standard " + communityName)) {
            communityMembers = parseMembers(output, communityName, "standard ", "permit",
                    COMMUNITY_MEMBER_STANDARD, value -> new CommunityMember(
                                   new BgpStdCommunityType(BgpStdCommunityTypeString.getDefaultInstance(value))));
            communityMembersDeny = parseMembers(output, communityName, "standard ", "deny",
                    COMMUNITY_MEMBER_STANDARD, value -> new CommunityMemberDeny(
                            new BgpStdCommunityType(BgpStdCommunityTypeString.getDefaultInstance(value))));
        } else {
            communityMembers = parseMembers(output, communityName, "expanded ", "permit",
                    COMMUNITY_MEMBER_EXPANDED, value -> new CommunityMember(new BgpCommunityRegexpType(value)));
            communityMembersDeny = parseMembers(output, communityName, "expanded ", "deny",
                    COMMUNITY_MEMBER_EXPANDED, value -> new CommunityMemberDeny(new BgpCommunityRegexpType(value)));
        }
        configBuilder.addAugmentation(CommMembersAug.class, new CommMembersAugBuilder()
                .setCommunityMemberDeny(communityMembersDeny)
                .build());
        configBuilder.setCommunityMember(communityMembers);
        configBuilder.build();
    }

    private static <T> List<T> parseMembers(String output, String communityName,
                                            String standardExpanded, String permitDeny, Pattern regex,
                                            Function<String, T> mapper) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(line -> line.contains(standardExpanded + communityName + " "))
                .filter(line -> line.contains(permitDeny))
                .map(regex::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("number"))
                .map(mapper)
                .collect(Collectors.toList());
    }
}

