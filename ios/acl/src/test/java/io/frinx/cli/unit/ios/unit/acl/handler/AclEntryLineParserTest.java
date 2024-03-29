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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEstablishedStateAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEstablishedStateAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclOptionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclOptionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclPrecedenceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclPrecedenceAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetOption;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetPrecedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.HopRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.IcmpMsgType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.acl.icmp.type.IcmpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange.Enumeration;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;

class AclEntryLineParserTest {

    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport,
                                       Short icmpMessageType) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, null, transport, icmpMessageType, null, null);
    }

    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, null, transport, null, null, null);
    }

    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport, AclPrecedenceAug precedenceAug,
                                       AclOptionAug optionAug) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, null, transport, null,
                precedenceAug, optionAug);
    }

    static AclEntry createIpv6AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport,
                                       Short icmpMessageType) {
        return createAclEntry(sequenceId, fwdAction, null, ipv6Config, transport, icmpMessageType, null, null);
    }

    static AclEntry createIpv6AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, null, ipv6Config, transport, null, null, null);
    }

    static AclEntry createAclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                   Config ipv4Config,
                                   org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                           .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                   Transport transport, final Short icmpMessageType,
                                   final AclPrecedenceAug precedenceAug,
                                   final AclOptionAug optionAug) {

        Actions actions = AclEntryLineParser.createActions(fwdAction);
        AclEntryBuilder builder = new AclEntryBuilder();
        // sequence id
        builder.setSequenceId(sequenceId);
        builder.setConfig(new ConfigBuilder()
                .setSequenceId(sequenceId)
                .build()
        );
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
        if (icmpMessageType != null) {
            builder.addAugmentation(AclEntry1.class, new AclEntry1Builder()
                    .setIcmp(new IcmpBuilder()
                            .setConfig(
                                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext
                                            .rev180314.acl.icmp.type.icmp.ConfigBuilder()
                                            .setMsgType(new IcmpMsgType(icmpMessageType))
                                            .build()
                            ).build()
                    ).build()
            );
        }
        if (precedenceAug != null) {
            builder.addAugmentation(AclPrecedenceAug.class, precedenceAug);
        }
        if (optionAug != null) {
            builder.addAugmentation(AclOptionAug.class, optionAug);
        }
        return builder.build();
    }

    static Transport defTransport(Boolean established) {
        TransportBuilder transportBuilder = new TransportBuilder();
        if (established != null && established.equals(true)) {
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .addAugmentation(AclEstablishedStateAug.class, new AclEstablishedStateAugBuilder()
                                    .setEstablished(true)
                                    .build())
                            .build());
        } else if (established != null && established.equals(false)) {
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .addAugmentation(AclEstablishedStateAug.class, new AclEstablishedStateAugBuilder()
                                    .setEstablished(false)
                                    .build())
                            .build());
        } else {
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
        }
        return transportBuilder.build();
    }

    @Test
    void testIpv4Standard() {
        final String lines = """
                Standard IP access list 70
                    10 permit 172.31.132.0, wildcard bits 0.0.3.255
                    20 permit 192.12.2.1
                    30 deny   any

                """;
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // 10 permit 172.31.132.0, wildcard bits 0.0.3.255
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                    new AclSetAclEntryIpv4WildcardedAugBuilder()
                            .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                                    .setAddress(new Ipv4Address("172.31.132.0"))
                                    .setWildcardMask(new Ipv4Address("0.0.3.255"))
                                    .build())
                            .build());
            long sequenceId = 10;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 20 permit 192.12.2.1
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv4Prefix("192.12.2.1/32"));
            long sequenceId = 20;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // 30 deny   any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv4Prefix("0.0.0.0/0"));
            long sequenceId = 30;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(), null));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findIpv4LineWithSequenceId(sequenceId, lines).get();
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4STANDARD.class);
            assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

    @Test
    void testIpv4Extended() {
        final String lines = """
                Mon May 14 14:36:55.408 UTC
                Extended IP access list foo
                 2 deny ip host 0.0.0.0 host 0.0.0.0 established
                 3 permit udp 192.168.1.1 0.0.0.255 10.10.10.10 0.0.0.255
                 4 permit tcp host 1.2.3.4 eq www any
                 5 deny icmp host 1.1.1.1 host 2.2.2.2 ttl range 0 10
                 6 permit udp 0.0.0.0 0.255.255.255 eq 10 0.0.0.0 0.255.255.255 gt 10
                 7 permit udp host 1.1.1.1 gt 10 0.0.0.0 0.255.128.255 lt 10
                 8 permit udp 0.0.0.0 0.255.0.255 lt 10 any range 10 10
                 13 permit tcp host 1.1.1.1 range 1024 65535 host 2.2.2.2 range 0 1023
                 14 permit ip any any ttl gt 12
                 15 permit udp any neq 80 any ttl neq 10
                 26 permit icmp any any router-solicitation
                 30 deny udp 10.10.99.0 0.0.0.254 neq 545 bootpc any
                 33 deny udp 10.10.99.0 0.0.0.254 eq 545 bootpc any\s
                 38 deny tcp any any precedence internet\s
                 42 deny ip any any option record-route\s
                !
                """;
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // 2 deny ip host 0.0.0.0 host 0.0.0.0 established
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv4Prefix("0.0.0.0/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("0.0.0.0/32"));
            long sequenceId = 2;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    defTransport(true)));
        }
        {
            // 3 permit udp 192.168.1.1 0.0.0.255 10.10.10.10 0.0.0.255
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("192.168.1.1"))
                            .setWildcardMask(new Ipv4Address("0.0.0.255"))
                            .build())
                    .setDestinationAddressWildcarded((new DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("10.10.10.10"))
                            .setWildcardMask(new Ipv4Address("0.0.0.255"))
                            .build()))
                    .build());
            long sequenceId = 3;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    defTransport(null)));
        }
        {
            // 4 permit tcp host 1.2.3.4 eq www any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.2.3.4/32"));
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("www")
                                    .build())
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .addAugmentation(AclEstablishedStateAug.class, new AclEstablishedStateAugBuilder()
                                    .setEstablished(false)
                                    .build())
                            .build());
            long sequenceId = 4;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 5 deny icmp host 1.1.1.1 host 2.2.2.2 ttl range 0 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("0..10"))
                    .build());
            long sequenceId = 5;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    defTransport(null)));
        }
        {
            // 6 permit udp 0.0.0.0 0.255.255.255 eq 10 0.0.0.0 0.255.255.255 gt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.255.255"))
                            .build())
                    .setDestinationAddressWildcarded((new DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.255.255"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(new PortNumber(10)))
                            .setDestinationPort(new PortNumRange("10..65535"))
                            .build());
            long sequenceId = 6;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 7 permit udp host 1.1.1.1 gt 10 0.0.0.0 0.255.128.255 lt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setDestinationAddressWildcarded((new DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.128.255"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("10..65535"))
                            .setDestinationPort(new PortNumRange("0..10"))
                            .build());
            long sequenceId = 7;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 8 permit udp 0.0.0.0 0.255.0.255 lt 10 any range 10 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setDestinationAddress((new Ipv4Prefix("0.0.0.0/0")));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded((new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("0.0.0.0"))
                            .setWildcardMask(new Ipv4Address("0.255.0.255"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("0..10"))
                            .setDestinationPort(new PortNumRange("10..10"))
                            .build());
            long sequenceId = 8;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 13 permit tcp host 1.1.1.1 range 1024 65535 host 2.2.2.2 range 0 1023
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("1024..65535"))
                            .setDestinationPort(new PortNumRange("0..1023"))
                            .addAugmentation(AclEstablishedStateAug.class, new AclEstablishedStateAugBuilder()
                                    .setEstablished(false)
                                    .build())
                            .build());
            long sequenceId = 13;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 14 permit ipv4 any any ttl gt 12
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("13..255"))
                    .build());
            long sequenceId = 14;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    defTransport(null)));
        }
        {
            // 15 permit udp any neq 80 any ttl neq 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("11..9"))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("81..79"))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            long sequenceId = 15;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 26 permit icmp any any router-solicitation
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            long sequenceId = 26;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    defTransport(null), (short) 10));
        }
        {
            // 30 deny udp 10.10.99.0 0.0.0.254 neq 545 bootpc any
            var configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215
                    .ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("10.10.99.0"))
                            .setWildcardMask(new Ipv4Address("0.0.0.254"))
                            .build())
                    .build());
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            var transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("0..67 69..544 546..65535")
                                    .build())
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            var sequenceId = 30L;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 33 deny udp 10.10.99.0 0.0.0.254 eq 545 bootpc any
            var configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215
                    .ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, new
                    AclSetAclEntryIpv4WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv4Address("10.10.99.0"))
                            .setWildcardMask(new Ipv4Address("0.0.0.254"))
                            .build())
                    .build());
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            var transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("545 68")
                                    .build())
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            var sequenceId = 33L;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 38 deny tcp any any precedence internet
            var configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215
                    .ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            var precedenceAug = new AclPrecedenceAugBuilder()
                    .setPrecedence(AclSetPrecedence.Precedence.INTERNET).build();
            var sequenceId = 38L;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    defTransport(false), precedenceAug, null));
        }
        {
            // 42 deny ip any any option record-route
            var configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215
                    .ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
            var optionAug = new AclOptionAugBuilder()
                    .setOption(new AclSetOption.Option(AclSetOption.Option.Enumeration.RECORDROUTE)).build();
            var sequenceId = 42L;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    defTransport(null), null, optionAug));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findIpv4LineWithSequenceId(sequenceId, lines).get();
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4EXTENDED.class);
            assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

    @Test
    void parseAclLineWithUnsupportedProtocolTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            final String line = "10 deny esp host 1.2.3.4 host 5.6.7.8";
            final AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4EXTENDED.class);
        });
    }

    @Test
    void testIpv4WithUnsupportedOption() {
        assertThrows(IllegalArgumentException.class, () -> {
            final String line = "permit tcp host 1.1.1.1 host 2.2.2.2 eq 69 established";
            final AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4EXTENDED.class);
        });
    }

    @Test
    void testFindLineWithSequenceId() {
        String lines = """
                a
                 1 foo
                 2 bar baz
                xxx""";
        assertEquals(Optional.of("1 foo"), AclEntryLineParser.findIpv4LineWithSequenceId(1L, lines));
        assertEquals(Optional.of("2 bar baz"), AclEntryLineParser.findIpv4LineWithSequenceId(2L, lines));
        assertEquals(Optional.empty(), AclEntryLineParser.findIpv4LineWithSequenceId(3L, lines));
    }

    @Test
    void testIpv6() {
        @SuppressWarnings("checkstyle:linelength")
        final String lines = """
                ipv6 access list foo
                 sequence 1 permit ipv6 any any
                 sequence 3 permit icmpv6 any any router-solicitation
                 sequence 4 deny ipv6 2001:db8:a0b:12f0::1/55 any
                 sequence 5 permit tcp host ::1 host ::1
                 sequence 6 permit tcp host ::1 host ::1 lt www ttl eq 10
                 sequence 7 permit icmpv6 any host ::1 8 ttl neq 10
                 sequence 8 permit udp f::a a::b eq 10 fe80:0000:0000:0000:0202:b3ff:fe1e:8329 fe80:0000:0000:0000:0202:b3ff:fe1e:8329 gt 10
                 sequence 9 permit udp host fe80:0000:0000:0000:0202:b3ff:fe1e:8329 gt 10 f::a a::b lt 10
                 sequence 10 permit udp f::a a::b lt 10 any range 10 10
                 sequence 11 remark foo
                 sequence 21 remark bar
                 sequence 31 remark baz2
                 sequence 50 deny ipv6 any any
                !
                """;
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // 1 permit ipv6 any any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 1;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    defTransport(null)));
        }
        {
            // 3 permit icmpv6 any any router-solicitation
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 3;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    defTransport(null), (short) 133));
        }
        {
            // 4 deny ipv6 2001:db8:a0b:12f0::1/55 any
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv6Prefix("2001:db8:a0b:12f0::1/55"));
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 4;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    defTransport(null)));
        }
        {
            // 5 permit tcp host ::1 host ::1
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::1/128"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .setDestinationPort(new PortNumRange(Enumeration.ANY))
                            .build());
            long sequenceId = 5;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 6 permit tcp host ::1 host ::1 lt www ttl eq 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::1/128"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));

            configBuilder.addAugmentation(Config4.class, new Config4Builder().setHopRange(new HopRange("10..10"))
                    .build());

            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(Enumeration.ANY))
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setDestinationPortNamed("0..www")
                                    .build())
                            .build());
            long sequenceId = 6;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 7 permit icmpv6 any host ::1 8 ttl neq 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));
            configBuilder.addAugmentation(Config4.class, new Config4Builder()
                    .setHopRange(new HopRange("11..9"))
                    .build());
            long sequenceId = 7;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    defTransport(null), (short) 8));
        }
        {
            // 8 permit udp f::a a::b eq 10 FE80:0000:0000:0000:0202:B3FF:FE1E:8329
            // FE80:0000:0000:0000:0202:B3FF:FE1E:8329 gt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class, new
                    AclSetAclEntryIpv6WildcardedAugBuilder()
                    .setSourceAddressWildcarded(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl
                            .ext.rev180314.src.dst.ipv6.address.wildcarded.SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("f::a"))
                            .setWildcardMask(new Ipv6Address("a::b"))
                            .build())
                    .setDestinationAddressWildcarded((new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                            .acl.ext.rev180314.src.dst.ipv6.address.wildcarded.DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("fe80:0000:0000:0000:0202:b3ff:fe1e:8329"))
                            .setWildcardMask(new Ipv6Address("fe80:0000:0000:0000:0202:b3ff:fe1e:8329"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange(new PortNumber(10)))
                            .setDestinationPort(new PortNumRange("10..65535"))
                            .build());
            long sequenceId = 8;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 9 permit udp host fe80:0000:0000:0000:0202:b3ff:fe1e:8329 gt 10 f::a a::b lt 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("fe80:0000:0000:0000:0202:b3ff:fe1e:8329/128"));
            configBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class, new
                    AclSetAclEntryIpv6WildcardedAugBuilder()
                    .setDestinationAddressWildcarded((new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                            .acl.ext.rev180314.src.dst.ipv6.address.wildcarded.DestinationAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("f::a"))
                            .setWildcardMask(new Ipv6Address("a::b"))
                            .build()))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("10..65535"))
                            .setDestinationPort(new PortNumRange("0..10"))
                            .build());
            long sequenceId = 9;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // 10 permit udp f::a a::b lt 10 any range 10 10
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class, new
                    AclSetAclEntryIpv6WildcardedAugBuilder()
                    .setSourceAddressWildcarded((new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl
                            .ext.rev180314.src.dst.ipv6.address.wildcarded.SourceAddressWildcardedBuilder()
                            .setAddress(new Ipv6Address("f::a"))
                            .setWildcardMask(new Ipv6Address("a::b"))
                            .build()))
                    .build());
            configBuilder.setDestinationAddress(new Ipv6Prefix("::/0"));
            TransportBuilder transportBuilder = new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                            .rev171215.transport.fields.top.transport.ConfigBuilder()
                            .setSourcePort(new PortNumRange("0..10"))
                            .setDestinationPort(new PortNumRange("10..10"))
                            .build());
            long sequenceId = 10;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            // deny ipv6 any any fragments sequence 50
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setSourceAddress(AclEntryLineParser.IPV6_HOST_ANY);
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV6_HOST_ANY);
            long sequenceId = 50;
            expectedResults.put(sequenceId, createIpv6AclEntry(sequenceId, DROP.class, configBuilder.build(),
                    defTransport(null)));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findIpv6LineWithSequenceId(sequenceId, lines).get();
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV6.class);
            assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

    @Test
    void testIpv6WithUnsupportedProtocol() {
        assertThrows(IllegalArgumentException.class, () -> {
            final String line = "permit pcp host AB::1 host CD::2 eq 69 sequence 20";
            final AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV6.class);
        });
    }

    @Test
    void testIpv6WithUnsupportedOption() {
        assertThrows(IllegalArgumentException.class, () -> {
            final String line = "permit udp host AB::1 host CD::2 eq 69 time-range RANGE_1 sequence 20";
            final AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV6.class);
        });
    }

    @Test
    void parseIpv6PrefixTest() throws Exception {
        LinkedList<String> ips = new LinkedList<>();
        ips.add("::ffff:192.0.2.1/96");
        Optional<Ipv6Prefix> ipv6Prefix = AclEntryLineParser.parseIpv6Prefix(ips);
        assertTrue(ipv6Prefix.isPresent());
        assertEquals(new Ipv6Prefix("::ffff:c000:0201/96"), ipv6Prefix.get());
        ips.add("0:0:0:0:0:ffff:c000:0201/96");
        ipv6Prefix = AclEntryLineParser.parseIpv6Prefix(ips);
        assertTrue(ipv6Prefix.isPresent());
        assertEquals(new Ipv6Prefix("0:0:0:0:0:ffff:c000:0201/96"), ipv6Prefix.get());
        ips.add("FE80:0000:0000:0000:0202:B3FF:FE1E:8329/96");
        ipv6Prefix = AclEntryLineParser.parseIpv6Prefix(ips);
        assertTrue(ipv6Prefix.isPresent());
        assertEquals(new Ipv6Prefix("FE80:0000:0000:0000:0202:B3FF:FE1E:8329/96"), ipv6Prefix.get());
    }

}
