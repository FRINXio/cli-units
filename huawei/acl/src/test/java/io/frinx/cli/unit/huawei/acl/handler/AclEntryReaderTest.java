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

package io.frinx.cli.unit.huawei.acl.handler;

import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReaderTest {

    private static final String OUTPUT = "Fri Feb 23 15:25:27.410 UTC\n"
            + "acl name MGT-IN 3000\n"
            + " description CPE management access-ingress\n"
            + " step 10\n"
            + " rule 10 permit ip source 172.16.0.111 0 destination-port eq 1985\n"
            + "!";
    private static final AclEntryKey ACL_ENTRY_KEY = new AclEntryKey(10L);

    @Test
    public void testParseAclEntryKey() {
        List<AclEntryKey> result = AclEntryReader.parseAclEntryKey(OUTPUT);
        Assert.assertEquals(Collections.singletonList(ACL_ENTRY_KEY), result);
    }

    @Test
    public void testParseAclBody() {
        AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setKey(ACL_ENTRY_KEY);
        InstanceIdentifier<AclSet> aclEntryInstanceIdentifier =  IidUtils.createIid(IIDs.AC_AC_ACLSET,
                new AclSetKey("MGT-IN", ACLIPV4EXTENDED.class));
        AclEntryReader.parseACL(aclEntryInstanceIdentifier, aclEntryBuilder, OUTPUT);

        AclEntry expected = new AclEntryBuilder()
                .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                        .entries.top.acl.entries.acl.entry.ConfigBuilder()
                        .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                        .build())
                .setIpv4(new Ipv4Builder()
                        .setConfig(new ConfigBuilder()
                                .setSourceAddress(Ipv4Prefix.getDefaultInstance("172.16.0.111/0"))
                                .build())
                        .build())
                .setActions(new ActionsBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action
                                .top.actions.ConfigBuilder()
                                .setForwardingAction(ACCEPT.class)
                                .build())
                        .build())
                .setTransport(AclEntryLineParserTest.defTransport())
                .build();

        Assert.assertEquals(expected, aclEntryBuilder.build());
    }
}
