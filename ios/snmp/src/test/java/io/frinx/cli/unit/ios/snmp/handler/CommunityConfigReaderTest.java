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

package io.frinx.cli.unit.ios.snmp.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.SnmpCommunityConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.community.ConfigBuilder;

public class CommunityConfigReaderTest {

    private static final String OUTPUT = "snmp-server community Foo RO\n";
    private static final String OUTPUT_VIEW = "snmp-server community Bar view SnmpReadAccess RO\n";
    private static final String OUTPUT_VIEW_ACL = "snmp-server community Foobar view SnmpWriteAccess RW 70\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    public void test() {
        CommunityConfigReader.fillInConfig("Foo", OUTPUT, configBuilder);
        Assert.assertEquals("Foo", configBuilder.getName());
        Assert.assertEquals(SnmpCommunityConfig.Access.Ro, configBuilder.getAccess());
        Assert.assertNull(configBuilder.getView());
        Assert.assertNull(configBuilder.getAccessList());
    }

    @Test
    public void testWithView() {
        CommunityConfigReader.fillInConfig("Bar", OUTPUT_VIEW, configBuilder);
        Assert.assertEquals("Bar", configBuilder.getName());
        Assert.assertEquals("SnmpReadAccess", configBuilder.getView());
        Assert.assertEquals(SnmpCommunityConfig.Access.Ro, configBuilder.getAccess());
        Assert.assertNull(configBuilder.getAccessList());
    }

    @Test
    public void testWithViewAndAcl() {
        CommunityConfigReader.fillInConfig("Foobar", OUTPUT_VIEW_ACL, configBuilder);
        Assert.assertEquals("Foobar", configBuilder.getName());
        Assert.assertEquals("SnmpWriteAccess", configBuilder.getView());
        Assert.assertEquals(SnmpCommunityConfig.Access.Rw, configBuilder.getAccess());
        Assert.assertEquals("70", configBuilder.getAccessList());
    }

}