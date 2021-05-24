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

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.bgp.IIDs;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommunityMembers.CommunityMemberDeny;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.CommunitySetConfig.CommunityMember;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpCommunityRegexpType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityTypeString;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunityListConfigWriterTest {

    private static final String FIRST_STANDARD_CMD = "configure terminal\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE permit 65222:999\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE permit 65222:888\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE deny 65333:111\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE deny 65333:222\n"
            + "end\n";

    private static final String FIRST_EXTENDED_CMD = "configure terminal\n"
            + "ip community-list expanded COM_BLACKHOLE permit .*:666\n"
            + "ip community-list expanded COM_BLACKHOLE permit .*:888\n"
            + "ip community-list expanded COM_BLACKHOLE deny .*:222\n"
            + "ip community-list expanded COM_BLACKHOLE deny .*:111\n"
            + "end\n";

    private static final String DELETE_STANDARD_CMD = "configure terminal\n"
            + "no ip community-list standard COM_NO_EXPORT_TO_PE\n"
            + "end\n";

    private static final String DELETE_EXTEND_CMD = "configure terminal\n"
            + "no ip community-list expanded COM_BLACKHOLE\n"
            + "end\n";
    @Mock
    private Cli cli;

    private final InstanceIdentifier<Config> iid = IIDs.RO_DE_AUG_DEFINEDSETS2_BG_CO_CO_CONFIG;

    private CommunityListConfigWriter writer;

    private Config createStandardConfig(String groupName, String... communityValue) {
        List<CommunityMember> communityMembers = getCommunityMembersList(0, 2,
            value -> new CommunityMember(new BgpStdCommunityType(
                    BgpStdCommunityTypeString.getDefaultInstance(communityValue[value]))));

        List<CommunityMemberDeny> communityMemberDeny = getCommunityMembersList(2, 4,
            value -> new CommunityMemberDeny(new BgpStdCommunityType(
                    BgpStdCommunityTypeString.getDefaultInstance(communityValue[value]))));

        return new ConfigBuilder().setCommunitySetName(groupName).addAugmentation(CommMembersAug.class,
                new CommMembersAugBuilder().setCommunityMemberDeny(communityMemberDeny).build())
                .setCommunityMember(communityMembers)
                .build();
    }

    private Config createExpandedConfig(String groupName, String... communityValue) {
        List<CommunityMember> communityMembers = getCommunityMembersList(0, 2,
            value -> new CommunityMember(new BgpCommunityRegexpType(communityValue[value])));

        List<CommunityMemberDeny> communityMemberDeny = getCommunityMembersList(2, 4,
            value -> new CommunityMemberDeny(new BgpCommunityRegexpType(communityValue[value])));

        return new ConfigBuilder().setCommunitySetName(groupName).addAugmentation(CommMembersAug.class,
                new CommMembersAugBuilder().setCommunityMemberDeny(communityMemberDeny).build())
                .setCommunityMember(communityMembers)
                .build();
    }

    private <T> List<T> getCommunityMembersList(int rangeMin, int rangeMax, IntFunction<T> mapper) {
        return IntStream
                .range(rangeMin, rangeMax)
                .mapToObj(mapper)
                .collect(Collectors.toList());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CommunityListConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesTest_Standard() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, createStandardConfig("COM_NO_EXPORT_TO_PE",
                "65222:999", "65222:888", "65333:111", "65333:222"), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(FIRST_STANDARD_CMD));
    }

    @Test
    public void writeCurrentAttributesTest_Extend() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, createExpandedConfig("COM_BLACKHOLE",
                ".*:666", ".*:888", ".*:222", ".*:111"), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(FIRST_EXTENDED_CMD));
    }

    @Test
    public void deleteCurrentAttributesTestStandard() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, createStandardConfig("COM_NO_EXPORT_TO_PE",
                "65222:888", "65222:999", "65333:111", "65333:222"), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_STANDARD_CMD));
    }

    @Test
    public void deleteCurrentAttributesTestExtended() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, createExpandedConfig("COM_BLACKHOLE",
                ".*:555", "_4_", ".*:222", "_5_"), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_EXTEND_CMD));
    }
}