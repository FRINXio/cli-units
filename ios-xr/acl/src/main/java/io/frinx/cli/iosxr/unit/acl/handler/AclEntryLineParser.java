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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.HopRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.ADDRESSFAMILY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.PortNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AclEntryLineParser {
    private static final Logger LOG = LoggerFactory.getLogger(AclEntryLineParser.class);
    static final Ipv4Prefix IPV4_HOST_ANY = new Ipv4Prefix("0.0.0.0/0");
    static final Ipv6Prefix IPV6_HOST_ANY = new Ipv6Prefix("::/0");
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int MAX_TTL = 255;
    private static final IpProtocolType IP_PROTOCOL_ICMP = new IpProtocolType(IPICMP.class);
    private static final IpProtocolType IP_PROTOCOL_TCP = new IpProtocolType(IPTCP.class);
    private static final IpProtocolType IP_PROTOCOL_UDP = new IpProtocolType(IPUDP.class);
    private static final IpProtocolType IP_PROTOCOL_IP = new IpProtocolType(IPPROTOCOL.class);
    public static final Pattern ZERO_TO_255_PATTERN = Pattern.compile("^2[0-5][0-5]|2[0-4][0-9]|1?[0-9]?[0-9]$");

    static Optional<String> findAclEntryWithSequenceId(InstanceIdentifier<?> id, String lines) {
        // search for line containing current sequence number
        AclEntryKey entryKey = id.firstKeyOf(AclEntry.class);
        long sequenceId = entryKey.getSequenceId();
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

    static AclEntry parseLine(String line, Class<? extends ADDRESSFAMILY> addressFamily) {
        checkArgument(IPV4.class.equals(addressFamily) || IPV6.class.equals(addressFamily),
                "Unsupported address family " + addressFamily);
        Queue<String> words = Lists.newLinkedList(Arrays.asList(line.split("\\s")));
        long sequenceId = Long.parseLong(words.poll());
        AclEntryBuilder builder = new AclEntryBuilder();
        // sequence id
        builder.setSequenceId(sequenceId);
        // fwd action
        Class<? extends FORWARDINGACTION> fwdAction = parseAction(words.poll());
        builder.setActions(createActions(fwdAction));
        // protocol
        IpProtocolType ipProtocolType = parseProtocol(words.poll());
        if (ipProtocolType != null) {
            if (IPV4.class.equals(addressFamily)) {
                Ipv4Builder ipv4Builder = new Ipv4Builder();
                ParseIpv4LineResult parseIpv4LineResult = parseIpv4Line(ipProtocolType, words);
                ipv4Builder.setConfig(parseIpv4LineResult.ipv4ProtocolFieldsConfig);
                builder.setIpv4(ipv4Builder.build());
                builder.setTransport(parseIpv4LineResult.transport);
            } else if (IPV6.class.equals(addressFamily)) {
                Ipv6Builder ipv6Builder = new Ipv6Builder();
                ParseIpv6LineResult parseIpv6LineResult = parseIpv6Line(ipProtocolType, words);
                ipv6Builder.setConfig(parseIpv6LineResult.ipv6ProtocolFieldsConfig);
                builder.setIpv6(ipv6Builder.build());
                builder.setTransport(parseIpv6LineResult.transport);
            } else {
                throw new IllegalArgumentException("Not supported:" + addressFamily);
            }
        }
        return builder.build();
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
                LOG.warn("Did not match forwarding action for {}", action);
                return null;
        }
    }

    private static class ParseIpv4LineResult {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4ProtocolFieldsConfig;
        final Transport transport;

        ParseIpv4LineResult(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config ipv4ProtocolFieldsConfig, Transport transport) {
            this.ipv4ProtocolFieldsConfig = ipv4ProtocolFieldsConfig;
            this.transport = transport;
        }
    }

    private static ParseIpv4LineResult parseIpv4Line(IpProtocolType ipProtocolType, Queue<String> words) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder ipv4ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        ipv4ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();
        boolean addTransport = false;
        // src
        ipv4ProtocolFieldsConfigBuilder.setSourceAddress(parseIpv4Prefix(words));
        if (!words.isEmpty() && isPortNumRange(words.peek())) {
            addTransport = true;
            transportConfigBuilder.setSourcePort(parsePortNumRange(words));
        }
        // dst
        ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(parseIpv4Prefix(words));
        if (!words.isEmpty() && isPortNumRange(words.peek())) {
            addTransport = true;
            transportConfigBuilder.setDestinationPort(parsePortNumRange(words));
        }
        Config1Builder augment = new Config1Builder();
        if (IP_PROTOCOL_ICMP.equals(ipProtocolType)) {
            Optional<Short> maybeMsgType = tryToParseIcmpType(words.peek());
            if (maybeMsgType.isPresent()) {
                words.poll();
                augment.setIcmpMessageType(maybeMsgType.get());
            }

        }
        // ttl
        if (!words.isEmpty() && "ttl".equals(words.peek())) {
            Entry<Integer, Integer> ttlRange = parseTTLRange(words);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            if (lowerEndpoint == 0) {
                // fill the openconfig compliant hop-limit
                ipv4ProtocolFieldsConfigBuilder.setHopLimit((short) upperEndpoint);
            }
            augment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
        }
        if (augment.getHopRange() != null || augment.getIcmpMessageType() != null) {
            ipv4ProtocolFieldsConfigBuilder.addAugmentation(Config1.class, augment.build());
        }
        Transport transport = null;
        if (addTransport) {
            transport = new TransportBuilder().setConfig(transportConfigBuilder.build()).build();
        }
        return new ParseIpv4LineResult(ipv4ProtocolFieldsConfigBuilder.build(), transport);
    }

    private static class ParseIpv6LineResult {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6ProtocolFieldsConfig;
        final Transport transport;

        ParseIpv6LineResult(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.Config ipv6ProtocolFieldsConfig, Transport transport) {
            this.ipv6ProtocolFieldsConfig = ipv6ProtocolFieldsConfig;
            this.transport = transport;
        }
    }

    private static ParseIpv6LineResult parseIpv6Line(IpProtocolType ipProtocolType, Queue<String> words) {

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder ipv6ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder();
        ipv6ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder transportConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.ConfigBuilder();
        boolean addTransport = false;
        // src
        ipv6ProtocolFieldsConfigBuilder.setSourceAddress(parseIpv6Prefix(words));
        if (!words.isEmpty() && isPortNumRange(words.peek())) {
            addTransport = true;
            transportConfigBuilder.setSourcePort(parsePortNumRange(words));
        }
        // dst
        ipv6ProtocolFieldsConfigBuilder.setDestinationAddress(parseIpv6Prefix(words));
        if (!words.isEmpty() && isPortNumRange(words.peek())) {
            addTransport = true;
            transportConfigBuilder.setDestinationPort(parsePortNumRange(words));
        }
        Config2Builder augment = new Config2Builder();
        if (IP_PROTOCOL_ICMP.equals(ipProtocolType)) {
            Optional<Short> maybeMsgType = tryToParseIcmpType(words.peek());
            if (maybeMsgType.isPresent()) {
                words.poll();
                augment.setIcmpMessageType(maybeMsgType.get());
            }
        }
        // ttl
        if (!words.isEmpty() && "ttl".equals(words.peek())) {
            Entry<Integer, Integer> ttlRange = parseTTLRange(words);
            int lowerEndpoint = ttlRange.getKey();
            int upperEndpoint = ttlRange.getValue();
            if (lowerEndpoint == 0) {
                // fill the openconfig compliant hop-limit
                ipv6ProtocolFieldsConfigBuilder.setHopLimit((short) upperEndpoint);
            }
            augment.setHopRange(new HopRange(lowerEndpoint + ".." + upperEndpoint));
        }
        if (augment.getHopRange() != null || augment.getIcmpMessageType() != null) {
            ipv6ProtocolFieldsConfigBuilder.addAugmentation(Config2.class, augment.build());
        }
        Transport transport = null;
        if (addTransport) {
            transport = new TransportBuilder().setConfig(transportConfigBuilder.build()).build();
        }
        return new ParseIpv6LineResult(ipv6ProtocolFieldsConfigBuilder.build(), transport);
    }

    private static Optional<Short> tryToParseIcmpType(String word) {
        if (word == null) {
            return Optional.empty();
        }
        if (ZERO_TO_255_PATTERN.matcher(word).matches()) {
            return Optional.of(Short.parseShort(word));
        } else {
            Short result = ServiceToPortMapping.ICMP_MAPPING.get(word);
            return Optional.ofNullable(result);
        }
    }


    /**
     * Parse ttl (hop range).
     * If words contain ttl, return closed range 0-255..0-255.
     * If ttl neq number is specified, return number+1..number-1.
     */
    private static Entry<Integer, Integer> parseTTLRange(Queue<String> words) {
        String ttl = words.poll();
        checkArgument("ttl".equals(ttl));
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
                throw new IllegalArgumentException("Cannot parse ttl range keyword:" + keyword);
        }
    }

    private static int serviceToPortNumber(String portNumberOrServiceName) {
        try {
            return Integer.parseInt(portNumberOrServiceName);
        } catch (NumberFormatException e) {
            Integer result = ServiceToPortMapping.TCP_MAPPING.get(portNumberOrServiceName);
            if (result != null) {
                return result;
            }
            throw new IllegalArgumentException("Cannot parse port:" + portNumberOrServiceName);
        }
    }

    private static PortNumRange parsePortNumRange(Queue<String> words) {
        String possiblePortRangeKeyword = words.poll();
        int port1 = serviceToPortNumber(words.poll());

        switch (possiblePortRangeKeyword) {
            case "eq":
                return new PortNumRange(new PortNumber(port1));
            case "neq":
                if (port1 == 0) { // >0
                    return createPortNumRangeFromInt(1, MAX_PORT_NUMBER);
                } else if (port1 == MAX_PORT_NUMBER) {// <65535
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
                int port2 =  serviceToPortNumber(words.poll());
                return createPortNumRange(String.valueOf(port1), String.valueOf(port2));
            default:
                throw new IllegalArgumentException("Not a port range keyword:" + possiblePortRangeKeyword);
        }
    }

    private static PortNumRange createPortNumRangeFromInt(int lower, int upper) {
        return createPortNumRange(String.valueOf(lower), String.valueOf(upper));
    }

    private static PortNumRange createPortNumRange(String lower, String upper) {
        return new PortNumRange(lower + ".." + upper);
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
                return IP_PROTOCOL_IP;
            case "udp":
                return IP_PROTOCOL_UDP;
            case "tcp":
                return IP_PROTOCOL_TCP;
            case "icmp":
            case "icmpv6":
                return IP_PROTOCOL_ICMP;
            default:
                LOG.warn("Unknown protocol {}", protocol);
                return null;
        }
    }

    private static Ipv4Prefix parseIpv4Prefix(Queue<String> words) {
        String first = words.poll();
        if ("any".equals(first)) {
            return IPV4_HOST_ANY;
        } else if ("host".equals(first)) {
            int mask = 32;
            String ip = words.poll();
            return new Ipv4Prefix(ip + "/" + mask);
        } else {
            if (!first.contains("/")) {
                first = first + "/32";
            }
            return new Ipv4Prefix(first);
        }
    }


    private static Ipv6Prefix parseIpv6Prefix(Queue<String> words) {
        String first = words.poll();
        if ("any".equals(first)) {
            return IPV6_HOST_ANY;
        } else if ("host".equals(first)) {
            int mask = 128;
            String ip = words.poll();
            return new Ipv6Prefix(ip + "/" + mask);
        } else {
            if (!first.contains("/")) {
                first = first + "/128";
            }
            return new Ipv6Prefix(first);
        }
    }

}
