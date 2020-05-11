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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;

public class AclEntryWriterTest {

    private final AclEntryWriter writer = new AclEntryWriter(Mockito.mock(Cli.class));

    @Test
    public void writeTemplate() {
        Assert.assertEquals(
                "access-list add profile AclSet1 rule test1 precedence 1 filter-action allow any\n"
                        + "configuration save",
                writer.writeTemplate(
                        createAclEntry(new Long("1"), "test1", ACCEPT.class), "AclSet1"));
        Assert.assertEquals(
                "access-list add profile AclSet1 rule test2 precedence 2 filter-action deny any\n"
                        + "configuration save",
                writer.writeTemplate(
                        createAclEntry(new Long("2"), "test2", DROP.class), "AclSet1"));
    }

    @Test
    public void updateTemplate() {
        Assert.assertEquals(
                "access-list set profile AclSet1 rule test1 precedence 1 filter-action deny\n"
                        + "configuration save",
                writer.updateTemplate(
                        createAclEntry(new Long("1"), "test1", ACCEPT.class),
                        createAclEntry(new Long("1"), "test1", DROP.class), "AclSet1"));
    }

    @Test
    public void deleteTemplate() {
        Assert.assertEquals(
                "access-list remove profile AclSet1 rule test1\n"
                        + "configuration save",
                writer.deleteTemplate(
                        createAclEntry(new Long("1"), "test1", ACCEPT.class), "AclSet1"));
    }

    private AclEntry createAclEntry(Long seq, String termName, Class<? extends FORWARDINGACTION> action) {
        return new AclEntryBuilder()
                .setSequenceId(seq)
                .setConfig(new ConfigBuilder()
                        .setSequenceId(seq)
                        .addAugmentation(Config2.class,
                                new Config2Builder()
                                        .setTermName(termName)
                                        .build())
                        .build()
                ).setActions(new ActionsBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action
                                .top.actions.ConfigBuilder()
                                .setForwardingAction(action)
                                .build())
                        .build())
                .build();
    }
}
