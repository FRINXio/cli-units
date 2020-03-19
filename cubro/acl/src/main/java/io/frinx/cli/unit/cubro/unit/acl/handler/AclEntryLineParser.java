/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.unit.acl.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.AclCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.AclCubroAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.COUNT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.ELAG;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.FORWARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.IPANY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.DestinationAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

public final class AclEntryLineParser {

    static final Ipv4Prefix IPV4_HOST_ANY = new Ipv4Prefix("0.0.0.0/0");
    private static final IpProtocolType IP_PROTOCOL_ANY = new IpProtocolType(IPANY.class);

    private AclEntryLineParser() {
    }

    static Optional<String> findAclEntryWithSequenceId(AclEntryKey aclEntryKey, String lines,
                                                       Class<? extends ACLTYPE> aclType) {
        // search for line containing current sequence number
        long sequenceId = aclEntryKey.getSequenceId();
        return findIpv4LineWithSequenceId(sequenceId, lines);
    }

    static Optional<String> findIpv4LineWithSequenceId(long sequenceId, String lines) {
        Pattern pattern = Pattern.compile("^\\s*(" + sequenceId + " .*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(lines);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    static void parseLine(final AclEntryBuilder builder, String line, Class<? extends ACLTYPE> aclType) {

        Preconditions.checkArgument(ACLIPV4.class.equals(aclType),
                "Unsupported ACL type: " + aclType);
        Queue<String> words = Lists.newLinkedList(Arrays.asList(line.trim().split("\\s")));

        parseSequenceId(builder, words.poll());

        // fwd action
        Class<? extends FORWARDINGACTION> fwdAction = parseAction(words.poll());
        AclCubroAugBuilder aclCubroAugBuilder = new AclCubroAugBuilder();

        if (fwdAction.equals(FORWARD.class)) {
            String word = words.poll();
            if (word.equals("elag")) {
                aclCubroAugBuilder.setEgressType(ELAG.class);
                aclCubroAugBuilder.setEgressValue(Long.parseLong(words.poll()));
            } else {
                throw new IllegalArgumentException("Unsupported egress type: " + word);
            }
        } else {
            throw new IllegalArgumentException("Unsupported forwarding action: " + fwdAction);
        }

        // protocol
        IpProtocolType ipProtocolType = parseProtocol(words.poll());

        if (ACLIPV4.class.equals(aclType)) {
            Ipv4Builder ipv4Builder = new Ipv4Builder();
            ParseIpv4LineResult parseIpv4LineResult = parseIpv4Line(ipProtocolType, words, aclCubroAugBuilder);
            ipv4Builder.setConfig(parseIpv4LineResult.ipv4ProtocolFieldsConfig);
            builder.setIpv4(ipv4Builder.build());
        } else if (ACLIPV6.class.equals(aclType)) {
            throw new IllegalArgumentException("IPV6 is unsupported");
        }

        builder.setActions(createActions(fwdAction, aclCubroAugBuilder));
        // if there are some unsupported expressions, ACL cannot be parsed at all
        if (!words.isEmpty()) {
            throw new IllegalArgumentException("ACL entry contains unsupported expressions that cannot be parsed: "
                    + words);
        }
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

    static Actions createActions(Class<? extends FORWARDINGACTION> fwdAction, AclCubroAugBuilder aclCubroAugBuilder) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setForwardingAction(fwdAction);
        configBuilder.addAugmentation(AclCubroAug.class, aclCubroAugBuilder.build());
        return new ActionsBuilder().setConfig(configBuilder.build()).build();
    }

    private static Class<? extends FORWARDINGACTION> parseAction(String action) {
        switch (action) {
            case "forward":
                return FORWARD.class;
            default:
                throw new IllegalArgumentException("Did not match forwarding action for: " + action);
        }
    }

    private static class ParseIpv4LineResult {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol
                .fields.top.ipv4.Config ipv4ProtocolFieldsConfig;

        ParseIpv4LineResult(
                Config ipv4ProtocolFieldsConfig) {
            this.ipv4ProtocolFieldsConfig = ipv4ProtocolFieldsConfig;
        }
    }

    private static ParseIpv4LineResult parseIpv4Line(IpProtocolType ipProtocolType,
                                                     Queue<String> words, AclCubroAugBuilder aclCubroAugBuilder) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top
                .ipv4.ConfigBuilder ipv4ProtocolFieldsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        ipv4ProtocolFieldsConfigBuilder.setProtocol(ipProtocolType);

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

        // dst address
        Optional<Ipv4Prefix> dstIpv4PrefixOpt = parseIpv4Prefix(words);
        if (dstIpv4PrefixOpt.isPresent()) {
            ipv4ProtocolFieldsConfigBuilder.setDestinationAddress(dstIpv4PrefixOpt.get());
        } else {
            DestinationAddressWildcarded dstIpv4Wildcarded
                    = new DestinationAddressWildcardedBuilder(parseIpv4Wildcarded(words)).build();
            ipv4WildcardedAugBuilder.setDestinationAddressWildcarded(dstIpv4Wildcarded);
        }

        // src dst wildcarded address
        if (ipv4WildcardedAugBuilder.getSourceAddressWildcarded() != null
                || ipv4WildcardedAugBuilder.getDestinationAddressWildcarded() != null) {
            ipv4ProtocolFieldsConfigBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class,
                    ipv4WildcardedAugBuilder.build());
        }

        // cubro-operations
        if (Objects.equals(words.poll(), "count")) {
            aclCubroAugBuilder.setOperation(COUNT.class);
        }

        // if there are some unsupported expressions, ACL cannot be parsed at all
        if (!words.isEmpty()) {
            throw new IllegalArgumentException("ACL entry contains unsupported expressions that cannot be parsed: "
                    + words);
        }
        return new ParseIpv4LineResult(ipv4ProtocolFieldsConfigBuilder.build());
    }

    private static IpProtocolType parseProtocol(String protocol) {
        switch (protocol) {
            case "any":
                return IP_PROTOCOL_ANY;
            default:
                throw new IllegalArgumentException("IP protocol with following identifier is not supported: "
                        + protocol);
        }
    }

    private static Optional<Ipv4Prefix> parseIpv4Prefix(Queue<String> words) {
        String ip = words.peek();
        if ("any".equals(ip)) {
            // remove "any" from queue
            words.remove();
            return Optional.of(IPV4_HOST_ANY);
        } else if (ip != null && !ip.contains("/")) {
            // when "host" is used, the wildcard is always 0.0.0.0
            int mask = 32;
            return Optional.of(new Ipv4Prefix(ip + "/" + mask));
        }
        return Optional.empty();
    }

    private static Ipv4AddressWildcarded parseIpv4Wildcarded(Queue<String> words) {
        String address = words.poll();
        List<String> parts = Arrays.asList(address.split("/"));
        return new SourceAddressWildcardedBuilder().setAddress(new Ipv4Address(parts.get(0)))
                .setWildcardMask(new Ipv4Address(parts.get(1)))
                .build();
    }
}