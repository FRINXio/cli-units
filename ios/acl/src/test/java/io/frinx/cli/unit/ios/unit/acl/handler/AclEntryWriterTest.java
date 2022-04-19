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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEstablishedStateAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEstablishedStateAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclOptionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclOptionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclPrecedenceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclPrecedenceAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetOption;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetPrecedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class AclEntryWriterTest {

    private static final String ACL_STANDARD_WRITE = "configure terminal\n"
            + "ip access-list standard TEST\n"
            + "20 permit 123.45.6.0 0.0.0.255\n"
            + "end\n";

    private static final String ACL_STANDARD_DELETE = "configure terminal\n"
            + "ip access-list standard TEST\n"
            + "no 10\n"
            + "end\n";

    private static final String ACL_EXTENDED_WRITE_EQ = "configure terminal\n"
            + "ip access-list extended TEST\n"
            + "10 deny udp 10.10.99.0 0.0.0.254 eq 545 68 0.0.0.0 255.255.255.255     \n"
            + "end\n";

    private static final String ACL_EXTENDED_WRITE_NEQ = "configure terminal\n"
            + "ip access-list extended TEST\n"
            + "10 deny udp 10.10.99.0 0.0.0.254 neq 68 545 0.0.0.0 255.255.255.255     \n"
            + "end\n";

    private static final String ACL_EXTENDED_WRITE_PRECEDENCE = "configure terminal\n"
            + "ip access-list extended TEST\n"
            + "10 deny tcp 0.0.0.0 255.255.255.255  0.0.0.0 255.255.255.255   precedence internet  \n"
            + "end\n";

    private static final String ACL_EXTENDED_WRITE_OPTION = "configure terminal\n"
            + "ip access-list extended TEST\n"
            + "10 deny ip 0.0.0.0 255.255.255.255 0.0.0.0 255.255.255.255  option record-route \n"
            + "end\n";

    private static final String ACL_EXTENDED_WRITE_ESTABLISHED_TRUE = "configure terminal\n"
            + "ip access-list extended TEST\n"
            + "10 permit tcp 1.1.1.1 0.0.0.0  2.2.2.2 0.0.0.0  established   \n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private AclEntryWriter writer;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(AclSets.class)
            .child(AclSet.class, new AclSetKey("TEST", ACLIPV4STANDARD.class));
    private InstanceIdentifier piidext = KeyedInstanceIdentifier.create(AclSets.class)
            .child(AclSet.class, new AclSetKey("TEST", ACLIPV4EXTENDED.class));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new AclEntryWriter(cli);
    }

    @Test
    public void testStandardWrite() throws WriteFailedException {
        final AclEntry aclEntry = getStandardAclEntry(20L, "permit", "123.45.6.0", "0.0.0.255");
        writer.writeCurrentAttributes(piid, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_STANDARD_WRITE, response.getValue().getContent());
    }

    @Test
    public void testStandardDelete() throws WriteFailedException {
        final AclEntry aclEntry = new AclEntryBuilder().setSequenceId(10L).build();
        writer.deleteCurrentAttributes(piid, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_STANDARD_DELETE, response.getValue().getContent());
    }

    @Test
    public void testExtendedWriteEq() throws WriteFailedException {
        final AclEntry aclEntry = getExtendedAclEntry(10L, "deny", "udp",
                "10.10.99.0", "0.0.0.254", null, null, "545 68", null,
                null, new PortNumRange(PortNumRange.Enumeration.ANY),
                null, null ,null, null);
        writer.writeCurrentAttributes(piidext, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_EXTENDED_WRITE_EQ, response.getValue().getContent());
    }

    @Test
    public void testExtendedWriteNeq() throws WriteFailedException {
        final AclEntry aclEntry = getExtendedAclEntry(10L, "deny", "udp",
                "10.10.99.0", "0.0.0.254", null, null, "0..67 69..544 546..65535",
                null, null, new PortNumRange(PortNumRange.Enumeration.ANY),
                null, null, null, null);
        writer.writeCurrentAttributes(piidext, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_EXTENDED_WRITE_NEQ, response.getValue().getContent());
    }

    @Test
    public void testExtendedWritePrecedence() throws WriteFailedException {
        final AclEntry aclEntry = getExtendedAclEntry(10L, "deny", "tcp",
                null, null, null, null, null, null,
                null, null, AclSetPrecedence.Precedence.INTERNET,
                null, null, false);
        writer.writeCurrentAttributes(piidext, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_EXTENDED_WRITE_PRECEDENCE, response.getValue().getContent());
    }

    @Test
    public void testExtendedWriteOption() throws WriteFailedException {
        final AclEntry aclEntry = getExtendedAclEntry(10L, "deny", "ip",
                null, null, null, null, null, null,
                null, null, null, AclSetOption.Option.Enumeration.RECORDROUTE,
                null, null);
        writer.writeCurrentAttributes(piidext, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_EXTENDED_WRITE_OPTION, response.getValue().getContent());
    }

    @Test
    public void testExtendedWriteEstablished() throws WriteFailedException {
        final AclEntry aclEntry = getExtendedAclEntry(10L, "permit", "tcp",
                "1.1.1.1", "0.0.0.0", "2.2.2.2", "0.0.0.0", null, null,
                null, null, null, null, null, true);
        writer.writeCurrentAttributes(piidext, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(ACL_EXTENDED_WRITE_ESTABLISHED_TRUE, response.getValue().getContent());
    }

    private AclEntry getStandardAclEntry(final Long sequence,
                                         final String fwdAction,
                                         final String ip,
                                         final String mask) {
        final AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setSequenceId(sequence);
        aclEntryBuilder.setActions(parseAction(fwdAction));

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, getSourceIpv4WildcardedAug(ip, mask));

        final Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setConfig(configBuilder.build());
        aclEntryBuilder.setIpv4(ipv4Builder.build());

        return aclEntryBuilder.build();
    }

    private AclEntry getExtendedAclEntry(final Long sequence,
                                         final String fwdAction,
                                         final String protocol,
                                         final String srcIp,
                                         final String srcMask,
                                         final String dstIp,
                                         final String dstMask,
                                         final String srcNamedPort,
                                         final String dstNamedPort,
                                         final PortNumRange srcPort,
                                         final PortNumRange dstPort,
                                         final AclSetPrecedence.Precedence precedence,
                                         final AclSetOption.Option.Enumeration optionNamed,
                                         final Short optionNumbered,
                                         final Boolean established) {
        final var aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setSequenceId(sequence);
        aclEntryBuilder.setActions(parseAction(fwdAction));

        final var configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                .rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        final var transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                .rev171215.transport.fields.top.transport.ConfigBuilder();
        if (srcIp == null || srcMask == null) {
            configBuilder.setSourceAddress(AclEntryLineParser.IPV4_HOST_ANY);
        }
        if (dstIp == null || dstMask == null) {
            configBuilder.setDestinationAddress(AclEntryLineParser.IPV4_HOST_ANY);
        }
        var ipv4WildcardedAug = getIpv4WildcardedAug(srcIp, srcMask, dstIp, dstMask);
        ipv4WildcardedAug.ifPresent(a -> {
            configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, a);
        });

        final var transportBuilder = new TransportBuilder();
        if (srcPort != null) {
            transportConfigBuilder.setSourcePort(srcPort);
        }
        if (dstPort != null) {
            transportConfigBuilder.setDestinationPort(dstPort);
        }
        var namedPortAug = getTransportNamedPortAug(srcNamedPort, dstNamedPort);
        namedPortAug.ifPresent(a -> {
            transportConfigBuilder.addAugmentation(AclSetAclEntryTransportPortNamedAug.class, a);
        });
        if (established != null && established.equals(true)) {
            transportConfigBuilder.addAugmentation(AclEstablishedStateAug.class, new AclEstablishedStateAugBuilder()
                    .setEstablished(true)
                    .build());
        } else if (established != null && established.equals(false)) {
            transportConfigBuilder.addAugmentation(AclEstablishedStateAug.class, new AclEstablishedStateAugBuilder()
                    .setEstablished(false)
                    .build());
        }
        transportBuilder.setConfig(transportConfigBuilder.build());

        if (protocol.equals("tcp")) {
            configBuilder.setProtocol(new IpProtocolType(IPTCP.class));
        } else if (protocol.equals("udp")) {
            configBuilder.setProtocol(new IpProtocolType(IPUDP.class));
        }
        if (precedence != null) {
            var aclPrecedence = new AclPrecedenceAugBuilder()
                    .setPrecedence(precedence)
                    .build();
            aclEntryBuilder.addAugmentation(AclPrecedenceAug.class, aclPrecedence);
        }
        if (optionNamed != null) {
            var aclOptions = new AclOptionAugBuilder()
                    .setOption(new AclSetOption.Option(optionNamed))
                    .build();
            aclEntryBuilder.addAugmentation(AclOptionAug.class, aclOptions);
        } else if (optionNumbered != null) {
            var aclOptions = new AclOptionAugBuilder()
                    .setOption(new AclSetOption.Option(optionNumbered))
                    .build();
            aclEntryBuilder.addAugmentation(AclOptionAug.class, aclOptions);
        }
        final var ipv4Builder = new Ipv4Builder();
        ipv4Builder.setConfig(configBuilder.build());
        aclEntryBuilder.setIpv4(ipv4Builder.build());
        aclEntryBuilder.setTransport(transportBuilder.build());

        return aclEntryBuilder.build();
    }

    private Actions parseAction(String action) {
        final ActionsBuilder actionsBuilder = new ActionsBuilder();
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions
                .ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl
                .rev170526.action.top.actions.ConfigBuilder();

        switch (action) {
            case "permit":
                configBuilder.setForwardingAction(ACCEPT.class);
                break;
            case "deny":
                configBuilder.setForwardingAction(DROP.class);
                break;
            default:
                break;
        }

        actionsBuilder.setConfig(configBuilder.build());
        return actionsBuilder.build();
    }

    private AclSetAclEntryIpv4WildcardedAug getSourceIpv4WildcardedAug(final String ip, final String mask) {
        final AclSetAclEntryIpv4WildcardedAugBuilder augBuilder = new AclSetAclEntryIpv4WildcardedAugBuilder();
        augBuilder.setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                .setAddress(new Ipv4Address(ip))
                .setWildcardMask(new Ipv4Address(mask))
                .build());
        return augBuilder.build();
    }

    private Optional<AclSetAclEntryIpv4WildcardedAug> getIpv4WildcardedAug(final String srcIp,
                                                                 final String srcMask,
                                                                 final String dstIp,
                                                                 final String dstMask) {
        final var augBuilder = new AclSetAclEntryIpv4WildcardedAugBuilder();
        if ((srcIp == null || srcMask == null) && (dstIp == null || dstMask == null)) {
            return Optional.empty();
        }
        if (srcIp != null && srcMask != null) {
            augBuilder.setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                    .setAddress(new Ipv4Address(srcIp))
                    .setWildcardMask(new Ipv4Address(srcMask))
                    .build());
        }
        if (dstIp != null && dstMask != null) {
            augBuilder.setDestinationAddressWildcarded(new DestinationAddressWildcardedBuilder()
                    .setAddress(new Ipv4Address(dstIp))
                    .setWildcardMask(new Ipv4Address(dstMask))
                    .build());
        }
        return Optional.of(augBuilder.build());
    }

    private Optional<AclSetAclEntryTransportPortNamedAug> getTransportNamedPortAug(final String srcPort,
                                                                                   final String dstPort) {
        var builder = new AclSetAclEntryTransportPortNamedAugBuilder();
        if (srcPort == null && dstPort == null) {
            return Optional.empty();
        }
        if (srcPort != null) {
            builder.setSourcePortNamed(srcPort);
        }
        if (dstPort != null) {
            builder.setDestinationPortNamed(dstPort);
        }
        return Optional.of(builder.build());
    }

    @Test
    public void processIpv6_srcDst_addresses() {
        AclEntry aclEntry = new AclEntryBuilder().setSequenceId(1L)
                .setConfig(new ConfigBuilder().setSequenceId(1L).build())
                .setIpv6(new Ipv6Builder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                    .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(new Ipv6Prefix("FE80:0000:0000:0000:0202:B3FF:FE1E:1111/96"))
                                .setSourceAddress(new Ipv6Prefix("FE80:0000:0000:0000:0202:B3FF:FE1E:2222/96"))
                                .build()
                ).build())
                .build();
        AclEntryWriter.MaxMetricCommandDTO result = new AclEntryWriter.MaxMetricCommandDTO();
        AclEntryWriter.processIpv6(aclEntry, result);
        Assert.assertEquals("FE80:0000:0000:0000:0202:B3FF:FE1E:1111/96", result.aclDstAddr);
        Assert.assertEquals("FE80:0000:0000:0000:0202:B3FF:FE1E:2222/96", result.aclSrcAddr);

        aclEntry = new AclEntryBuilder().setSequenceId(1L)
                .setConfig(new ConfigBuilder().setSequenceId(1L).build())
                .setIpv6(new Ipv6Builder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                    .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(new Ipv6Prefix("0:0:0:0:0:ffff:c000:0201/96"))
                                .setSourceAddress(new Ipv6Prefix("0:0:0:0:0:ffff:c000:0201/96"))
                                .build()
                ).build())
                .build();
        result = new AclEntryWriter.MaxMetricCommandDTO();
        AclEntryWriter.processIpv6(aclEntry, result);
        Assert.assertEquals("0:0:0:0:0:ffff:192.0.2.1/96", result.aclDstAddr);
        Assert.assertEquals("0:0:0:0:0:ffff:192.0.2.1/96", result.aclSrcAddr);

        aclEntry = new AclEntryBuilder().setSequenceId(1L)
                .setConfig(new ConfigBuilder().setSequenceId(1L).build())
                .setIpv6(new Ipv6Builder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                    .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(new Ipv6Prefix("::ffff:c000:0201/96"))
                                .setSourceAddress(new Ipv6Prefix("::ffff:c000:0201/96"))
                                .build()
                ).build())
                .build();
        result = new AclEntryWriter.MaxMetricCommandDTO();
        AclEntryWriter.processIpv6(aclEntry, result);
        Assert.assertEquals("::ffff:192.0.2.1/96", result.aclDstAddr);
        Assert.assertEquals("::ffff:192.0.2.1/96", result.aclSrcAddr);
    }
}