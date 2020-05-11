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
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Saos6AclSetAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Saos6AclSetAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.REJECT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.ConfigBuilder;

public class AclSetConfigWriterTest {

    private final AclSetConfigWriter writer = new AclSetConfigWriter(Mockito.mock(Cli.class));

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals(
                "access-list create acl-profile test1 default-filter-action deny\nconfiguration save",
                writer.writeTemplate(createConfig("test1", DROP.class, ACLTYPE.class, null)));
        Assert.assertEquals(
                "access-list create acl-profile test2 default-filter-action allow\nconfiguration save",
                writer.writeTemplate(createConfig("test2", ACCEPT.class, ACLTYPE.class, null)));
        Assert.assertEquals(
                "access-list create acl-profile test3 default-filter-action allow\n"
                + "access-list disable profile test3\nconfiguration save",
                writer.writeTemplate(createConfig("test3", ACCEPT.class, ACLTYPE.class,false)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeTemplateTest_exceptions() {
        // incorrect type (ACLIPV4.class)
        Assert.assertEquals(
                "access-list create acl-profile test4 default-filter-action deny\nconfiguration save",
                writer.writeTemplate(createConfig("test4", DROP.class, ACLIPV4.class, null)));

        // enabled = true
        Assert.assertEquals(
                "",
                writer.writeTemplate(createConfig("test4", DROP.class, ACLTYPE.class, true)));

        // incorrect action (REJECT.class)
        Assert.assertEquals(
                "access-list create acl-profile test4 default-filter-action deny\nconfiguration save",
                writer.writeTemplate(createConfig("test4", REJECT.class, ACLTYPE.class, false)));
    }

    @Test
    public void updateTemplate() {
        Assert.assertEquals("access-list disable profile test5\nconfiguration save", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLTYPE.class, null),
                createConfig("test5", ACCEPT.class, ACLTYPE.class, false)));

        Assert.assertEquals("access-list enable profile test5\nconfiguration save", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLTYPE.class, false),
                createConfig("test5", ACCEPT.class, ACLTYPE.class, true)));

        Assert.assertEquals("access-list enable profile test5\nconfiguration save", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLTYPE.class, false),
                createConfig("test5", ACCEPT.class, ACLTYPE.class, null)));

        Assert.assertEquals("access-list disable profile test5\nconfiguration save", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLTYPE.class, true),
                createConfig("test5", ACCEPT.class, ACLTYPE.class, false)));

        Assert.assertEquals("configuration save", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLTYPE.class, true),
                createConfig("test5", ACCEPT.class, ACLTYPE.class, null)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateTemplate_exceptions() {
        Assert.assertEquals("", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLTYPE.class, null),
                createConfig("test5", ACCEPT.class, ACLTYPE.class, true)));
    }

    @Test
    public void deleteTemplateTest() {
        Assert.assertEquals("access-list delete profile test5\nconfiguration save",
                writer.deleteTemplate(createConfig("test5", ACCEPT.class, ACLTYPE.class, true)));
    }

    private Config createConfig(String name, Class<? extends FORWARDINGACTION> action, Class<? extends ACLTYPE> type,
                                Boolean enable) {
        ConfigBuilder builder = new ConfigBuilder().setName(name).setType(type);
        Saos6AclSetAugBuilder augBuilder = new Saos6AclSetAugBuilder().setDefaultFwdAction(action);

        if (enable != null) {
            augBuilder.setEnabled(enable);
        }

        return builder.addAugmentation(Saos6AclSetAug.class, augBuilder.build()).build();
    }
}
