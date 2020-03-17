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

package io.frinx.cli.unit.cubro.unit.acl.handler;

import com.google.common.collect.Lists;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.AclCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.AclCubroAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.COUNT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.ELAG;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.IPANY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReaderTest {

    private static final AclEntryKey ACL_ENTRY_KEY = new AclEntryKey(2000L);

    @Test
    public void testParseAclEntryKey() {
        AclSetKey aclSetKey = new AclSetKey("acl2", ACLIPV4.class);
        List<AclEntryKey> result = AclEntryReader.parseAclEntryKey(AclInterfaceReaderTest.OUTPUT, aclSetKey);
        Assert.assertEquals(Lists.newArrayList(
                ACL_ENTRY_KEY,
                new AclEntryKey(2004L)),
                result);
    }

    @Test
    public void testParseAclBody() {
        AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setKey(ACL_ENTRY_KEY);
        InstanceIdentifier<AclSet> aclEntryInstanceIdentifier =  IidUtils.createIid(IIDs.AC_AC_ACLSET,
                new AclSetKey("acl2", ACLIPV4.class));
        AclEntryReader.parseACL(aclEntryInstanceIdentifier, aclEntryBuilder, AclInterfaceReaderTest.OUTPUT);

        // AclEntry -> 2000 forward elag 10 any 10.0.0.1/255.0.0.0 any count"
        AclEntry expected = new AclEntryBuilder()
                .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                        .entries.top.acl.entries.acl.entry.ConfigBuilder()
                        .setSequenceId(ACL_ENTRY_KEY.getSequenceId())
                        .build())
                .setActions(new ActionsBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action
                                .top.actions.ConfigBuilder()
                                .setForwardingAction(ACCEPT.class)
                                .addAugmentation(AclCubroAug.class, new AclCubroAugBuilder()
                                        .setEgressValue(10L)
                                        .setEgressType(ELAG.class)
                                        .setOperation(COUNT.class)
                                        .build())
                                .build())
                        .build())
                .setIpv4(new Ipv4Builder()
                        .setConfig(new ConfigBuilder()
                                .addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                                        new AclSetAclEntryIpv4WildcardedAugBuilder()
                                        .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                                                .setAddress(new Ipv4Address("10.0.0.1"))
                                                .setWildcardMask(new Ipv4Address("255.0.0.0"))
                                                .build())
                                        .build())
                                .setDestinationAddress(Ipv4Prefix.getDefaultInstance("0.0.0.0/0"))
                                .setProtocol(new IpProtocolType(IPANY.class))
                                .build())
                        .build())
                .build();

        Assert.assertEquals(expected, aclEntryBuilder.build());
    }
}