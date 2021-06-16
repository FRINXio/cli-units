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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommunityMembers.CommunityMemberDeny;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.CommunitySetConfig.CommunityMember;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class CommunityListConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "{% loop in $members as $member %}\n"
            + "{% if ($member.bgp_std_community_type.bgp_std_community_type_string.value) %}"
            + "ip community-list standard {$group_name} permit "
            + "{$member.bgp_std_community_type.bgp_std_community_type_string.value}\n"
            + "{% endif %}"
            + "{% if ($member.bgp_std_community_type.bgp_std_community_type_unit32.value) %}"
            + "ip community-list standard {$group_name} permit "
            + "{$member.bgp_std_community_type.bgp_std_community_type_unit32.value}\n"
            + "{% endif %}"
            + "{% if ($member.bgp_community_regexp_type.value) %}"
            + "ip community-list expanded {$group_name} permit {$member.bgp_community_regexp_type.value}\n"
            + "{% endif %}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% loop in $deny_members as $deny_member %}\n"
            + "{% if ($deny_member.bgp_std_community_type.bgp_std_community_type_string.value) %}"
            + "ip community-list standard {$group_name} deny "
            + "{$deny_member.bgp_std_community_type.bgp_std_community_type_string.value}\n"
            + "{% endif %}"
            + "{% if ($deny_member.bgp_std_community_type.bgp_std_community_type_unit32.value) %}"
            + "ip community-list standard {$group_name} deny "
            + "{$deny_member.bgp_std_community_type.bgp_std_community_type_unit32.value}\n"
            + "{% endif %}"
            + "{% if ($deny_member.bgp_community_regexp_type.value) %}"
            + "ip community-list expanded {$group_name} deny {$deny_member.bgp_community_regexp_type.value}\n"
            + "{% endif %}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "{% loop in $members as $member %}\n"
            + "{% if ($member.bgp_std_community_type.bgp_std_community_type_string.value) %}"
            + "no ip community-list standard {$group_name}\n"
            + "{% elseIf ($member.bgp_std_community_type.bgp_std_community_type_unit32.value) %}"
            + "no ip community-list standard {$group_name}\n"
            + "{% elseIf ($member.bgp_community_regexp_type.value) %}"
            + "no ip community-list expanded {$group_name}\n"
            + "{% endif %}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% loop in $deny_members as $deny_member %}\n"
            + "{% if ($deny_member.bgp_std_community_type.bgp_std_community_type_string.value) %}"
            + "no ip community-list standard {$group_name}\n"
            + "{% elseIf ($deny_member.bgp_std_community_type.bgp_std_community_type_unit32.value) %}"
            + "no ip community-list standard {$group_name}\n"
            + "{% elseIf ($deny_member.bgp_community_regexp_type.value) %}"
            + "no ip community-list expanded {$group_name}\n"
            + "{% endif %}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "end";


    private final Cli cli;

    public CommunityListConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String groupName = config.getCommunitySetName();
        List<CommunityMember> members = config.getCommunityMember();
        List<CommunityMemberDeny> denyMembers = null;
        if (config.getAugmentation(CommMembersAug.class) != null) {
            denyMembers = config.getAugmentation(CommMembersAug.class).getCommunityMemberDeny();
        }
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE, "group_name", groupName,
                        "members", members, "deny_members", denyMembers));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String groupName = config.getCommunitySetName();
        List<CommunityMember> members = config.getCommunityMember();
        List<CommunityMemberDeny> denyMembers = null;
        if (config.getAugmentation(CommMembersAug.class) != null) {
            denyMembers = config.getAugmentation(CommMembersAug.class).getCommunityMemberDeny();
        }
        String deleteCommand = fT(DELETE_TEMPLATE, "group_name",
                groupName, "members", members, "deny_members", denyMembers);
        deleteCommand = ParsingUtils.NEWLINE.splitAsStream(deleteCommand)
                .distinct()
                .collect(Collectors.joining("\n"));
        blockingDeleteAndRead(cli, id, deleteCommand);
    }
}
