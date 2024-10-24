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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.frinx.cli.unit.huawei.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AclEntryLineParser {
    private static final Logger LOG = LoggerFactory.getLogger(AclEntryLineParser.class);
    private static final int MAX_PORT_NUMBER = 65535;
    private static final IpProtocolType IP_PROTOCOL_ICMP = new IpProtocolType(IPICMP.class);
    private static final IpProtocolType IP_PROTOCOL_ICMP_NUMBER = new IpProtocolType((short) 1);
    private static final IpProtocolType IP_PROTOCOL_ICMP6_NUMBER = new IpProtocolType((short) 58);
    private static final IpProtocolType IP_PROTOCOL_TCP = new IpProtocolType(IPTCP.class);
    private static final IpProtocolType IP_PROTOCOL_UDP = new IpProtocolType(IPUDP.class);

    private AclEntryLineParser() {
    }

    static Optional<String> findAclEntryWithSequenceId(AclEntryKey aclEntryKey, String lines,
                                                       Class<? extends ACLTYPE> aclType) {
        // search for line containing current sequence number
        long sequenceId = aclEntryKey.getSequenceId();
        return findIpv4LineWithSequenceId(sequenceId, lines);
    }

    static Optional<String> findIpv4LineWithSequenceId(long sequenceId, String lines) {
        Pattern pattern = Pattern.compile("^\\s*rule (" + sequenceId + " .*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(lines);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    static String findAccessListType(String name, String lines) {
        Optional<String> accessList = ParsingUtils.parseField(lines, 0,
            parseAcl(name)::matcher,
            matcher -> matcher.group("accessList"));
        return accessList.orElse("");
    }

    private static Pattern parseAcl(final String name) {
        final String regex = String.format("acl name %s (?<accessList>\\S+)", name);
        return Pattern.compile(regex);
    }

    static void parseLine(final AclEntryBuilder builder, String line, Class<? extends ACLTYPE> aclType,
                          String accessListType) {

        Preconditions.checkArgument(AclUtil.isIpv4Acl(aclType) || ACLIPV6.class.equals(aclType),
                "Unsupported ACL type: " + aclType);
        line = AclUtil.editAclEntry(line, aclType);
        Queue<String> words = Lists.newLinkedList(Arrays.asList(line.trim().split("\\s")));
        // ipv4 access lists have sequence number in the beginning of the line, ipv6 access lists have it at the end
        parseSequenceId(builder, words.poll());
        // fwd action
        Class<? extends FORWARDINGACTION> fwdAction = parseAction(words.poll());
        builder.setActions(createActions(fwdAction));
        // protocol
        IpProtocolType ipProtocolType = null;
        if (accessListType.charAt(0) != '2') {
            ipProtocolType = parseProtocol(words.poll());
        }

        Ipv4Builder ipv4Builder = new Ipv4Builder();
        ParseIpv4LineResult parseIpv4LineResult = parseIpv4Line(ipProtocolType, words, aclType);
        ipv4Builder.setConfig(parseIpv4LineResult.ipv4ProtocolFieldsConfig);
        builder.setIpv4(ipv4Builder.build());
        builder.setTransport(parseIpv4LineResult.transport);
    }

    private static void parseSequenceId(final AclEntryBuilder builder, String sequence) {
        long sequenceId = Long.parseLong(Objects.requireNonNull(sequence));
        // sequence id
        builder.setSequenceId(sequenceId);
        builder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list
                .entries.top.acl.entries.acl.entry.ConfigBuilder()
                .setSequenceId(sequenceId)
                .build()
        );
    }

    static Actions createActions(Class<? extends FORWARDINGACTION> fwdAction) {
        return new ActionsBuilder().setConfig(new ConfigBuilder().setForwardingAction(fwdAction).build()).build();
    }

    private static Class<? extends FORWARDINGACTION> parseAction(String action) {
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
        final Config ipv4ProtocolFieldsConfig;
        final Transport transport;
        final AclEntry1 icmpMsgTypeAugment;

        @SuppressFBWarnings("URF_UNREAD_FIELD")
        ParseIpv4LineResult(
                Config ipv4ProtocolFieldsConfig,
                Transport transport,
                final AclEntry1 icmpMsgTypeAugment) {
            this.ipv4ProtocolFieldsConfig = ipv4ProtocolFieldsConfig;
            this.transport = transport;
            this.icmpMsgTypeAugment = icmpMsgTypeAugment;
        }
    }

    private static ParseIpv4LineResult parseIpv4Line(IpProtocolType ipProtocolType,
                                                     Queue<String> words,
                                                     Class<? extends ACLTYPE> aclType) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top
                .ipv4.ConfigBuilder ipv4ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        ipv4ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top
                .transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();

        AclSetAclEntryIpv4WildcardedAugBuilder ipv4WildcardedAugBuilder = new AclSetAclEntryIpv4WildcardedAugBuilder();
        // src address
        String specification = words.poll();
        if ("source".equals(specification)) {
            Optional<Ipv4Prefix> srcIpv4PrefixOpt = parseIpv4Prefix(words);
            if (srcIpv4PrefixOpt.isPresent()) {
                ipv4ProtocolFieldsConfigBuilder.setSourceAddress(srcIpv4PrefixOpt.get());
            } else {
                SourceAddressWildcarded srcIpv4Wildcarded = new SourceAddressWildcardedBuilder(
                        parseIpv4Wildcarded(words)).build();
                ipv4WildcardedAugBuilder.setSourceAddressWildcarded(srcIpv4Wildcarded);
                ipv4ProtocolFieldsConfigBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                        ipv4WildcardedAugBuilder.build());
            }
            specification = words.poll();
        } else if ("destination".equals(specification)) {
            Optional<Ipv4Prefix> srcIpv4PrefixOpt = parseIpv4Prefix(words);
            if (srcIpv4PrefixOpt.isPresent()) {
                ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(srcIpv4PrefixOpt.get());
            } else {
                DestinationAddressWildcarded dstIpv4Wildcarded
                        = new DestinationAddressWildcardedBuilder(parseIpv4Wildcarded(words)).build();
                ipv4WildcardedAugBuilder.setDestinationAddressWildcarded(dstIpv4Wildcarded);
                ipv4ProtocolFieldsConfigBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                        ipv4WildcardedAugBuilder.build());
            }
            specification = words.poll();
        }

        if ("source-port".equals(specification)) {
            parseTransportSourcePort(transportConfigBuilder, words);
        } else if ("destination-port".equals(specification)) {
            parseTransportDestinationPort(transportConfigBuilder, words);
        } else {
            return new ParseIpv4LineResult(ipv4ProtocolFieldsConfigBuilder.build(), null, null);
        }

        Transport transport = new TransportBuilder().setConfig(transportConfigBuilder.build()).build();

        // if there are some unsupported expressions, ACL cannot be parsed at all
        if (!words.isEmpty()) {
            throw new IllegalArgumentException("ACL entry contains unsupported expressions that cannot be parsed: "
                    + words);
        }
        return new ParseIpv4LineResult(ipv4ProtocolFieldsConfigBuilder.build(), transport, null);
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
        String port2String = "range".equals(possiblePortRangeKeyword) ? words.peek() : null;

        boolean arePortsNumeric = port2String == null
                ? StringUtils.isNumeric(port1String) : StringUtils.isNumeric(port1String)
                && StringUtils.isNumeric(port2String);

        if (arePortsNumeric) {
            int port1 = serviceToPortNumber(port1String);

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

    private static IpProtocolType parseProtocol(String protocol) {
        switch (protocol) {
            case "ip":
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
        if (words.contains("0")) {
            String ip = words.poll();
            String mask = words.poll();
            return Optional.of(new Ipv4Prefix(ip + "/" + mask));
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
}