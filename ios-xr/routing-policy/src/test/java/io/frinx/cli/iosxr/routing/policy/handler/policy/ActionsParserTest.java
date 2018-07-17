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

package io.frinx.cli.iosxr.routing.policy.handler.policy;

import static io.frinx.cli.iosxr.routing.policy.handler.policy.StatementsTest.getSetCommInlineAction;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

public class ActionsParserTest {

    public static final String PREPEND_O1 = "prepend as-path 23.344 32";
    public static final String PREPEND_O2 = "prepend as-path 23";
    public static final String PREPEND_O3 = "prepend as-path 23.1";
    public static final String PREPEND_O4 = "prepend as-path 4848484 23";
    public static final String PREPEND_O5 = "prepend as-path 23 2";

    @Test
    public void testParsePrepend() throws Exception {
        BgpActionsBuilder builder = new BgpActionsBuilder();
        ActionsParser.parsePrependAsPath(PREPEND_O2, builder);
        assertEquals(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path
                        .prepend.top.set.as.path.prepend.ConfigBuilder()
                        .setAsn(new AsNumber(23L))
                        .build(),
                builder.build()
                        .getSetAsPathPrepend()
                        .getConfig());

        builder = new BgpActionsBuilder();
        ActionsParser.parsePrependAsPath(PREPEND_O4, builder);
        assertEquals(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path
                        .prepend.top.set.as.path.prepend.ConfigBuilder()
                        .setAsn(new AsNumber(4848484L))
                        .setRepeatN((short) 23)
                        .build(),
                builder.build()
                        .getSetAsPathPrepend()
                        .getConfig());

        builder = new BgpActionsBuilder();
        ActionsParser.parsePrependAsPath(PREPEND_O5, builder);
        assertEquals(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path
                        .prepend.top.set.as.path.prepend.ConfigBuilder()
                        .setAsn(new AsNumber(23L))
                        .setRepeatN((short) 2)
                        .build(),
                builder.build()
                        .getSetAsPathPrepend()
                        .getConfig());
    }

    @Test
    public void testParseCommunity() throws Exception {
        BgpActionsBuilder builder = new BgpActionsBuilder();
        ActionsParser.parseSetCommunity("set community (17676:320, 17676:430, 17676:436)", builder);
        assertEquals(getSetCommInlineAction(Lists.newArrayList("17676:320", "17676:430", "17676:436"), null),
                builder.build()
                        .getSetCommunity());

        builder = new BgpActionsBuilder();
        ActionsParser.parseSetCommunity("set community (17676:320) additive", builder);
        assertEquals(getSetCommInlineAction(Lists.newArrayList("17676:320"), BgpSetCommunityOptionType.ADD),
                builder.build()
                        .getSetCommunity());
    }

}