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

package io.frinx.cli.unit.junos.unit.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.HopRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.IcmpMsgType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.acl.icmp.type.IcmpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;

class AclEntryWriterTest {

    @Test
    void termNameTest() {
        AclEntry entry = getAclEntry(1L, "testTerm", null, null, null, 0, null, null, null, null, null, null, false);
        String termName = AclEntryWriter.getTermName(entry);
        assertEquals("testTerm", termName);

        entry = getAclEntry(1L, null, null, null, null, 0, null, null, null, null, null, null, false);
        termName = AclEntryWriter.getTermName(entry);
        assertEquals("1", termName);

        entry = getAclEntry(1L, "(klasd&!", null, null, null, 0, null, null, null, null, null, null, false);
        termName = AclEntryWriter.getTermName(entry);
        assertEquals("\"(klasd&!\"", termName);

        entry = getAclEntry(1L, "haha hah", null, null, null, 0, null, null, null, null, null, null, false);
        termName = AclEntryWriter.getTermName(entry);
        assertEquals("\"haha hah\"", termName);
    }

    @Test
    void processIpv4() {
        AclEntry entry = getAclEntry(1L, "testTerm", "2.2.2.2/32", "2.2.2.2/32", IPUDP.class, 8, DROP.class,
                new PortNumRange(new PortNumber(9)), new PortNumRange("4..8"), null, null, "2..9", false);
        HashMap<AclEntryWriter.CommandKey, String> commandVars = new HashMap<>();
        AclEntryWriter.processIpv4(entry, commandVars);

        Set<AclEntryWriter.CommandKey> wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_ADDR,
                AclEntryWriter.CommandKey.ACL_ICMP_MSG_TYPE, AclEntryWriter.CommandKey.ACL_TTL,
                AclEntryWriter.CommandKey.ACL_PROTOCOL);
        Set<String> values = Sets.newHashSet("2.2.2.2/32", "8", "udp", "2-9");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));

        entry = getAclEntry(1L, "[(?test", "2.2.2.3/32", "2.2.2.2/32", null, 8, DROP.class,
                null, null, null, null, null, false);
        commandVars = new HashMap<>();
        AclEntryWriter.processIpv4(entry, commandVars);

        wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_SRC_ADDR, AclEntryWriter.CommandKey.ACL_DST_ADDR,
                AclEntryWriter.CommandKey.ACL_ICMP_MSG_TYPE);
        values = Sets.newHashSet("2.2.2.2/32", "2.2.2.3/32", "8");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));
    }

    @Test
    void processTransport() {
        AclEntry entry = getAclEntry(1L, "testTerm", "2.2.2.2/32", "2.2.2.2/32", IPUDP.class, 8, DROP.class,
                new PortNumRange(new PortNumber(9)), new PortNumRange("4..8"), null, null, null, false);
        HashMap<AclEntryWriter.CommandKey, String> commandVars = new HashMap<>();
        AclEntryWriter.processTransport(entry, commandVars);

        Set<AclEntryWriter.CommandKey> wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_SRC_PORT,
                AclEntryWriter.CommandKey.ACL_DST_PORT);
        Set<String> values = Sets.newHashSet("9", "4-8");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));

        entry = getAclEntry(1L, "[(?test", "2.2.2.2/32", "2.2.2.2/32", IPUDP.class, 8, DROP.class, null, null,
                "ftp", "ftp", null, false);
        commandVars = new HashMap<>();
        AclEntryWriter.processTransport(entry, commandVars);

        wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_PORT);
        values = Sets.newHashSet("ftp");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));
    }

    @Test
    void processAction() {
        AclEntry entry = getAclEntry(2L, "testTerm", "2.2.2.2/32", "2.2.2.2/32", IPUDP.class, 8, DROP.class,
                new PortNumRange(new PortNumber(9)), new PortNumRange("4..8"), null, null, null, false);
        HashMap<AclEntryWriter.CommandKey, String> commandVars = new HashMap<>();
        AclEntryWriter.processActions(entry, commandVars);

        Set<AclEntryWriter.CommandKey> wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_ACTION);
        Set<String> values = Sets.newHashSet("discard");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));

        entry = getAclEntry(1L, "testTerm", "2.2.2.2/32", "2.2.2.2/32", IPUDP.class, 8, ACCEPT.class,
                new PortNumRange(new PortNumber(9)), new PortNumRange("4..8"), null, null, null, false);
        commandVars = new HashMap<>();
        AclEntryWriter.processActions(entry, commandVars);

        wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_ACTION);
        values = Sets.newHashSet("accept");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));
    }

    private AclEntry getAclEntry(long id, String termName, String dstAddr, String srcAddr,
                                 Class<? extends IPPROTOCOL> protocol, int icmpMsg,
                                 Class<? extends FORWARDINGACTION> action, PortNumRange dstPort,
                                 PortNumRange srcPort, String dstNamedPort, String srcNamedPort,
                                 String hop, boolean isIpv6) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top
                .transport.ConfigBuilder transport = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();
        if (dstPort != null) {
            transport.setDestinationPort(dstPort);
        }
        if (srcPort != null) {
            transport.setSourcePort(srcPort);
        }
        if (dstNamedPort != null || srcNamedPort != null) {
            AclSetAclEntryTransportPortNamedAugBuilder namedAugBuilder = new
                    AclSetAclEntryTransportPortNamedAugBuilder();
            if (dstNamedPort != null) {
                namedAugBuilder.setDestinationPortNamed(dstNamedPort);
            }
            if (srcNamedPort != null) {
                namedAugBuilder.setSourcePortNamed(srcNamedPort);
            }
            transport.addAugmentation(AclSetAclEntryTransportPortNamedAug.class, namedAugBuilder.build());
        }
        Ipv4 build = null;
        Ipv6 build6 = null;
        if (isIpv6) {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215
                    .ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http
                    .frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            if (hop != null) {
                configBuilder.addAugmentation(Config4.class, new Config4Builder()
                        .setHopRange(new HopRange(hop))
                        .build());
            }
            build6 = new Ipv6Builder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215
                            .ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                            .setDestinationAddress(dstAddr == null ? null : new Ipv6Prefix(dstAddr))
                            .setSourceAddress(srcAddr == null ? null : new Ipv6Prefix(srcAddr))
                            .setProtocol(protocol == null ? null : new IpProtocolType(protocol))
                            .build())
                    .build();
        } else {
            ConfigBuilder configBuilder = new ConfigBuilder();
            if (hop != null) {
                configBuilder.addAugmentation(Config3.class, new Config3Builder()
                        .setHopRange(new HopRange(hop))
                        .build());
            }
            build = new Ipv4Builder()
                    .setConfig(configBuilder
                            .setDestinationAddress(dstAddr == null ? null : new Ipv4Prefix(dstAddr))
                            .setSourceAddress(srcAddr == null ? null : new Ipv4Prefix(srcAddr))
                            .setProtocol(protocol == null ? null : new IpProtocolType(protocol))
                            .build())
                    .build();
        }
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries
                .acl.entry.ConfigBuilder termConfig = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder();
        termConfig.setSequenceId(id);
        if (termName != null) {
            termConfig.addAugmentation(Config2.class, new Config2Builder()
                    .setTermName(termName)
                    .build());
        }
        return new AclEntryBuilder()
                .setConfig(termConfig.build())
                .setKey(new AclEntryKey(id))
                .setIpv4(build)
                .setIpv6(build6)
                .setTransport(new TransportBuilder()
                        .setConfig(transport.build())
                        .build())
                .setActions(new ActionsBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526
                                .action.top.actions.ConfigBuilder()
                                .setForwardingAction(action)
                                .build())
                        .build())
                .addAugmentation(AclEntry1.class, new AclEntry1Builder()
                        .setIcmp(new IcmpBuilder()
                                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext
                                        .rev180314.acl.icmp.type.icmp.ConfigBuilder()
                                        .setMsgType(new IcmpMsgType((short) icmpMsg))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    void processIpv6() {
        AclEntry entry = getAclEntry(1L, "testTerm", "f::2/128", "f::2/128", IPUDP.class, 128, DROP.class,
                null, null, null, null, null, true);
        HashMap<AclEntryWriter.CommandKey, String> commandVars = new HashMap<>();
        AclEntryWriter.processIpv6(entry, commandVars);

        Set<AclEntryWriter.CommandKey> wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_ADDR,
                AclEntryWriter.CommandKey.ACL_ICMP_MSG_TYPE,
                AclEntryWriter.CommandKey.ACL_PROTOCOL_IPV6);
        Set<String> values = Sets.newHashSet("f::2/128", "128", "udp");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));

        entry = getAclEntry(1L, "testTerm", "f::2/128", "f::8/128", IPUDP.class, 128, DROP.class,
                null, null, null, null, null, true);
        commandVars = new HashMap<>();
        AclEntryWriter.processIpv6(entry, commandVars);

        wanted = Sets.newHashSet(AclEntryWriter.CommandKey.ACL_SRC_ADDR, AclEntryWriter.CommandKey.ACL_DST_ADDR,
                AclEntryWriter.CommandKey.ACL_ICMP_MSG_TYPE,
                AclEntryWriter.CommandKey.ACL_PROTOCOL_IPV6);
        values = Sets.newHashSet("f::2/128", "f::8/128", "128", "udp");
        assertEquals(wanted, commandVars.keySet());
        assertEquals(values, Sets.newHashSet(commandVars.values()));
    }
}