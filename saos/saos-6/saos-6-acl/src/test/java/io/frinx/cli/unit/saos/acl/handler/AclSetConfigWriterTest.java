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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
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

class AclSetConfigWriterTest {

    private final AclSetConfigWriter writer = new AclSetConfigWriter(Mockito.mock(Cli.class));

    @Test
    void writeTemplateTest() {
        assertEquals(
                "access-list create acl-profile test1 default-filter-action deny\n",
                writer.writeTemplate(createConfig("test1", DROP.class, ACLIPV4.class, null)));
        assertEquals(
                "access-list create acl-profile test2 default-filter-action allow\n",
                writer.writeTemplate(createConfig("test2", ACCEPT.class, ACLIPV4.class, true)));
        assertEquals(
                "access-list create acl-profile test3 default-filter-action allow\n"
                + "access-list disable profile test3",
                writer.writeTemplate(createConfig("test3", ACCEPT.class, ACLIPV4.class,false)));
    }

    @Test
    void writeTemplateTest_exceptions() {
        assertThrows(IllegalArgumentException.class, () -> {
            // incorrect type (ACLTYPE.class)
            assertEquals("",
                    writer.writeTemplate(createConfig("test4", DROP.class, ACLTYPE.class, null)));

            // incorrect action (REJECT.class)
            assertEquals("",
                    writer.writeTemplate(createConfig("test4", REJECT.class, ACLIPV4.class, false)));
        });
    }

    @Test
    void updateTemplate() {
        assertEquals("access-list disable profile test5", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLIPV4.class, null),
                createConfig("test5", ACCEPT.class, ACLIPV4.class, false)));

        assertEquals("access-list enable profile test5", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLIPV4.class, false),
                createConfig("test5", ACCEPT.class, ACLIPV4.class, true)));

        assertEquals("access-list enable profile test5", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLIPV4.class, false),
                createConfig("test5", ACCEPT.class, ACLIPV4.class, null)));

        assertEquals("access-list disable profile test5", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLIPV4.class, true),
                createConfig("test5", ACCEPT.class, ACLIPV4.class, false)));

        assertEquals("", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLIPV4.class, true),
                createConfig("test5", ACCEPT.class, ACLIPV4.class, null)));

        assertEquals("", writer.updateTemplate(
                createConfig("test5", ACCEPT.class, ACLIPV4.class, null),
                createConfig("test5", ACCEPT.class, ACLIPV4.class, true)));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("access-list delete profile test5",
                writer.deleteTemplate(createConfig("test5", ACCEPT.class, ACLIPV4.class, true)));
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
