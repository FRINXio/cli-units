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

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommMembersAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.CommunityMembers.CommunityMemberDeny;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.CommunitySetConfig.CommunityMember;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.community.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpCommunityRegexpType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityTypeString;

public class CommunityListConfigReaderTest {

    private static final String CMD = "configure terminal\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE permit 65222:999\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE permit 65222:888\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE deny 65333:123\n"
            + "ip community-list standard COM_NO_EXPORT_TO_PE deny 65222:777\n"
            + "ip community-list standard COM_ART_AK permit 68888:111\n"
            + "ip community-list expanded COM_BLACKHOLE permit .*:666\n"
            + "ip community-list expanded COM_BLACKHOLE permit .*:888\n"
            + "ip community-list expanded COM_BLACKHOLE deny .*:777\n"
            + "ip community-list expanded COM_BLACKHOLE deny .*:999\n"
            + "ip community-list expanded COM_BLACK permit 233\n"
            + "end\n";

    ConfigBuilder builder;
    ConfigBuilder expectedBuilder;
    CommMembersAugBuilder commListBuilder;

    @Before
    public void setUp() {
        builder = new ConfigBuilder();
        expectedBuilder = new ConfigBuilder();
        commListBuilder = new CommMembersAugBuilder();
    }

    @Test
    public void parseConfigTest_Standard() {
        expectedBuilder.setCommunityMember(Arrays.asList(
                new CommunityMember(new BgpStdCommunityType(
                        BgpStdCommunityTypeString.getDefaultInstance("65222:999"))),
                new CommunityMember(new BgpStdCommunityType(
                        BgpStdCommunityTypeString.getDefaultInstance("65222:888")))));
        commListBuilder.setCommunityMemberDeny(Arrays.asList(
                new CommunityMemberDeny(new BgpStdCommunityType(
                        BgpStdCommunityTypeString.getDefaultInstance("65333:123"))),
                new CommunityMemberDeny(new BgpStdCommunityType(
                        BgpStdCommunityTypeString.getDefaultInstance("65222:777")))));
        expectedBuilder.addAugmentation(CommMembersAug.class, commListBuilder.build());
        CommunityListConfigReader.parseCommunityConfig(CMD, builder, "COM_NO_EXPORT_TO_PE");
        Assert.assertNull(builder.getCommunityMember().get(0).getBgpCommunityRegexpType());
        Assert.assertNotNull(builder.getCommunityMember().get(0).getBgpStdCommunityType());
        Assert.assertEquals("COM_NO_EXPORT_TO_PE", builder.getCommunitySetName());
        Assert.assertEquals(expectedBuilder.getCommunityMember(), builder.getCommunityMember());
        Assert.assertEquals(expectedBuilder.getAugmentation(CommMembersAug.class).getCommunityMemberDeny(),
                builder.getAugmentation(CommMembersAug.class).getCommunityMemberDeny());
    }

    @Test
    public void parseConfigTest_Expanded() {
        expectedBuilder.setCommunityMember(Arrays.asList(
                new CommunityMember(new BgpCommunityRegexpType(".*:666")),
                new CommunityMember(new BgpCommunityRegexpType(".*:888"))));
        commListBuilder.setCommunityMemberDeny(Arrays.asList(
                new CommunityMemberDeny(new BgpCommunityRegexpType("*:777")),
                new CommunityMemberDeny(new BgpCommunityRegexpType("*:999"))));
        CommunityListConfigReader.parseCommunityConfig(CMD, builder, "COM_BLACKHOLE");
        Assert.assertNull(builder.getCommunityMember().get(0).getBgpStdCommunityType());
        Assert.assertNotNull(builder.getCommunityMember().get(0).getBgpCommunityRegexpType());
        Assert.assertEquals("COM_BLACKHOLE", builder.getCommunitySetName());
        Assert.assertEquals(expectedBuilder.getCommunityMember(), builder.getCommunityMember());
    }
}