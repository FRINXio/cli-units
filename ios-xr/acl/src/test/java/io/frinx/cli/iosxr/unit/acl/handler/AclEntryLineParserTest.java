/*
 * Copyright © 2018 Frinx and others.
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
package io.frinx.cli.iosxr.unit.acl.handler;

import static io.frinx.cli.iosxr.unit.acl.handler.AclEntryLineParser.IPV4_HOST_ANY;
import static io.frinx.cli.iosxr.unit.acl.handler.AclEntryLineParser.IPV6_HOST_ANY;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.HopRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;

public class AclEntryLineParserTest {

    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, null, transport);

    }

    static AclEntry createIpv6AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, null, ipv6Config, transport);
    }

    static AclEntry createAclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                   org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                   org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                   Transport transport) {

        Actions actions = AclEntryLineParser.createActions(fwdAction);
        AclEntryBuilder builder = new AclEntryBuilder();
        // sequence id
        builder.setSequenceId(sequenceId);
        // fwd action
        builder.setActions(actions);
        builder.setTransport(transport);
        if (ipv4Config != null) {
            // ipv4
            builder.setIpv4(new Ipv4Builder().setConfig(ipv4Config).build());
        } else {
            // ipv6
            builder.setIpv6(new Ipv6Builder().setConfig(ipv6Config).build());
        }
        return builder.build();
    }


    @Test
    public void testIpv4() {
        String lines = "ipv4 access-list foo\n" +
                " 2 deny ipv4 host 1.2.3.4 any\n" +
                " 3 permit udp 192.168.1.1/24 10.10.10.10/24\n" +
                " 4 permit tcp host 1.2.3.4 eq www any\n" +
                " 5 deny icmp host 1.1.1.1 host 2.2.2.2 ttl range 0 10\n" +
                " 13 permit tcp host 1.1.1.1 range 1024 65535 host 2.2.2.2 range 0 1023\n" +
                " 14 permit ipv4 any any ttl gt 12\n" +
                " 15 permit udp any neq 80 any ttl neq 10\n" +
                " 26 permit icmp any any echo\n" +
                "!\n";
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();
        {
            // 2 deny ipv4 host 1.2.3.4 any
            long sequenceId = 2;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPPROTOCOL.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.2.3.4/32"));
            configBuilder.setDestinationAddress(IPV4_HOST_ANY);

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }
        {
            // 3 permit udp 192.168.1.1/24 10.10.10.10/24
            long sequenceId = 3;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("192.168.1.1/24"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("10.10.10.10/24"));

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 4 permit tcp host 1.2.3.4 eq www any
            long sequenceId = 4;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.2.3.4/32"));
            configBuilder.setDestinationAddress(IPV4_HOST_ANY);
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(new PortNumber(80)))
                            .build());

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), transportBuilder.build()));
        }
        {
            // 5 deny icmp host 1.1.1.1 host 2.2.2.2 ttl range 0 10
            long sequenceId = 5;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            configBuilder.setHopLimit((short) 10);
            configBuilder.addAugmentation(Config1.class, new Config1Builder().setHopRange(new HopRange("0..10")).build());

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }
        {
            // 13 permit tcp host 1.1.1.1 range 1024 65535 host 2.2.2.2 range 0 1023
            long sequenceId = 13;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("1024..65535"))
                            .setDestinationPort(new PortNumRange("0..1023"))
                            .build());
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), transportBuilder.build()));
        }
        {
            // 14 permit ipv4 any any ttl gt 12
            long sequenceId = 14;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPPROTOCOL.class));
            configBuilder.setSourceAddress(IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config1.class, new Config1Builder().setHopRange(new HopRange("13..255")).build());
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 15 permit udp any neq 80 any ttl neq 10
            long sequenceId = 15;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config1.class, new Config1Builder().setHopRange(new HopRange("11..9")).build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("81..79"))
                            .build());

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), transportBuilder.build()));
        }
        {
            // 26 permit icmp any any echo
            long sequenceId = 26;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config1.class, new Config1Builder().setIcmpMessageType((short) 8).build());

            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findLineWithSequenceId(sequenceId, lines).get();
            AclEntry parsed = AclEntryLineParser.parseLine(line, IPV4.class);
            assertEquals(entry.getValue(), parsed);
        }
    }

    @Test
    public void testFindLineWithSequenceId() {
        String lines = "a\n" +
                " 1 foo\n" +
                " 2 bar baz\n" +
                "xxx";
        assertEquals(Optional.of("1 foo"), AclEntryLineParser.findLineWithSequenceId(1l, lines));
        assertEquals(Optional.of("2 bar baz"), AclEntryLineParser.findLineWithSequenceId(2l, lines));
        assertEquals(Optional.empty(), AclEntryLineParser.findLineWithSequenceId(3l, lines));
    }

    @Test
    public void testIpv6() {
        String lines = "ipv6 access-list foo\n" +
                " 1 permit ipv6 any any\n" +
                " 3 permit icmpv6 any any\n" +
                " 4 deny ipv6 2001:db8:a0b:12f0::1/55 any\n" +
                " 5 permit tcp host ::1 host ::1\n" +
                " 6 permit tcp host ::1 host ::1 lt www ttl eq 10\n" +
                " 7 permit icmpv6 any host ::1 8 ttl neq 10\n" +
                " 11 remark foo\n" +
                " 21 remark bar\n" +
                " 31 remark baz2\n" +
                "!\n";
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // 1 permit ipv6 any any
            long sequenceId = 1;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPPROTOCOL.class));
            configBuilder.setSourceAddress(IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(IPV6_HOST_ANY);

            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 3 permit icmpv6 any any
            long sequenceId = 3;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(IPV6_HOST_ANY);

            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 4 deny ipv6 2001:db8:a0b:12f0::1/55 any
            long sequenceId = 4;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPPROTOCOL.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("2001:db8:a0b:12f0::1/55"));
            configBuilder.setDestinationAddress(IPV6_HOST_ANY);

            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }
        {
            // 5 permit tcp host ::1 host ::1
            long sequenceId = 5;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::1/128"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));

            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 6 permit tcp host ::1 host ::1 lt www ttl eq 10
            long sequenceId = 6;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::1/128"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));

            configBuilder.addAugmentation(Config2.class, new Config2Builder().setHopRange(new HopRange("10..10")).build());

            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setDestinationPort(new PortNumRange("0..80"))
                            .build());

            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), transportBuilder.build()));
        }
        {
            // 7 permit icmpv6 any host ::1 8 ttl neq 10
            long sequenceId = 7;
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));
            configBuilder.addAugmentation(Config2.class, new Config2Builder()
                            .setHopRange(new HopRange("11..9"))
                            .setIcmpMessageType((short) 8)
                            .build());
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findLineWithSequenceId(sequenceId, lines).get();
            AclEntry parsed = AclEntryLineParser.parseLine(line, IPV6.class);
            assertEquals(entry.getValue(), parsed);
        }

    }

}
