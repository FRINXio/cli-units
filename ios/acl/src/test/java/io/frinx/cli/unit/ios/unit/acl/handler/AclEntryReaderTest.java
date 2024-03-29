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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class AclEntryReaderTest {

    private static final String OUTPUT = """
            Fri Feb 23 15:25:27.410 UTC
            Extended IP access list ipv4foo
             1 permit ip any any
             10 remark remark1
            !""";

    private static final String OUTPUT_IPV6 = """
            ipv6 access-list ipv6foo
             sequence 1 permit ipv6 any any
            !""";

    private static final AclEntryKey ACL_ENTRY_KEY = new AclEntryKey(1L);

    @Test
    void testParseAclEntryKey() {
        List<AclEntryKey> result = AclEntryReader.parseAclEntryKey(OUTPUT, ACLIPV4EXTENDED.class);
        assertEquals(Collections.singletonList(ACL_ENTRY_KEY), result);
    }

    @Test
    void testParseAclEntryKeyIpv6() {
        List<AclEntryKey> result = AclEntryReader.parseAclEntryKey(OUTPUT_IPV6, ACLIPV6.class);
        assertEquals(Collections.singletonList(ACL_ENTRY_KEY), result);
    }

    @Test
    void testParseAclBody() {
        AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setKey(ACL_ENTRY_KEY);
        InstanceIdentifier<AclSet> aclEntryInstanceIdentifier =  IidUtils.createIid(IIDs.AC_AC_ACLSET,
                new AclSetKey("ipv4foo", ACLIPV4EXTENDED.class));
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
                .setTransport(AclEntryLineParserTest.defTransport(null))
                .build();

        assertEquals(expected, aclEntryBuilder.build());
    }

    @Test
    void testParseAclBodyIpv6() {
        AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setKey(ACL_ENTRY_KEY);
        InstanceIdentifier<AclSet> aclEntryInstanceIdentifier =  IidUtils.createIid(IIDs.AC_AC_ACLSET,
                new AclSetKey("ipv6foo", ACLIPV6.class));
        AclEntryReader.parseACL(aclEntryInstanceIdentifier, aclEntryBuilder, OUTPUT_IPV6);

        AclEntry expected = new AclEntryBuilder()
                .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                        .entries.top.acl.entries.acl.entry.ConfigBuilder()
                        .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                        .build())
                .setIpv6(new Ipv6Builder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(Ipv6Prefix.getDefaultInstance("::/0"))
                                .setSourceAddress(Ipv6Prefix.getDefaultInstance("::/0"))
                                .build())
                        .build())
                .setActions(new ActionsBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action
                                .top.actions.ConfigBuilder()
                                .setForwardingAction(ACCEPT.class)
                                .build())
                        .build())
                .setTransport(AclEntryLineParserTest.defTransport(null))
                .build();

        assertEquals(expected, aclEntryBuilder.build());
    }
}
