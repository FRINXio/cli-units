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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Test;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;

public class AclEntryLineParserTest {

    private static long sequenceId = 1;

    static AclEntry createIpv4AclEntry(String termName, Class<? extends FORWARDINGACTION> fwdAction,
                                       Config ipv4Config,
                                       Transport transport,
                                       Short icmpMessageType) {
        return createAclEntry(termName, fwdAction, ipv4Config, null, transport, icmpMessageType);
    }

    static AclEntry createIpv4AclEntry(String termName, Class<? extends FORWARDINGACTION> fwdAction,
                                       Config ipv4Config,
                                       Transport transport) {
        return createAclEntry(termName, fwdAction, ipv4Config, null, transport, null);
    }

    static AclEntry createIpv6AclEntry(String termName, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport,
                                       Short icmpMessageType) {
        return createAclEntry(termName, fwdAction, null, ipv6Config, transport, icmpMessageType);
    }

    static AclEntry createIpv6AclEntry(String termName, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                       Transport transport) {
        return createAclEntry(termName, fwdAction, null, ipv6Config, transport, null);
    }

    static AclEntry createAclEntry(String termName, Class<? extends FORWARDINGACTION> fwdAction,
                                   Config ipv4Config,
                                   org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                           .rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6Config,
                                   Transport transport, final Short icmpMessageType) {

        Actions actions = AclEntryLineParser.createActions(fwdAction);
        AclEntryBuilder builder = new AclEntryBuilder();
        // sequence id
        builder.setSequenceId(sequenceId);
        builder.setConfig(new ConfigBuilder()
                .setSequenceId(sequenceId++)
                .addAugmentation(Config2.class, new Config2Builder().setTermName(termName).build())
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
        return builder.build();
    }

    static Transport defTransport() {
        TransportBuilder transportBuilder = new TransportBuilder();
        transportBuilder.setConfig(
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                        .fields.top.transport.ConfigBuilder()
                        .setSourcePort(new PortNumRange(PortNumRange.Enumeration.ANY))
                        .setDestinationPort(new PortNumRange(PortNumRange.Enumeration.ANY))
                        .build());
        return transportBuilder.build();
    }

    @Test
    public void testIpv4() {
        String lines = "Mon May 14 14:36:55.408 UTC\n"
                + "set firewall family inet filter inacl1 term 1 from source-address 0.0.0.2/32\n"
                + "set firewall family inet filter inacl1 term 1 from destination-address 0.0.0.254/32\n"
                + "set firewall family inet filter inacl1 term 1 from ttl 70-80\n"
                + "set firewall family inet filter inacl1 term 1 from source-port http\n"
                + "set firewall family inet filter inacl1 term 1 from source-port ftp\n"
                + "set firewall family inet filter inacl1 term 1 then discard\n"

                + "set firewall family inet filter inacl1 term 2 from source-address 0.0.0.2/32\n"
                + "set firewall family inet filter inacl1 term 2 from destination-address 0.0.0.254/32\n"
                + "set firewall family inet filter inacl1 term 2 from protocol udp\n"
                + "set firewall family inet filter inacl1 term 2 then accept\n"

                + "set firewall family inet filter inacl1 term 4 from source-address 1.2.3.4/32\n"
                + "set firewall family inet filter inacl1 term 4 from source-port www\n"
                + "set firewall family inet filter inacl1 term 4 from protocol tcp\n"
                + "set firewall family inet filter inacl1 term 4 then accept\n"

                + "set firewall family inet filter inacl1 term 5 from source-address 1.1.1.1/32\n"
                + "set firewall family inet filter inacl1 term 5 from destination-address 2.2.2.2/32\n"
                + "set firewall family inet filter inacl1 term 5 from protocol icmp\n"
                + "set firewall family inet filter inacl1 term 5 from ttl 0-10\n"
                + "set firewall family inet filter inacl1 term 5 then discard\n"

                + "set firewall family inet filter inacl1 term 6 from address 0.0.0.0/8\n"
                + "set firewall family inet filter inacl1 term 6 from port smtp\n"
                + "set firewall family inet filter inacl1 term 6 from protocol udp\n"
                + "set firewall family inet filter inacl1 term 6 from ttl 0-10\n"
                + "set firewall family inet filter inacl1 term 6 then accept\n"

                + "set firewall family inet filter inacl1 term 12 from source-address 1.1.1.1\n"
                + "set firewall family inet filter inacl1 term 12 from destination-address 0.0.0.0/8\n"
                + "set firewall family inet filter inacl1 term 12 from protocol udp\n"
                + "set firewall family inet filter inacl1 term 12 from source-port www\n"
                + "set firewall family inet filter inacl1 term 12 from destination-port http\n"
                + "set firewall family inet filter inacl1 term 12 then accept\n";

        LinkedHashMap<String, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setSourceAddress(new Ipv4Prefix("0.0.0.2/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("0.0.0.254/32"));
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("70..80"))
                    .build());
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("ftp")
                                    .build())
                            .setDestinationPort(new PortNumRange(PortNumRange.Enumeration.ANY))
                            .build());
            String termName = "1";
            expectedResults.put(termName, createIpv4AclEntry(termName, DROP.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("0.0.0.2/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("0.0.0.254/32"));
            String termName = "2";
            expectedResults.put(termName, createIpv4AclEntry(termName, ACCEPT.class, configBuilder.build(),
                    defTransport()));
        }
        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.2.3.4/32"));
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("www")
                                    .build())
                            .setDestinationPort(new PortNumRange(PortNumRange.Enumeration.ANY))
                            .build());
            String termName = "4";
            expectedResults.put(termName, createIpv4AclEntry(termName, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("2.2.2.2/32"));
            configBuilder.addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("0..10"))
                    .build());
            String termName = "5";
            expectedResults.put(termName, createIpv4AclEntry(termName, DROP.class, configBuilder.build(),
                    defTransport()));
        }
        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class))
                    .setDestinationAddress(new Ipv4Prefix("0.0.0.0/8"))
                    .setSourceAddress(new Ipv4Prefix("0.0.0.0/8"))
                    .addAugmentation(Config3.class, new Config3Builder().setHopRange(new HopRange("0..10"))
                        .build())
                    .build();
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("smtp")
                                    .setDestinationPortNamed("smtp")
                                    .build())
                            .build());
            String termName = "6";
            expectedResults.put(termName, createIpv4AclEntry(termName, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }
        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv4Prefix("1.1.1.1/32"));
            configBuilder.setDestinationAddress(new Ipv4Prefix("0.0.0.0/8"));
            TransportBuilder transportBuilder = new TransportBuilder();
            transportBuilder.setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                            .fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("www")
                                    .setDestinationPortNamed("http")
                                    .build())
                            .build());
            String termName = "12";
            expectedResults.put(termName, createIpv4AclEntry(termName, ACCEPT.class, configBuilder.build(),
                    transportBuilder.build()));
        }

        // verify expected results
        for (Entry<String, AclEntry> entry : expectedResults.entrySet()) {
            String termName = entry.getValue().getConfig().getAugmentation(Config2.class).getTermName();
            List<String> line = AclEntryLineParser.findLinesWithTermName(termName, lines);
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLines(resultBuilder, line, ACLIPV4.class, entry.getValue().getKey());
            Assert.assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

    @Test
    public void parseIpv6() {

        String lines = "set firewall family inet6 filter inacl1 term 7 from source-address ::/0\n"
            + "set firewall family inet6 filter inacl1 term 7 from destination-address ::1/128\n"
            + "set firewall family inet6 filter inacl1 term 7 from payload-protocol icmp\n"
            + "set firewall family inet6 filter inacl1 term 7 from icmp-type echo-request\n"
            + "set firewall family inet6 filter inacl1 term 7 from ttl 11-9\n"
            + "set firewall family inet6 filter inacl1 term 7 then accept\n"

            + "set firewall family inet6 filter inacl1 term 8 from destination-address "
            + "fe80:0000:0000:0000:0202:b3ff:fe1e:8329/128\n"
            + "set firewall family inet6 filter inacl1 term 8 from source-address f::a/64\n"
            + "set firewall family inet6 filter inacl1 term 8 from payload-protocol udp\n"
            + "set firewall family inet6 filter inacl1 term 8 from port ftp\n"
            + "set firewall family inet6 filter inacl1 term 8 then accept\n";

        LinkedHashMap<String, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPICMP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("::/0"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("::1/128"));
            configBuilder.addAugmentation(Config4.class, new Config4Builder()
                    .setHopRange(new HopRange("11..9"))
                    .build());
            String termName = "7";
            expectedResults.put(termName, createIpv6AclEntry(termName, ACCEPT.class, configBuilder.build(),
                    defTransport(), (short) 128));
        }
        {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
            configBuilder.setSourceAddress(new Ipv6Prefix("f::a/64"));
            configBuilder.setDestinationAddress(new Ipv6Prefix("fe80:0000:0000:0000:0202:b3ff:fe1e:8329/128"));
            String termName = "8";
            expectedResults.put(termName, createIpv6AclEntry(termName, ACCEPT.class, configBuilder.build(),
                    new TransportBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                            .header.fields.rev171215.transport.fields.top.transport.ConfigBuilder()
                            .addAugmentation(AclSetAclEntryTransportPortNamedAug.class, new
                                    AclSetAclEntryTransportPortNamedAugBuilder()
                                    .setSourcePortNamed("ftp")
                                    .setDestinationPortNamed("ftp")
                                    .build())
                            .build()).build()));
        }

        // verify expected results
        for (Entry<String, AclEntry> entry : expectedResults.entrySet()) {
            String termName = entry.getValue().getConfig().getAugmentation(Config2.class).getTermName();
            List<String> line = AclEntryLineParser.findLinesWithTermName(termName, lines);
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLines(resultBuilder, line, ACLIPV6.class, entry.getValue().getKey());
            Assert.assertEquals(entry.getValue(), resultBuilder.build());
        }
    }
}
