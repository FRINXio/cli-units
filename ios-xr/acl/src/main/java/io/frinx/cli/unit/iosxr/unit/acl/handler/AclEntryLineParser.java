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
package io.frinx.cli.unit.iosxr.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.HopRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.IcmpMsgType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv6AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.acl.icmp.type.IcmpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.ConfigBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AclEntryLineParser {
    private static final Logger LOG = LoggerFactory.getLogger(AclEntryLineParser.class);
    static final Ipv4Prefix IPV4_HOST_ANY = new Ipv4Prefix("0.0.0.0/0");
    static final Ipv6Prefix IPV6_HOST_ANY = new Ipv6Prefix("::/0");
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int MAX_TTL = 255;
    private static final IpProtocolType IP_PROTOCOL_ICMP = new IpProtocolType(IPICMP.class);
    private static final IpProtocolType IP_PROTOCOL_ICMP_NUMBER = new IpProtocolType((short) 1);
    private static final IpProtocolType IP_PROTOCOL_ICMP6_NUMBER = new IpProtocolType((short) 58);
    private static final IpProtocolType IP_PROTOCOL_TCP = new IpProtocolType(IPTCP.class);
    private static final IpProtocolType IP_PROTOCOL_UDP = new IpProtocolType(IPUDP.class);
    public static final Pattern ZERO_TO_255_PATTERN = Pattern.compile("^2[0-5][0-5]|2[0-4][0-9]|1?[0-9]?[0-9]$");

    private AclEntryLineParser() {
    }

    static Optional<String> findAclEntryWithSequenceId(AclEntryKey aclEntryKey, String lines) {
        // search for line containing current sequence number
        long sequenceId = aclEntryKey.getSequenceId();
        return findLineWithSequenceId(sequenceId, lines);
    }

    static Optional<String> findLineWithSequenceId(long sequenceId, String lines) {
        Pattern pattern = Pattern.compile("^\\s*(" + sequenceId + " .*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(lines);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    static void parseLine(final AclEntryBuilder builder, String line, Class<? extends ACLTYPE> aclType) {

        Preconditions.checkArgument(ACLIPV4.class.equals(aclType) || ACLIPV6.class.equals(aclType),
                "Unsupported ACL type: " + aclType);
        Queue<String> words = Lists.newLinkedList(Arrays.asList(line.split("\\s")));
        long sequenceId = Long.parseLong(words.poll());
        // sequence id
        builder.setSequenceId(sequenceId);
        builder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                .entries.top.acl.entries.acl.entry.ConfigBuilder()
                .setSequenceId(sequenceId)
                .build()
        );
        // fwd action
        Class<? extends FORWARDINGACTION> fwdAction = parseAction(words.poll());
        builder.setActions(createActions(fwdAction));
        // protocol
        IpProtocolType ipProtocolType = parseProtocol(words.poll());
        if (ACLIPV4.class.equals(aclType)) {
            Ipv4Builder ipv4Builder = new Ipv4Builder();
            ParseIpv4LineResult parseIpv4LineResult = parseIpv4Line(ipProtocolType, words);
            ipv4Builder.setConfig(parseIpv4LineResult.ipv4ProtocolFieldsConfig);
            builder.setIpv4(ipv4Builder.build());
            builder.setTransport(parseIpv4LineResult.transport);
            builder.addAugmentation(AclEntry1.class, parseIpv4LineResult.icmpMsgTypeAugment);
        } else if (ACLIPV6.class.equals(aclType)) {
            Ipv6Builder ipv6Builder = new Ipv6Builder();
            ParseIpv6LineResult parseIpv6LineResult = parseIpv6Line(ipProtocolType, words);
            ipv6Builder.setConfig(parseIpv6LineResult.ipv6ProtocolFieldsConfig);
            builder.setIpv6(ipv6Builder.build());
            builder.setTransport(parseIpv6LineResult.transport);
            builder.addAugmentation(AclEntry1.class, parseIpv6LineResult.icmpMsgTypeAugment);
        }
    }

    static Actions createActions(Class<? extends FORWARDINGACTION> fwdAction) {
        return new ActionsBuilder().setConfig(new ConfigBuilder().setForwardingAction(fwdAction).build()).build();
    }

    static Class<? extends FORWARDINGACTION> parseAction(String action) {
        switch (action) {
            case "permit":
                return ACCEPT.class;
            case "deny":
                return DROP.class;
            default:
                throw new IllegalArgumentException("Did not match forwarding action for: " + action);
        }
    }

    private static class ParseIpv4LineResult {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol
                .fields.top.ipv4.Config ipv4ProtocolFieldsConfig;
        final Transport transport;
        final AclEntry1 icmpMsgTypeAugment;

        ParseIpv4LineResult(
                Config ipv4ProtocolFieldsConfig,
                Transport transport,
                final AclEntry1 icmpMsgTypeAugment) {
            this.ipv4ProtocolFieldsConfig = ipv4ProtocolFieldsConfig;
            this.transport = transport;
            this.icmpMsgTypeAugment = icmpMsgTypeAugment;
        }
    }

    private static ParseIpv4LineResult parseIpv4Line(IpProtocolType ipProtocolType, Queue<String> words) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top
                .ipv4.ConfigBuilder ipv4ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        ipv4ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top
                .transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();

        AclSetAclEntryIpv4WildcardedAugBuilder ipv4WildcardedAugBuilder = new AclSetAclEntryIpv4WildcardedAugBuilder();
        // src address
        Optional<Ipv4Prefix> srcIpv4PrefixOpt = parseIpv4Prefix(words);
        if (srcIpv4PrefixOpt.isPresent()) {
            ipv4ProtocolFieldsConfigBuilder.setSourceAddress(srcIpv4PrefixOpt.get());
        } else {
            SourceAddressWildcarded srcIpv4Wildcarded = new SourceAddressWildcardedBuilder(parseIpv4Wildcarded(words)
            ).build();
            ipv4WildcardedAugBuilder.setSourceAddressWildcarded(srcIpv4Wildcarded);
        }

        // src port
        parseTransportSourcePort(transportConfigBuilder, words);

        // dst address
        Optional<Ipv4Prefix> dstIpv4PrefixOpt = parseIpv4Prefix(words);
        if (dstIpv4PrefixOpt.isPresent()) {
            ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(dstIpv4PrefixOpt.get());
        } else {
            DestinationAddressWildcarded dstIpv4Wildcarded
                = new DestinationAddressWildcardedBuilder(parseIpv4Wildcarded(words)).build();
            ipv4WildcardedAugBuilder.setDestinationAddressWildcarded(dstIpv4Wildcarded);
        }

        // dst port
        parseTransportDestinationPort(transportConfigBuilder, words);

        // src dst wildcarded address
        if (ipv4WildcardedAugBuilder.getSourceAddressWildcarded() != null
                || ipv4WildcardedAugBuilder.getDestinationAddressWildcarded() != null) {
            ipv4ProtocolFieldsConfigBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                    ipv4WildcardedAugBuilder.build());
        }

        // transport
        Transport transport = new TransportBuilder().setConfig(transportConfigBuilder.build()).build();

        //icmp
        final AclEntry1 icmpMsgTypeAugment = parseIcmpMsgType(ipProtocolType, words, true);

        // ttl
        if (!words.isEmpty() && "ttl".equals(words.peek())) {
            Entry<Integer, Integer> ttlRange = parseTTLRange(words);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            Config3Builder hopRangeAugment = new Config3Builder();
            hopRangeAugment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
            ipv4ProtocolFieldsConfigBuilder.addAugmentation(Config3.class, hopRangeAugment.build());
        }

        // if there are some unsupported expressions, ACL cannot be parsed at all
        if (!words.isEmpty()) {
            throw new IllegalArgumentException("ACL entry contains unsupported expressions that cannot be parsed: "
                    + words);
        }
        return new ParseIpv4LineResult(ipv4ProtocolFieldsConfigBuilder.build(), transport, icmpMsgTypeAugment);
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    private static AclEntry1 parseIcmpMsgType(final IpProtocolType ipProtocolType, final Queue<String> words, boolean
            isIpv4Acl) {
        final AclEntry1Builder icmpMsgTypeAugment = new AclEntry1Builder();
        if (IP_PROTOCOL_ICMP.equals(ipProtocolType) || IP_PROTOCOL_ICMP_NUMBER.equals(ipProtocolType)
                || IP_PROTOCOL_ICMP6_NUMBER.equals(ipProtocolType)) {
            Optional<Short> maybeMsgType = tryToParseIcmpType(words.peek(), isIpv4Acl);
            if (maybeMsgType.isPresent()) {
                words.poll();
                icmpMsgTypeAugment.setIcmp(new IcmpBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext
                                .rev180314.acl.icmp.type.icmp.ConfigBuilder()
                                .setMsgType(new IcmpMsgType(maybeMsgType.get()))
                                .build()
                        ).build()
                );
                return icmpMsgTypeAugment.build();
            }
        }
        return null;
    }

    private static class ParseIpv6LineResult {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol
                .fields.top.ipv6.Config ipv6ProtocolFieldsConfig;
        final Transport transport;
        final AclEntry1 icmpMsgTypeAugment;

        ParseIpv6LineResult(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol
                        .fields.top.ipv6.Config ipv6ProtocolFieldsConfig,
                Transport transport,
                final AclEntry1 icmpMsgTypeAugment) {
            this.ipv6ProtocolFieldsConfig = ipv6ProtocolFieldsConfig;
            this.transport = transport;
            this.icmpMsgTypeAugment = icmpMsgTypeAugment;
        }
    }

    private static ParseIpv6LineResult parseIpv6Line(IpProtocolType ipProtocolType, Queue<String> words) {

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top
                .ipv6.ConfigBuilder ipv6ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
        ipv6ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top
                .transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();

        AclSetAclEntryIpv6WildcardedAugBuilder ipv6WildcardedAugBuilder = new AclSetAclEntryIpv6WildcardedAugBuilder();
        // src address
        Optional<Ipv6Prefix> srcIpv6PrefixOpt = parseIpv6Prefix(words);
        if (srcIpv6PrefixOpt.isPresent()) {
            ipv6ProtocolFieldsConfigBuilder.setSourceAddress(srcIpv6PrefixOpt.get());
        } else {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv6.address
                    .wildcarded.SourceAddressWildcarded srcIpv6Wildcarded = new org.opendaylight.yang.gen.v1.http
                    .frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv6.address.wildcarded
                    .SourceAddressWildcardedBuilder(parseIpv6Wildcarded(words)).build();
            ipv6WildcardedAugBuilder.setSourceAddressWildcarded(srcIpv6Wildcarded);
        }

        // src port
        parseTransportSourcePort(transportConfigBuilder, words);

        // dst address
        Optional<Ipv6Prefix> dstIpv6PrefixOpt = parseIpv6Prefix(words);
        if (dstIpv6PrefixOpt.isPresent()) {
            ipv6ProtocolFieldsConfigBuilder.setDestinationAddress(dstIpv6PrefixOpt.get());
        } else {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv6.address
                    .wildcarded.DestinationAddressWildcarded dstIpv6Wildcarded = new org.opendaylight.yang.gen
                    .v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv6.address.wildcarded
                    .DestinationAddressWildcardedBuilder(parseIpv6Wildcarded(words)).build();
            ipv6WildcardedAugBuilder.setDestinationAddressWildcarded(dstIpv6Wildcarded);
        }

        // dst port
        parseTransportDestinationPort(transportConfigBuilder, words);

        // src dst wildcarded address
        if (ipv6WildcardedAugBuilder.getSourceAddressWildcarded() != null
                || ipv6WildcardedAugBuilder.getDestinationAddressWildcarded() != null) {
            ipv6ProtocolFieldsConfigBuilder.addAugmentation(AclSetAclEntryIpv6WildcardedAug.class,
                    ipv6WildcardedAugBuilder.build());
        }

        // transport
        Transport transport = new TransportBuilder().setConfig(transportConfigBuilder.build()).build();

        //icmp
        final AclEntry1 icmpMsgTypeAugment = parseIcmpMsgType(ipProtocolType, words, false);

        // ttl
        if (!words.isEmpty() && "ttl".equals(words.peek())) {
            Entry<Integer, Integer> ttlRange = parseTTLRange(words);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            Config4Builder hopRangeAugment = new Config4Builder();
            hopRangeAugment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
            ipv6ProtocolFieldsConfigBuilder.addAugmentation(Config4.class, hopRangeAugment.build());
        }

        // if there are some unsupported expressions, ACL cannot be parsed at all
        if (!words.isEmpty()) {
            throw new IllegalArgumentException("ACL entry contains unsupported expressions that cannot be parsed: "
                    + words);
        }
        return new ParseIpv6LineResult(ipv6ProtocolFieldsConfigBuilder.build(), transport, icmpMsgTypeAugment);
    }

    private static void parseTransportSourcePort(
            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                    .fields.top.transport.ConfigBuilder transportConfigBuilder,
            final Queue<String> words) {
        if (!words.isEmpty() && isPortNumRange(words.peek())) {
            parsePortNumRange(words, transportConfigBuilder, true);
        } else {
            transportConfigBuilder.setSourcePort(new PortNumRange(Enumeration.ANY));
        }
    }

    private static void parseTransportDestinationPort(
            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                    .fields.top.transport.ConfigBuilder transportConfigBuilder,
            final Queue<String> words) {
        if (!words.isEmpty() && isPortNumRange(words.peek())) {
            parsePortNumRange(words, transportConfigBuilder, false);
        } else {
            transportConfigBuilder.setDestinationPort(new PortNumRange(Enumeration.ANY));
        }
    }

    private static Optional<Short> tryToParseIcmpType(String word, boolean isIpv4Acl) {
        if (word == null) {
            return Optional.empty();
        }
        if (ZERO_TO_255_PATTERN.matcher(word).matches()) {
            return Optional.of(Short.parseShort(word));
        }
        if (isIpv4Acl) {
            return Optional.ofNullable(ServiceToPortMapping.ICMP_MAPPING.get(word));
        }
        return Optional.ofNullable(ServiceToPortMapping.ICMPV6_MAPPING.get(word));
    }


    /**
     * Parse ttl (hop range).
     * If words contain ttl, return closed range 0-255..0-255.
     * If ttl neq number is specified, return number+1..number-1.
     */
    private static Entry<Integer, Integer> parseTTLRange(Queue<String> words) {
        String ttl = words.poll();
        Preconditions.checkArgument("ttl".equals(ttl));
        String keyword = words.poll();
        int num = Integer.parseInt(words.poll());
        switch (keyword) {
            case "eq": {
                return Maps.immutableEntry(num, num);
            }
            case "neq": {
                if (num == 0) { // > 0
                    return Maps.immutableEntry(1, MAX_TTL);
                } else if (num == MAX_TTL) { // < 255
                    return Maps.immutableEntry(0, MAX_TTL - 1);
                } else {
                    // not 22 = 23-21
                    return Maps.immutableEntry(num + 1, num - 1);
                }
            }
            case "lt": {
                return Maps.immutableEntry(0, (num - 1));
            }
            case "gt": {
                return Maps.immutableEntry((num + 1), MAX_TTL);
            }
            case "range": {
                int num2 = Integer.parseInt(words.poll());
                return Maps.immutableEntry(num, num2);
            }
            default:
                throw new IllegalArgumentException("Cannot parse ttl range keyword: " + keyword);
        }
    }

    private static int serviceToPortNumber(String portNumberOrServiceName) {
        try {
            return Integer.parseInt(portNumberOrServiceName);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse port: " + portNumberOrServiceName, e);
        }
    }

    private static void parsePortNumRange(Queue<String> words,
                                          org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                                  .rev171215.transport.fields.top.transport.ConfigBuilder
                                                  transportConfigBuilder,
                                          boolean source) {
        String possiblePortRangeKeyword = words.poll();

        String port1String = words.poll();
        String port2String = possiblePortRangeKeyword.equals("range") ? words.peek() : null;

        boolean arePortsNumeric = port2String == null
                ? StringUtils.isNumeric(port1String) : StringUtils.isNumeric(port1String)
                && StringUtils.isNumeric(port2String);

        if (arePortsNumeric) {
            Integer port1 = serviceToPortNumber(port1String);

            if (source) {
                transportConfigBuilder.setSourcePort(parsePortNumRangeNumbers(possiblePortRangeKeyword, port1, words));
            } else {
                transportConfigBuilder.setDestinationPort(parsePortNumRangeNumbers(possiblePortRangeKeyword, port1,
                        words));
            }
        } else {
            AclSetAclEntryTransportPortNamedAug existingAug = transportConfigBuilder
                    .getAugmentation(AclSetAclEntryTransportPortNamedAug.class);
            AclSetAclEntryTransportPortNamedAugBuilder aclSetAclEntryTransportPortNamedAugBuilder = existingAug
                    != null ? new AclSetAclEntryTransportPortNamedAugBuilder(existingAug) : new
                    AclSetAclEntryTransportPortNamedAugBuilder();

            if (source) {
                aclSetAclEntryTransportPortNamedAugBuilder
                        .setSourcePortNamed(parsePortNumRangeNamed(possiblePortRangeKeyword, port1String, words));
            } else {
                aclSetAclEntryTransportPortNamedAugBuilder
                        .setDestinationPortNamed(parsePortNumRangeNamed(possiblePortRangeKeyword, port1String, words));
            }

            transportConfigBuilder.addAugmentation(AclSetAclEntryTransportPortNamedAug.class,
                    aclSetAclEntryTransportPortNamedAugBuilder
                    .build());
        }
    }

    private static PortNumRange parsePortNumRangeNumbers(String possiblePortRangeKeyword, int port1, Queue<String>
            words) {
        switch (possiblePortRangeKeyword) {
            case "eq":
                return new PortNumRange(new PortNumber(port1));
            case "neq":
                if (port1 == 0) { // >0
                    return createPortNumRangeFromInt(1, MAX_PORT_NUMBER);
                } else if (port1 == MAX_PORT_NUMBER) { // <65535
                    return createPortNumRangeFromInt(0, MAX_PORT_NUMBER - 1);
                } else {
                    // not 22 = 23-21
                    return createPortNumRangeFromInt(port1 + 1, port1 - 1);
                }
            case "lt":
                return createPortNumRange(String.valueOf(0), String.valueOf(port1));
            case "gt":
                return createPortNumRange(String.valueOf(port1), String.valueOf(MAX_PORT_NUMBER));
            case "range":
                int port2 = serviceToPortNumber(words.poll());
                return createPortNumRange(String.valueOf(port1), String.valueOf(port2));
            default:
                throw new IllegalArgumentException("Not a port range keyword: " + possiblePortRangeKeyword);
        }
    }

    private static String parsePortNumRangeNamed(String possiblePortRangeKeyword, String port1, Queue<String> words) {
        switch (possiblePortRangeKeyword) {
            case "eq":
                return port1;
            case "neq":
                Integer result = ServiceToPortMapping.TCP_MAPPING.get(port1);
                if (result == null) {
                    // This is best effort case. We can only transform not SSH, if we know the number behind SSH,
                    // otherwise
                    // we fail
                    throw new IllegalArgumentException("Unknown named port, unable to translate to from 'neq' to "
                            + "range: " + port1);
                }

                // not 22 = 23-21
                return createPortNumRangeFromInt(result + 1, result - 1).getString();
            case "lt":
                return createPortRangeString(String.valueOf(0), port1);
            case "gt":
                return createPortRangeString(port1, String.valueOf(MAX_PORT_NUMBER));
            case "range":
                String port2 = words.poll();
                return createPortRangeString(port1, port2);
            default:
                throw new IllegalArgumentException("Not a port range keyword: " + possiblePortRangeKeyword);
        }
    }

    private static PortNumRange createPortNumRangeFromInt(int lower, int upper) {
        return createPortNumRange(String.valueOf(lower), String.valueOf(upper));
    }

    private static PortNumRange createPortNumRange(String lower, String upper) {
        return new PortNumRange(createPortRangeString(lower, upper));
    }

    private static String createPortRangeString(String lower, String upper) {
        return lower + ".." + upper;
    }

    private static boolean isPortNumRange(String word) {
        switch (word) {
            case "eq":
            case "neq":
            case "lt":
            case "gt":
            case "range":
                return true;
            default:
                return false;
        }
    }

    @Nullable
    private static IpProtocolType parseProtocol(String protocol) {
        switch (protocol) {
            case "ipv4":
            case "ipv6":
                LOG.debug("Skipping IP protocol {}", protocol);
                return null;
            case "udp":
                return IP_PROTOCOL_UDP;
            case "tcp":
                return IP_PROTOCOL_TCP;
            case "icmp":
            case "icmpv6":
                return IP_PROTOCOL_ICMP;
            case "1":
                return IP_PROTOCOL_ICMP_NUMBER;
            case "58":
                return IP_PROTOCOL_ICMP6_NUMBER;
            default:
                if (NumberUtils.isParsable(protocol)) {
                    return new IpProtocolType(Integer.valueOf(protocol).shortValue());
                }
                throw new IllegalArgumentException("IP protocol with following identifier is not supported: "
                        + protocol);
        }
    }

    private static Optional<Ipv4Prefix> parseIpv4Prefix(Queue<String> words) {
        String first = words.peek();
        if ("any".equals(first)) {
            // remove "any" from queue
            words.remove();
            return Optional.of(IPV4_HOST_ANY);
        } else if ("host".equals(first)) {
            // remove "host" from queue
            words.remove();
            int mask = 32;
            String ip = words.poll();
            return Optional.of(new Ipv4Prefix(ip + "/" + mask));
        } else if (first.contains("/")) {
            // remove "x.x.x.x/x" from queue
            words.remove();
            return Optional.of(new Ipv4Prefix(first));
        }
        return Optional.empty();
    }

    private static Ipv4AddressWildcarded parseIpv4Wildcarded(Queue<String> words) {
        String address = words.poll();
        Preconditions.checkState(!address.contains("/"), "Expected address and wildcard");
        String wildcard = words.poll();
        return new SourceAddressWildcardedBuilder().setAddress(new Ipv4Address(address))
                .setWildcardMask(new Ipv4Address(wildcard))
                .build();
    }

    @VisibleForTesting
    static Optional<Ipv6Prefix> parseIpv6Prefix(Queue<String> words) {
        String first = words.peek();
        if ("any".equals(first)) {
            // remove "any" from queue
            words.remove();
            return Optional.of(IPV6_HOST_ANY);
        } else if ("host".equals(first)) {
            // remove "host" from queue
            words.remove();
            int mask = 128;
            String ip = words.poll();
            return Optional.of(new Ipv6Prefix(ip + "/" + mask));
        } else if (first.contains("/")) {
            // remove "x:x:x:x:x:x:x:x/x" from queue
            words.remove();
            if (first.contains(".")) {
                first = translateIpv4InIpv6ToIpv6(first);
            }
            return Optional.of(new Ipv6Prefix(first));
        }
        return Optional.empty();
    }

    @VisibleForTesting
    static String translateIpv4InIpv6ToIpv6(String ipv4InIpv6) {
        String ipv4String = ipv4InIpv6.substring(ipv4InIpv6.lastIndexOf(':') + 1, ipv4InIpv6.indexOf('/'));
        InetAddress ia = InetAddresses.forString(ipv4String);
        byte[] address = ia.getAddress();
        String zeroOne = joinBytes(address[0], address[1]);
        String twoThree = joinBytes(address[2], address[3]);
        return ipv4InIpv6.replace(ipv4String, zeroOne + ":" + twoThree);
    }

    private static String joinBytes(byte first, byte second) {
        return String.format("%02x%02x", first, second);
    }

    private static Ipv6AddressWildcarded parseIpv6Wildcarded(Queue<String> words) {
        String address = words.poll();
        Preconditions.checkState(!address.contains("/"), "Expected address and wildcard");
        String wildcard = words.poll();
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv6.address
                .wildcarded.SourceAddressWildcardedBuilder()
                .setAddress(new Ipv6Address(address))
                .setWildcardMask(new Ipv6Address(wildcard))
                .build();
    }
}