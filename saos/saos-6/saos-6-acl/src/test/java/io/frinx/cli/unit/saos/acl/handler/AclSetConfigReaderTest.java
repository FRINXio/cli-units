/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.acl.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Saos6AclSetAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.ConfigBuilder;

public class AclSetConfigReaderTest {

    private AclSetConfigReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new AclSetConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    public void parseConfigTest() {
        buildAndTest("foo", ACCEPT.class, null);
        buildAndTest("ACL_TEMPLATE_EVPN_v017", ACCEPT.class, false);
        buildAndTest("CPE_MGT_TEMPLATE_v08", DROP.class, null);
    }

    private void buildAndTest(String aclProfile, Class<? extends FORWARDINGACTION>  expDefaultFwdAction,
                              Boolean expEnabled) {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseConfig(AclSetReaderTest.OUTPUT, builder, aclProfile);

        Assert.assertEquals(aclProfile, builder.getName());
        Assert.assertEquals(ACLIPV4.class, builder.getType());
        Assert.assertEquals(expDefaultFwdAction, builder.getAugmentation(Saos6AclSetAug.class).getDefaultFwdAction());
        Assert.assertEquals(expEnabled ,builder.getAugmentation(Saos6AclSetAug.class).isEnabled());
    }
}
