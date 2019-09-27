/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ios.unit.acl.handler;

import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReaderTest {

    private static final String OUTPUT = "Fri Feb 23 15:25:27.410 UTC\n"
            + "ip access-list ipv4foo\n"
            + " 1 permit ip any any\n"
            + " 10 remark remark1\n"
            + "!";
    private static final AclEntryKey ACL_ENTRY_KEY = new AclEntryKey(1L);

    @Test
    public void testParseAclEntryKey() {
        List<AclEntryKey> result = AclEntryReader.parseAclEntryKey(OUTPUT, ACLIPV4.class);
        Assert.assertEquals(Collections.singletonList(ACL_ENTRY_KEY), result);
    }

    @Test
    public void testParseAclBody() {
        AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        InstanceIdentifier<AclEntry> aclEntryInstanceIdentifier =  IidUtils.createIid(IIDs.AC_AC_AC_AC_ACLENTRY,
                new AclSetKey("ipv4foo", ACLIPV4.class), ACL_ENTRY_KEY);
        AclEntryReader.parseACL(aclEntryInstanceIdentifier, aclEntryBuilder, OUTPUT);

        AclEntry expected = new AclEntryBuilder()
                .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                        .entries.top.acl.entries.acl.entry.ConfigBuilder()
                        .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                        .build())
                .setIpv4(new Ipv4Builder()
                        .setConfig(new ConfigBuilder()
                                .setDestinationAddress(Ipv4Prefix.getDefaultInstance("0.0.0.0/0"))
                                .setSourceAddress(Ipv4Prefix.getDefaultInstance("0.0.0.0/0"))
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
