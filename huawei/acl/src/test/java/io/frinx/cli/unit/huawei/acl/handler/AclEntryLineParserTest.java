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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;

class AclEntryLineParserTest {
    static AclEntry createIpv4AclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                       org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                               .rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4Config,
                                       Transport transport) {
        return createAclEntry(sequenceId, fwdAction, ipv4Config, transport);
    }

    static AclEntry createAclEntry(long sequenceId, Class<? extends FORWARDINGACTION> fwdAction,
                                   Config ipv4Config,
                                   Transport transport) {

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
        // ipv4
        builder.setIpv4(new Ipv4Builder().setConfig(ipv4Config).build());

        return builder.build();
    }

    static Transport defTransport() {
        TransportBuilder transportBuilder = new TransportBuilder();
        transportBuilder.setConfig(
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                        .fields.top.transport.ConfigBuilder()
                        .setDestinationPort(new PortNumRange(new PortNumber(1985)))
                        .build());
        return transportBuilder.build();
    }

    @Test
    void testIpv4Standard() {
        String lines = """
                acl name MGT-IN 3000
                 rule 10 permit ip source 198.18.2.0 0.0.0.255\s
                 rule 20 permit ip destination 192.12.2.1 0

                """;
        LinkedHashMap<Long, AclEntry> expectedResults = new LinkedHashMap<>();

        {
            // rule 10 permit ip source 198.18.2.0, wildcard 0.0.0.255
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                    new AclSetAclEntryIpv4WildcardedAugBuilder()
                            .setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                                    .setAddress(new Ipv4Address("198.18.2.0"))
                                    .setWildcardMask(new Ipv4Address("0.0.0.255"))
                                    .build())
                            .build());
            long sequenceId = 10;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }
        {
            // rule 20 permit ip destination 213.51.120.0 0
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                    .net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
            configBuilder.setDestinationAddress(new Ipv4Prefix("192.12.2.1/0"));
            long sequenceId = 20;
            expectedResults.put(sequenceId, createIpv4AclEntry(sequenceId, ACCEPT.class, configBuilder.build(), null));
        }

        // verify expected results
        for (Entry<Long, AclEntry> entry : expectedResults.entrySet()) {
            long sequenceId = entry.getKey();
            String line = AclEntryLineParser.findIpv4LineWithSequenceId(sequenceId, lines).get();
            AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4STANDARD.class, "3000");
            assertEquals(entry.getValue(), resultBuilder.build());
        }
    }

    @Test
    void parseAclLineWithUnsupportedProtocolTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            final String line = "10 deny esp host 1.2.3.4 host 5.6.7.8";
            final AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4EXTENDED.class, "3000");
        });
    }

    @Test
    void testIpv4WithUnsupportedOption() {
        assertThrows(IllegalArgumentException.class, () -> {
            final String line = "permit tcp host 1.1.1.1 host 2.2.2.2 eq 69 established";
            final AclEntryBuilder resultBuilder = new AclEntryBuilder();
            AclEntryLineParser.parseLine(resultBuilder, line, ACLIPV4EXTENDED.class, "3000");
        });
    }

    @Test
    void testFindLineWithSequenceId() {
        String lines = """
                a
                 rule 1 foo
                 rule 2 bar baz
                xxx""";
        assertEquals(Optional.of("1 foo"), AclEntryLineParser.findIpv4LineWithSequenceId(1L, lines));
        assertEquals(Optional.of("2 bar baz"), AclEntryLineParser.findIpv4LineWithSequenceId(2L, lines));
        assertEquals(Optional.empty(), AclEntryLineParser.findIpv4LineWithSequenceId(3L, lines));
    }

}
