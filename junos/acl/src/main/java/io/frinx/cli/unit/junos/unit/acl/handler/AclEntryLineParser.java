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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange.Enumeration;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AclEntryLineParser {
    private static final Logger LOG = LoggerFactory.getLogger(AclEntryLineParser.class);
    static final Ipv4Prefix IPV4_HOST_ANY = new Ipv4Prefix("0.0.0.0/0");
    static final Ipv6Prefix IPV6_HOST_ANY = new Ipv6Prefix("::/0");
    private static final PortNumRange ANY_PORT = new PortNumRange(Enumeration.ANY);
    private static final IpProtocolType IP_PROTOCOL_ICMP = new IpProtocolType(IPICMP.class);
    private static final IpProtocolType IP_PROTOCOL_ICMP_NUMBER = new IpProtocolType((short) 1);
    private static final IpProtocolType IP_PROTOCOL_TCP = new IpProtocolType(IPTCP.class);
    private static final IpProtocolType IP_PROTOCOL_UDP = new IpProtocolType(IPUDP.class);
    private static final Pattern ZERO_TO_255_PATTERN = Pattern.compile("^2[0-5][0-5]|2[0-4][0-9]|1?[0-9]?[0-9]$");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
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
            parseIpv4lines(builder, lines);
        } else if (ACLIPV6.class.equals(aclType)) {
            parseIpv6lines(builder, lines);
        } else {
            throw new IllegalArgumentException("Unsupported ACL type: " + aclType);
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

    private static void parseIpv4lines(AclEntryBuilder builder, List<String> lines) {
        List<String> unsupportedLines = lines.stream()
                .filter(line -> !line.contains("from") && !line.contains("then") && !line.trim().isEmpty())
                .collect(Collectors.toList());
        if (!unsupportedLines.isEmpty()) {
            throw new IllegalArgumentException("The following access-list lines are not supported:\n "
                    + unsupportedLines);
        }

        Ipv4AclEntrySupportedCheckingBuilder checkingBuilder = new Ipv4AclEntrySupportedCheckingBuilder(builder);
        List<String> from = lines.stream().filter(s -> s.contains("from")).collect(Collectors.toList());
        parseIpv4FromLines(checkingBuilder, from);

        List<String> then = lines.stream().filter(s -> s.contains("then")).collect(Collectors.toList());
        parseThenLines(then, checkingBuilder);
        checkingBuilder.compile();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    private static void parseIpv4FromLines(Ipv4AclEntrySupportedCheckingBuilder builder, List<String> from) {
        Queue<String> ttlArgs = new LinkedList<>();
        for (String line : from) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(WHITESPACE.split(line.trim())));
            //remove from
            words.poll();

            if (words.isEmpty()) {
                throw new IllegalStateException("Missing any keyword in access-list");
            }

            String poll = words.poll();
            switch (poll) {
                case "address" -> {
                    // src or dst address
                    Optional<Ipv4Prefix> ipv4Prefix = parseIpv4Prefix(words.poll());
                    ipv4Prefix.ifPresent(prefix -> {
                        builder.setSourceAddress(prefix);
                        builder.setDestinationAddress(prefix);
                    });
                }
                case "source-address" ->
                    // src address
                        parseIpv4Prefix(words.poll()).ifPresent(builder::setSourceAddress);
                case "source-port" ->
                    // src port
                        parseTransportSourcePort(builder, words.poll());
                case "destination-address" ->
                    // dst address
                        parseIpv4Prefix(words.poll()).ifPresent(builder::setDestinationAddress);
                case "destination-port" ->
                    // dst port
                        parseTransportDestinationPort(builder, words.poll());
                case "port" -> {
                    // src or dst port
                    String port = words.poll();
                    parseTransportSourcePort(builder, port);
                    parseTransportDestinationPort(builder, port);
                }
                case "protocol" -> {
                    IpProtocolType ipProtocolType = parseProtocol(words.poll());
                    builder.setProtocol(ipProtocolType);
                }
                case "icmp-type" -> {
                    AclEntry1 icmpMsg = parseIcmpMsgType(builder.getProtocol(), words.poll(), true);
                    builder.setIcmpType(icmpMsg);
                }
                case "ttl" -> ttlArgs.add(words.poll());
                default -> throw new IllegalArgumentException(String.format("%s is not supported.", poll));
            }
        }

        if (!ttlArgs.isEmpty()) {
            Config3Builder hopRangeAugment = new Config3Builder();
            Entry<Integer, Integer> ttlRange = parseTTLRange(ttlArgs);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            hopRangeAugment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
            if (hopRangeAugment.getHopRange() != null) {
                builder.setTtlConfig(hopRangeAugment.build());
            }
        }
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

    private static void parseIpv6lines(AclEntryBuilder builder, List<String> lines) {
        Ipv6AclEntrySupportedCheckingBuilder dupsCheckingBuilder = new Ipv6AclEntrySupportedCheckingBuilder(builder);
        List<String> from = lines.stream().filter(s -> s.contains("from")).collect(Collectors.toList());
        parseIpv6FromLines(dupsCheckingBuilder, from);

        List<String> then = lines.stream().filter(s -> s.contains("then")).collect(Collectors.toList());
        parseThenLines(then, dupsCheckingBuilder);
        dupsCheckingBuilder.compile();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    private static void parseIpv6FromLines(Ipv6AclEntrySupportedCheckingBuilder builder, List<String> from) {
        Queue<String> ttlArgs = new LinkedList<>();
        Pattern whitespace = Pattern.compile("\\s");
        for (String line : from) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(whitespace.split(line.trim())));
            //remove from
            words.poll();

            if (words.isEmpty()) {
                throw new IllegalStateException("Missing any keyword in access-list");
            }

            String poll = words.poll();
            switch (poll) {
                case "address" -> {
                    // src or dst address
                    Optional<Ipv6Prefix> ipv6Prefix = parseIpv6Prefix(words.poll());
                    if (ipv6Prefix.isPresent()) {
                        builder.setSourceAddress(ipv6Prefix.get());
                        builder.setDestinationAddress(ipv6Prefix.get());
                    }
                }
                case "source-address" ->
                    // src address
                        parseIpv6Prefix(words.poll()).ifPresent(builder::setSourceAddress);
                case "source-port" ->
                    // src port
                        parseTransportSourcePort(builder, words.poll());
                case "destination-address" ->
                    // dst address
                        parseIpv6Prefix(words.poll()).ifPresent(builder::setDestinationAddress);
                case "destination-port" ->
                    // dst port
                        parseTransportDestinationPort(builder, words.poll());
                case "port" -> {
                    // src or dst port
                    String port = words.poll();
                    parseTransportSourcePort(builder, port);
                    parseTransportDestinationPort(builder, port);
                }
                case "payload-protocol" -> {
                    IpProtocolType ipProtocolType = parseProtocol(words.poll());
                    builder.setProtocol(ipProtocolType);
                }
                case "icmp-type" -> {
                    AclEntry1 icmpMsg = parseIcmpMsgType(builder.getProtocol(), words.poll(), false);
                    builder.setIcmpType(icmpMsg);
                }
                case "ttl" -> ttlArgs.add(words.poll());
                default -> throw new IllegalArgumentException(String.format("%s is not supported.", poll));
            }
        }

        if (!ttlArgs.isEmpty()) {
            Config4Builder hopRangeAugment = new Config4Builder();
            Entry<Integer, Integer> ttlRange = parseTTLRange(ttlArgs);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            hopRangeAugment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
            if (hopRangeAugment.getHopRange() != null) {
                builder.setTtlConfig(hopRangeAugment.build());
            }
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    private static void parseThenLines(final List<String> thenLines, final AclEntrySupportedCheckingBuilder builder) {
        Pattern whitespace = Pattern.compile("\\s");
        for (String line : thenLines) {
            Queue<String> words = Lists.newLinkedList(Arrays.asList(whitespace.split(line.trim())));
            words.poll();

            final String thenKeyword = words.poll();
            switch (thenKeyword) {
                case "accept" -> builder.setAction(createActions(ACCEPT.class));
                case "discard" -> builder.setAction(createActions(DROP.class));
                default -> throw new IllegalArgumentException(String.format("%s is not supported.", thenKeyword));
            }
        }

        if (!builder.isActionSet()) {
            // by default, the traffic that matches an ACL entry is accepted (see: https://www.juniper.net/
            // documentation/en_US/junos/topics/concept/firewall-filter-ex-series-evaluation-understanding.html
            builder.setAction(createActions(ACCEPT.class));
        }
    }

    private static void parseTransportSourcePort(final AclEntrySupportedCheckingBuilder checkingBuilder,
                                                 final String port) {
        if (!port.isEmpty()) {
            parsePortNum(port, checkingBuilder, true);
        }
    }

    private static void parseTransportDestinationPort(final AclEntrySupportedCheckingBuilder checkingBuilder,
                                                      final String port) {
        if (!port.isEmpty()) {
            parsePortNum(port, checkingBuilder, false);
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

    private static void parsePortNum(final String port, final AclEntrySupportedCheckingBuilder checkingBuilder,
                                     final boolean source) {
        String[] ports = port.split("-");
        boolean isRange = ports.length > 1;
        boolean arePortsNumeric = isRange ? StringUtils.isNumeric(ports[0]) && StringUtils.isNumeric(ports[1])
                : StringUtils.isNumeric(ports[0]);

        if (arePortsNumeric) {
            if (source) {
                checkingBuilder.setSourcePort(parsePortNumRangeNumbers(isRange, port));
            } else {
                checkingBuilder.setDestinationPort(parsePortNumRangeNumbers(isRange, port));
            }
        } else {
            if (source) {
                checkingBuilder.setNamedSourcePort(port);
            } else {
                checkingBuilder.setNamedDestinationPort(port);
            }
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
                throw new IllegalArgumentException("Unknown protocol: " + protocol);
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

    private abstract static class AclEntrySupportedCheckingBuilder {
        private static final String DUP_SOURCE_PORT_MESSAGE
                = "ACL entry with multiple source port definitions is not supported.";
        private static final String DUP_DESTINATION_PORT_MESSAGE
                = "ACL entry with multiple destination port definitions is not supported.";
        protected static final String DUP_SOURCE_ADDRESS_MESSAGE
                = "ACL entry with multiple source addresses is not supported.";
        protected static final String DUP_DESTINATION_ADDRESS_MESSAGE
                = "ACL entry with multiple destination addresses is not supported.";
        protected static final String DUP_PROTOCOL_MESSAGE
                = "ACL entry with multiple protocols is not supported.";
        protected static final String DUP_TTL_MESSAGE
                = "ACL entry with multiple TTL configurations is not supported.";

        protected AclEntryBuilder aclEntryBuilder;
        protected final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport
                .fields.top.transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();

        private boolean setSourcePort = false;
        private boolean setDestinationPort = false;
        private String namedSourcePort;
        private String namedDestinationPort;

        AclEntrySupportedCheckingBuilder(final AclEntryBuilder aclEntryBuilder) {
            this.aclEntryBuilder = aclEntryBuilder;
        }

        void setSourcePort(final PortNumRange portNumRange) {
            Preconditions.checkArgument(!setSourcePort, DUP_SOURCE_PORT_MESSAGE);
            transportConfigBuilder.setSourcePort(portNumRange);
            setSourcePort = true;
        }

        void setNamedSourcePort(final String namedSourcePort) {
            Preconditions.checkArgument(!setSourcePort, DUP_SOURCE_PORT_MESSAGE);
            this.namedSourcePort = namedSourcePort;
            setSourcePort = true;
        }

        void setDestinationPort(final PortNumRange portNumRange) {
            Preconditions.checkArgument(!setDestinationPort, DUP_DESTINATION_PORT_MESSAGE);
            transportConfigBuilder.setDestinationPort(portNumRange);
            setDestinationPort = true;
        }

        void setNamedDestinationPort(final String namedDestinationPort) {
            Preconditions.checkArgument(!setDestinationPort, DUP_DESTINATION_PORT_MESSAGE);
            this.namedDestinationPort = namedDestinationPort;
            setDestinationPort = true;
        }

        void setIcmpType(final AclEntry1 icmpFields) {
            Preconditions.checkArgument(aclEntryBuilder.getAugmentation(AclEntry1.class) == null,
                    "ACL entry with multiple ICMP types is not supported.");
            aclEntryBuilder.addAugmentation(AclEntry1.class, icmpFields);
        }

        void setAction(final Actions actions) {
            Preconditions.checkArgument(aclEntryBuilder.getActions() == null,
                    "ACL entry with multiple actions is not supported.");
            aclEntryBuilder.setActions(actions);
        }

        boolean isActionSet() {
            return aclEntryBuilder.getActions() != null;
        }

        void compile() {
            AclSetAclEntryTransportPortNamedAugBuilder namedPortsBuilder = null;
            if (namedSourcePort != null) {
                namedPortsBuilder = new AclSetAclEntryTransportPortNamedAugBuilder();
                namedPortsBuilder.setSourcePortNamed(namedSourcePort);
            }
            if (namedDestinationPort != null) {
                if (namedPortsBuilder == null) {
                    namedPortsBuilder = new AclSetAclEntryTransportPortNamedAugBuilder();
                }
                namedPortsBuilder.setDestinationPortNamed(namedDestinationPort);
            }

            if (transportConfigBuilder.getSourcePort() == null && namedSourcePort == null) {
                transportConfigBuilder.setSourcePort(ANY_PORT);
            }
            if (transportConfigBuilder.getDestinationPort() == null && namedDestinationPort == null) {
                transportConfigBuilder.setDestinationPort(ANY_PORT);
            }
            if (namedPortsBuilder != null) {
                transportConfigBuilder.addAugmentation(AclSetAclEntryTransportPortNamedAug.class,
                        namedPortsBuilder.build());
            }
        }
    }

    private static final class Ipv4AclEntrySupportedCheckingBuilder extends AclEntrySupportedCheckingBuilder {

        private final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol
                .fields.top.ipv4.ConfigBuilder ipv4ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http
                .frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();

        Ipv4AclEntrySupportedCheckingBuilder(final AclEntryBuilder aclEntryBuilder) {
            super(aclEntryBuilder);
        }

        void setSourceAddress(final Ipv4Prefix ipv4Prefix) {
            Preconditions.checkArgument(ipv4ProtocolFieldsConfigBuilder.getSourceAddress() == null,
                    DUP_SOURCE_ADDRESS_MESSAGE);
            ipv4ProtocolFieldsConfigBuilder.setSourceAddress(ipv4Prefix);
        }

        void setDestinationAddress(final Ipv4Prefix ipv4Prefix) {
            Preconditions.checkArgument(ipv4ProtocolFieldsConfigBuilder.getDestinationAddress() == null,
                    DUP_DESTINATION_ADDRESS_MESSAGE);
            ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(ipv4Prefix);
        }

        void setProtocol(final IpProtocolType protocol) {
            Preconditions.checkArgument(ipv4ProtocolFieldsConfigBuilder.getProtocol() == null, DUP_PROTOCOL_MESSAGE);
            ipv4ProtocolFieldsConfigBuilder.setProtocol(protocol);
        }

        void setTtlConfig(final Config3 ttlConfig) {
            Preconditions.checkArgument(ipv4ProtocolFieldsConfigBuilder.getAugmentation(Config3.class) == null,
                    DUP_TTL_MESSAGE);
            ipv4ProtocolFieldsConfigBuilder.addAugmentation(Config3.class, ttlConfig);
        }

        IpProtocolType getProtocol() {
            return ipv4ProtocolFieldsConfigBuilder.getProtocol();
        }

        void compile() {
            super.compile();

            if (ipv4ProtocolFieldsConfigBuilder.getSourceAddress() == null) {
                ipv4ProtocolFieldsConfigBuilder.setSourceAddress(IPV4_HOST_ANY);
            }
            if (ipv4ProtocolFieldsConfigBuilder.getDestinationAddress() == null) {
                ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(IPV4_HOST_ANY);
            }

            final Ipv4Builder ipv4Builder = new Ipv4Builder();
            ipv4Builder.setConfig(ipv4ProtocolFieldsConfigBuilder.build());
            aclEntryBuilder.setIpv4(ipv4Builder.build());
            aclEntryBuilder.setTransport(new TransportBuilder()
                    .setConfig(transportConfigBuilder.build())
                    .build());
        }
    }

    private static final class Ipv6AclEntrySupportedCheckingBuilder extends AclEntrySupportedCheckingBuilder {

        private final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol
                .fields.top.ipv6.ConfigBuilder ipv6ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http
                .frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();

        Ipv6AclEntrySupportedCheckingBuilder(final AclEntryBuilder aclEntryBuilder) {
            super(aclEntryBuilder);
        }

        void setSourceAddress(final Ipv6Prefix ipv6Prefix) {
            Preconditions.checkArgument(ipv6ProtocolFieldsConfigBuilder.getSourceAddress() == null,
                    DUP_SOURCE_ADDRESS_MESSAGE);
            ipv6ProtocolFieldsConfigBuilder.setSourceAddress(ipv6Prefix);
        }

        void setDestinationAddress(final Ipv6Prefix ipv6Prefix) {
            Preconditions.checkArgument(ipv6ProtocolFieldsConfigBuilder.getDestinationAddress() == null,
                    DUP_DESTINATION_ADDRESS_MESSAGE);
            ipv6ProtocolFieldsConfigBuilder.setDestinationAddress(ipv6Prefix);
        }

        void setProtocol(final IpProtocolType protocol) {
            Preconditions.checkArgument(ipv6ProtocolFieldsConfigBuilder.getProtocol() == null, DUP_PROTOCOL_MESSAGE);
            ipv6ProtocolFieldsConfigBuilder.setProtocol(protocol);
        }

        IpProtocolType getProtocol() {
            return ipv6ProtocolFieldsConfigBuilder.getProtocol();
        }

        void setTtlConfig(final Config4 ttlConfig) {
            Preconditions.checkArgument(ipv6ProtocolFieldsConfigBuilder.getAugmentation(Config4.class) == null,
                    DUP_TTL_MESSAGE);
            ipv6ProtocolFieldsConfigBuilder.addAugmentation(Config4.class, ttlConfig);
        }

        void compile() {
            super.compile();

            if (ipv6ProtocolFieldsConfigBuilder.getSourceAddress() == null) {
                ipv6ProtocolFieldsConfigBuilder.setSourceAddress(IPV6_HOST_ANY);
            }
            if (ipv6ProtocolFieldsConfigBuilder.getDestinationAddress() == null) {
                ipv6ProtocolFieldsConfigBuilder.setDestinationAddress(IPV6_HOST_ANY);
            }

            final Ipv6Builder ipv6Builder = new Ipv6Builder();
            ipv6Builder.setConfig(ipv6ProtocolFieldsConfigBuilder.build());
            aclEntryBuilder.setIpv6(ipv6Builder.build());
            aclEntryBuilder.setTransport(new TransportBuilder()
                    .setConfig(transportConfigBuilder.build())
                    .build());
        }
    }
}