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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AclEntryLineParser {
    private static final Logger LOG = LoggerFactory.getLogger(AclEntryLineParser.class);
    static final Ipv4Prefix IPV4_HOST_ANY = new Ipv4Prefix("0.0.0.0/0");
    static final Ipv6Prefix IPV6_HOST_ANY = new Ipv6Prefix("::/0");
    private static final IpProtocolType IP_PROTOCOL_ICMP = new IpProtocolType(IPICMP.class);
    private static final IpProtocolType IP_PROTOCOL_ICMP_NUMBER = new IpProtocolType((short) 1);
    private static final IpProtocolType IP_PROTOCOL_TCP = new IpProtocolType(IPTCP.class);
    private static final IpProtocolType IP_PROTOCOL_UDP = new IpProtocolType(IPUDP.class);
    private static final Pattern ZERO_TO_255_PATTERN = Pattern.compile("^2[0-5][0-5]|2[0-4][0-9]|1?[0-9]?[0-9]$");
    private static final String ESCAPE_REGEX_CHARS = "\"-\\^$+*?.()|[]{}";

    private AclEntryLineParser() {

    }

    static List<String> findLinesWithTermName(String termName, String lines) {
        termName = termName.contains("\"")
                ? Stream.of(termName.split(""))
                        .map(s -> ESCAPE_REGEX_CHARS.contains(s) ? "\\" + s : s)
                        .collect(Collectors.joining()) : termName;
        Pattern pattern = Pattern.compile("\\s* term " + termName + " (?<config>(then|from) .+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(lines);
        List<String> maybeLines = Lists.newArrayList();
        while (matcher.find()) {
            maybeLines.add(matcher.group("config"));
        }
        return maybeLines;
    }

    static void parseLines(final AclEntryBuilder builder, List<String> lines,
                           Class<? extends ACLTYPE> aclType, AclEntryKey entryKey, String termName) {

        Preconditions.checkArgument(ACLIPV4.class.equals(aclType) || ACLIPV6.class.equals(aclType),
                "Unsupported ACL type" + aclType);

        parseTermName(builder, entryKey, termName);

        if (ACLIPV4.class.equals(aclType)) {
            parseIpv4line(builder, lines);
        } else if (ACLIPV6.class.equals(aclType)) {
            parseIpv6line(builder, lines);
        }
    }

    private static void parseTermName(final AclEntryBuilder builder, AclEntryKey entryKey, String termName) {
        builder.setSequenceId(entryKey.getSequenceId());
        builder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                .entries.top.acl.entries.acl.entry.ConfigBuilder()
                .setSequenceId(entryKey.getSequenceId())
                .addAugmentation(Config2.class, new Config2Builder().setTermName(termName.replace("\"", "")).build())
                .build()
        );
    }

    static Actions createActions(Class<? extends FORWARDINGACTION> fwdAction) {
        return new ActionsBuilder().setConfig(new ConfigBuilder().setForwardingAction(fwdAction).build()).build();
    }

    private static Class<? extends FORWARDINGACTION> parseAction(String action) {
        switch (action) {
            case "accept":
                return ACCEPT.class;
            case "discard":
                return DROP.class;
            default:
                LOG.warn("Did not match forwarding action for {}", action);
                return null;
        }
    }

    private static void parseIpv4line(AclEntryBuilder builder, List<String> lines) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top
                .ipv4.ConfigBuilder ipv4ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        ipv4ProtocolFieldsConfigBuilder.setSourceAddress(IPV4_HOST_ANY);
        ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(IPV4_HOST_ANY);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top
                .transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();
        transportConfigBuilder.setSourcePort(new PortNumRange(PortNumRange.Enumeration.ANY));
        transportConfigBuilder.setDestinationPort(new PortNumRange(PortNumRange.Enumeration.ANY));

        Queue<String> ttlArgs = new LinkedList<>();

        List<String> from = lines.stream().filter(s -> s.contains("from")).collect(Collectors.toList());
        List<String> then = lines.stream().filter(s -> s.contains("then")).collect(Collectors.toList());
        AclEntry1 icmpMsg;
        for (String line : from) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(line.trim().split("\\s")));
            //remove from
            words.poll();

            if (words.isEmpty()) {
                throw new IllegalStateException("Missing any keyword in access-list");
            }

            String poll = words.poll();
            switch (poll) {
                case "address":
                    // src or dst address
                    Optional<Ipv4Prefix> ipv4Prefix = parseIpv4Prefix(words.poll());
                    if (ipv4Prefix.isPresent()) {
                        ipv4ProtocolFieldsConfigBuilder.setSourceAddress(ipv4Prefix.get());
                        ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(ipv4Prefix.get());
                    }
                    break;
                case "source-address":
                    // src address
                    parseIpv4Prefix(words.poll()).ifPresent(ipv4ProtocolFieldsConfigBuilder::setSourceAddress);
                    break;
                case "source-port":
                    // src port
                    parseTransportSourcePort(transportConfigBuilder, words.poll());
                    break;
                case "destination-address":
                    // dst address
                    parseIpv4Prefix(words.poll()).ifPresent(ipv4ProtocolFieldsConfigBuilder::setDestinationAddress);
                    break;
                case "destination-port":
                    // dst port
                    parseTransportDestinationPort(transportConfigBuilder, words.poll());
                    break;
                case "port":
                    // src or dst port
                    String port = words.poll();
                    parseTransportSourcePort(transportConfigBuilder, port);
                    parseTransportDestinationPort(transportConfigBuilder, port);
                    break;
                case "protocol":
                    IpProtocolType ipProtocolType = parseProtocol(words.poll());
                    ipv4ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
                    break;
                case "icmp-type":
                    icmpMsg = parseIcmpMsgType(ipv4ProtocolFieldsConfigBuilder.getProtocol(), words.poll(), true);
                    builder.addAugmentation(AclEntry1.class, icmpMsg);
                    break;
                case "ttl":
                    ttlArgs.add(words.poll());
                    break;
                default:
                    throw new IllegalArgumentException(String.format("%s is not supported.", poll));
            }
        }

        for (String line : then) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(line.trim().split("\\s")));
            words.poll();
            // fwd action
            Class<? extends FORWARDINGACTION> fwdAction = parseAction(words.poll());
            builder.setActions(createActions(fwdAction));
        }

        if (!ttlArgs.isEmpty()) {
            Config3Builder hopRangeAugment = new Config3Builder();
            Entry<Integer, Integer> ttlRange = parseTTLRange(ttlArgs);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            hopRangeAugment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
            if (hopRangeAugment.getHopRange() != null) {
                ipv4ProtocolFieldsConfigBuilder.addAugmentation(Config3.class, hopRangeAugment.build());
            }
        }

        Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setConfig(ipv4ProtocolFieldsConfigBuilder.build());
        builder.setIpv4(ipv4Builder.build());
        builder.setTransport(new TransportBuilder().setConfig(transportConfigBuilder.build()).build());
    }

    private static AclEntry1 parseIcmpMsgType(final IpProtocolType ipProtocolType, final String msgType,
                                              boolean isIpv4Acl) {
        final AclEntry1Builder icmpMsgTypeAugment = new AclEntry1Builder();
        if (IP_PROTOCOL_ICMP.equals(ipProtocolType) || IP_PROTOCOL_ICMP_NUMBER.equals(ipProtocolType)) {
            Optional<Short> maybeMsgType = tryToParseIcmpType(msgType, isIpv4Acl);
            if (maybeMsgType.isPresent()) {
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

    private static void parseIpv6line(AclEntryBuilder builder, List<String> lines) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top
                .ipv6.ConfigBuilder ipv6ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
        ipv6ProtocolFieldsConfigBuilder.setSourceAddress(IPV6_HOST_ANY);
        ipv6ProtocolFieldsConfigBuilder.setDestinationAddress(IPV6_HOST_ANY);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top
                .transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();
        transportConfigBuilder.setSourcePort(new PortNumRange(PortNumRange.Enumeration.ANY));
        transportConfigBuilder.setDestinationPort(new PortNumRange(PortNumRange.Enumeration.ANY));

        Queue<String> ttlArgs = new LinkedList<>();

        List<String> from = lines.stream().filter(s -> s.contains("from")).collect(Collectors.toList());
        List<String> then = lines.stream().filter(s -> s.contains("then")).collect(Collectors.toList());
        AclEntry1 icmpMsg;
        for (String line : from) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(line.trim().split("\\s")));
            //remove from
            words.poll();

            if (words.isEmpty()) {
                throw new IllegalStateException("Missing any keyword in access-list");
            }

            String poll = words.poll();
            switch (poll) {
                case "address":
                    // src or dst address
                    Optional<Ipv6Prefix> ipv6Prefix = parseIpv6Prefix(words.poll());
                    if (ipv6Prefix.isPresent()) {
                        ipv6ProtocolFieldsConfigBuilder.setSourceAddress(ipv6Prefix.get());
                        ipv6ProtocolFieldsConfigBuilder.setDestinationAddress(ipv6Prefix.get());
                    }
                    break;
                case "source-address":
                    // src address
                    parseIpv6Prefix(words.poll()).ifPresent(ipv6ProtocolFieldsConfigBuilder::setSourceAddress);
                    break;
                case "source-port":
                    // src port
                    parseTransportSourcePort(transportConfigBuilder, words.poll());
                    break;
                case "destination-address":
                    // dst address
                    parseIpv6Prefix(words.poll()).ifPresent(ipv6ProtocolFieldsConfigBuilder::setDestinationAddress);
                    break;
                case "destination-port":
                    // dst port
                    parseTransportDestinationPort(transportConfigBuilder, words.poll());
                    break;
                case "port":
                    // src or dst port
                    String port = words.poll();
                    parseTransportSourcePort(transportConfigBuilder, port);
                    parseTransportDestinationPort(transportConfigBuilder, port);
                    break;
                case "payload-protocol":
                    IpProtocolType ipProtocolType = parseProtocol(words.poll());
                    ipv6ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
                    break;
                case "icmp-type":
                    icmpMsg = parseIcmpMsgType(ipv6ProtocolFieldsConfigBuilder.getProtocol(), words.poll(), false);
                    builder.addAugmentation(AclEntry1.class, icmpMsg);
                    break;
                case "ttl":
                    ttlArgs.add(words.poll());
                    break;
                default:
                    throw new IllegalArgumentException(String.format("%s is not supported.", poll));
            }
        }

        if (!ttlArgs.isEmpty()) {
            Config4Builder hopRangeAugment = new Config4Builder();
            Entry<Integer, Integer> ttlRange = parseTTLRange(ttlArgs);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            hopRangeAugment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
            if (hopRangeAugment.getHopRange() != null) {
                ipv6ProtocolFieldsConfigBuilder.addAugmentation(Config4.class, hopRangeAugment.build());
            }
        }

        for (String line : then) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(line.trim().split("\\s")));
            words.poll();
            // fwd action
            Class<? extends FORWARDINGACTION> fwdAction = parseAction(words.poll());
            builder.setActions(createActions(fwdAction));
        }

        Ipv6Builder ipv6Builder = new Ipv6Builder();
        ipv6Builder.setConfig(ipv6ProtocolFieldsConfigBuilder.build());
        builder.setIpv6(ipv6Builder.build());
        builder.setTransport(new TransportBuilder().setConfig(transportConfigBuilder.build()).build());
    }

    private static void parseTransportSourcePort(
            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                    .fields.top.transport.ConfigBuilder transportConfigBuilder,
            final String port) {
        if (!port.isEmpty()) {
            parsePortNum(port, transportConfigBuilder, true);
        }
    }

    private static void parseTransportDestinationPort(
            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                    .fields.top.transport.ConfigBuilder transportConfigBuilder,
            final String port) {
        if (!port.isEmpty()) {
            parsePortNum(port, transportConfigBuilder, false);
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
     * if exists more than 1 ttl config, use first one, ignore others
     */
    private static Entry<Integer, Integer> parseTTLRange(Queue<String> words) {
        String ttl = words.poll();
        if (ttl.contains("-")) {
            String[] range = ttl.split("-");
            return Maps.immutableEntry(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
        } else {
            int intTtl = Integer.parseInt(ttl);
            return Maps.immutableEntry(intTtl, intTtl);
        }
    }

    private static void parsePortNum(String port,
                                     org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                                  .rev171215.transport.fields.top.transport.ConfigBuilder
                                                  transportConfigBuilder,
                                     boolean source) {
        String[] ports = port.split("-");
        boolean isRange = ports.length > 1;
        boolean arePortsNumeric = isRange ? StringUtils.isNumeric(ports[0]) && StringUtils.isNumeric(ports[1])
                : StringUtils.isNumeric(ports[0]);

        if (arePortsNumeric) {
            if (source) {
                transportConfigBuilder.setSourcePort(parsePortNumRangeNumbers(isRange, port));
            } else {
                transportConfigBuilder.setDestinationPort(parsePortNumRangeNumbers(isRange, port));
            }
        } else {
            AclSetAclEntryTransportPortNamedAug existingAug = transportConfigBuilder
                    .getAugmentation(AclSetAclEntryTransportPortNamedAug.class);
            AclSetAclEntryTransportPortNamedAugBuilder aclSetAclEntryTransportPortNamedAugBuilder = existingAug
                    != null ? new AclSetAclEntryTransportPortNamedAugBuilder(existingAug) : new
                    AclSetAclEntryTransportPortNamedAugBuilder();

            if (source) {
                aclSetAclEntryTransportPortNamedAugBuilder.setSourcePortNamed(port);
                transportConfigBuilder.setSourcePort(null);
            } else {
                aclSetAclEntryTransportPortNamedAugBuilder.setDestinationPortNamed(port);
                transportConfigBuilder.setDestinationPort(null);
            }

            transportConfigBuilder.addAugmentation(AclSetAclEntryTransportPortNamedAug.class,
                    aclSetAclEntryTransportPortNamedAugBuilder
                            .build());
        }
    }

    private static PortNumRange parsePortNumRangeNumbers(boolean isRange, String port) {
        if (isRange) {
            return new PortNumRange(port.replace("-", ".."));
        }
        return new PortNumRange(new PortNumber(Integer.parseInt(port)));
    }

    @Nullable
    private static IpProtocolType parseProtocol(String protocol) {
        switch (protocol) {
            case "ipip":
            case "ipv6":
                LOG.debug("Skipping IP protocol {}", protocol);
                return null;
            case "udp":
                return IP_PROTOCOL_UDP;
            case "tcp":
                return IP_PROTOCOL_TCP;
            case "icmp":
            case "icmp6":
                return IP_PROTOCOL_ICMP;
            default:
                if (NumberUtils.isParsable(protocol)) {
                    return new IpProtocolType(Integer.valueOf(protocol).shortValue());
                }
                LOG.warn("Unknown protocol {}", protocol);
                return null;
        }
    }

    private static Optional<Ipv4Prefix> parseIpv4Prefix(String ip) {
        ip = ip.contains("/") ? ip : ip + "/32";
        return Optional.of(new Ipv4Prefix(ip));
    }

    private static Optional<Ipv6Prefix> parseIpv6Prefix(String ip) {
        ip = ip.contains("/") ? ip : ip + "/128";
        if (ip.contains(".")) {
            ip = translateIpv4InIpv6ToIpv6(ip);
        }
        return Optional.of(new Ipv6Prefix(ip));
    }

    @VisibleForTesting
    private static String translateIpv4InIpv6ToIpv6(String ipv4InIpv6) {
        String ipv4String = ipv4InIpv6.substring(ipv4InIpv6.lastIndexOf(":") + 1, ipv4InIpv6.indexOf("/"));
        InetAddress ia = InetAddresses.forString(ipv4String);
        byte[] address = ia.getAddress();
        String zeroOne = joinBytes(address[0], address[1]);
        String twoThree = joinBytes(address[2], address[3]);
        return ipv4InIpv6.replace(ipv4String, zeroOne + ":" + twoThree);
    }

    private static String joinBytes(byte first, byte second) {
        return String.format("%02x%02x", first, second);
    }

}
