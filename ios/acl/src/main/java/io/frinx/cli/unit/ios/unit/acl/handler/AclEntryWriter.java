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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEstablishedStateAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclOptionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclPrecedenceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv6AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.IpProtocolFieldsCommonConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.Ipv4ProtocolFieldsConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.Ipv6ProtocolFieldsConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryWriter implements CliListWriter<AclEntry, AclEntryKey> {

    private static final int MAX_TTL = 255;
    private static final int MAX_PORT = 65535;
    private static final String RANGE_SEPARATOR = "..";
    private static final Function<String, String> WRONG_RANGE_FORMAT_MSG = rangeString -> String.format(
            "incorrect range format, range parameter should contains two numbers separated by '%s', entered: %s",
            RANGE_SEPARATOR, rangeString);
    private static final String ANY = "any";
    public static final String SOURCE_ADDRESS_AND_SOURCE_ADDRESS_WILDCARDED_TOGETHER_ERROR
            = "source-address and source-address-wildcarded cannot be defined in ACL together";
    public static final String NONE_SOURCE_ADDRESS_OR_SOURCE_ADDRESS_WILDCARDED_ERROR
            = "source-address or source-address-wildcarded must be defined in ACL";
    public static final String SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR = "source-address-wildcarded must "
            + "contain address and wildcard-mask";
    public static final String DESTINATION_ADDRESS_AND_DESTINATION_ADDRESS_WILDCARDED_TOGETHER_ERROR =
            "destination-address and destination-address-wildcarded cannot be defined in ACL together";
    public static final String NONE_DESTINATION_ADDRESS_OR_DESTINATION_ADDRESS_WILDCARDED_ERROR =
            "destination-address or destination-address-wildcarded must be defined in ACL";
    public static final String DESTINATION_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR = "destination-address-wildcarded "
            + "must contain address and wildcard-mask";

    private static final String ACL_STANDARD_ENTRY = """
            configure terminal
            ip access-list standard {$aclName}
            {$aclSeqId} {$aclFwdAction} {$aclSrcAddr}
            end
            """;
    private static final String ACL_IP_ENTRY = """
            configure terminal
            ip access-list extended {$aclName}
            {$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$precedence} {$options} {$aclTtl}
            end
            """;
    private static final String ACL_IP6_ENTRY = """
            configure terminal
            ipv6 access-list {$aclName}
            {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclTtl} {$aclSeqId}
            end
            """;

    @SuppressWarnings("checkstyle:linelength")
    private static final String ACL_TCP_ENTRY = """
            configure terminal
            ip access-list extended {$aclName}
            {$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclSrcPort} {$aclDstAddr} {$aclDstPort} {$established} {$precedence} {$options} {$aclTtl}
            end
            """;
    private static final String ACL_TCP_IP6_ENTRY = """
            configure terminal
            ipv6 access-list {$aclName}
            {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclSrcPort} {$aclDstAddr} {$aclDstPort} {$aclTtl} {$aclSeqId}
            end
            """;
    @SuppressWarnings("checkstyle:linelength")
    private static final String ACL_ICMP_ENTRY = """
            configure terminal
            ip access-list extended {$aclName}
            {$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclIcmpMsgType} {$precedence} {$options} {$aclTtl}
            end
            """;
    private static final String ACL_ICMP_IP6_ENTRY = """
            configure terminal
            ipv6 access-list {$aclName}
            {$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclIcmpMsgType}
            end
            """;
    private static final String ACL_STANDARD_DELETE = """
            configure terminal
            ip access-list standard {$aclName}
            no {$aclSeqId}
            end
            """;
    private static final String ACL_EXTENDED_DELETE = """
            configure terminal
            ip access-list extended {$aclName}
            no {$aclSeqId}
            end
            """;
    private static final String ACL_IP6_DELETE = """
            configure terminal
            ipv6 access-list {$aclName}
            no sequence {$aclSeqId}
            end
            """;

    private static final Pattern PORT_RANGE_PATTERN = Pattern.compile("(?<from>\\d*)..(?<to>\\d*)");
    private static final Pattern PORT_RANGE_NAMED_PATTERN = Pattern.compile("(?<from>\\S*)\\.\\.(?<to>\\S*)");
    private static final Pattern PORT_RANGE_MULTIPLE_PATTERN = Pattern.compile("\\S+ \\S+.*");
    private static final Pattern IPV4_IN_IPV6_PATTERN =
            Pattern.compile("^(?<ipv6Part>.+:(ffff|FFFF):)(?<ipv4Part>[0-9a-fA-F]{1,4}:[0-9a-fA-F]{1,4})$");

    private static final Map<Class<? extends ACLTYPE>, String> DELETE_COMMANDS = ImmutableMap.of(
            ACLIPV4STANDARD.class, ACL_STANDARD_DELETE,
            ACLIPV4EXTENDED.class, ACL_EXTENDED_DELETE,
            ACLIPV6.class, ACL_IP6_DELETE
    );

    private final Cli cli;

    public AclEntryWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<AclEntry> id,
                                       @NotNull AclEntry entry,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        processChange(id, entry);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<AclEntry> id,
                                        @NotNull AclEntry dataBefore,
                                        @NotNull AclEntry dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<AclEntry> id,
                                        @NotNull AclEntry dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String command = DELETE_COMMANDS.get(aclSetKey.getType());
        final String aclName = aclSetKey.getName();
        final String aclSequenceId = dataBefore.getSequenceId().toString();

        blockingWriteAndRead(fT(command, "aclName", aclName, "aclSeqId", aclSequenceId), cli, id, dataBefore);
    }

    private void processChange(@NotNull InstanceIdentifier<AclEntry> id,
                               @NotNull AclEntry entry) throws WriteFailedException.CreateFailedException {
        MaxMetricCommandDTO commandVars = new MaxMetricCommandDTO();
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        commandVars.aclName = aclSetKey.getName();
        commandVars.aclSeqId = entry.getSequenceId().toString();
        // ipv4|ipv6
        if (entry.getIpv4() != null) {
            processIpv4(entry, commandVars, aclSetKey.getType());
            commandVars.aclPrecedence = formatPrecedence(entry);
            commandVars.aclOptions = formatOptions(entry);
            commandVars.aclEstablished = formatEstablishedState(entry);
        } else if (entry.getIpv6() != null) {
            processIpv6(entry, commandVars);
            commandVars.aclSeqId = " sequence " + entry.getSequenceId().toString();
        } else {
            throw new IllegalStateException(f("No ip|ipv6 container found in acl entry %s", entry));
        }
        // transport
        processTransport(entry, commandVars);

        // actions
        processActions(entry, commandVars);

        switch (commandVars.aclProtocol) {
            case "ip":
                blockingWriteAndRead(fT(ACL_IP_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclTtl", commandVars.aclTtl,
                        "precedence", commandVars.aclPrecedence,
                        "options", commandVars.aclOptions),
                        cli, id, entry);
                break;
            case "ipv6":
                blockingWriteAndRead(fT(ACL_IP6_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclTtl", commandVars.aclTtl),
                        cli, id, entry);
                break;
            case "udp":
            case "tcp":
                blockingWriteAndRead(fT((entry.getIpv4() != null) ? ACL_TCP_ENTRY : ACL_TCP_IP6_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclSrcPort", commandVars.aclSrcPort,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclDstPort", commandVars.aclDstPort,
                        "aclTtl", commandVars.aclTtl,
                        "established", commandVars.aclEstablished,
                        "precedence", commandVars.aclPrecedence,
                        "options", commandVars.aclOptions),
                        cli, id, entry);
                break;
            case "icmp":
                blockingWriteAndRead(fT((entry.getIpv4() != null) ? ACL_ICMP_ENTRY : ACL_ICMP_IP6_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclIcmpMsgType", commandVars.aclIcmpMsgType,
                        "aclTtl", commandVars.aclTtl,
                        "precedence", commandVars.aclPrecedence,
                        "options", commandVars.aclOptions),
                        cli, id, entry);
                break;
            case "":
                if (ACLIPV4STANDARD.class.equals(aclSetKey.getType())) {
                    blockingWriteAndRead(fT(ACL_STANDARD_ENTRY,
                            "aclName", commandVars.aclName,
                            "aclSeqId", commandVars.aclSeqId,
                            "aclFwdAction", commandVars.aclFwdAction,
                            "aclSrcAddr", commandVars.aclSrcAddr),
                            cli, id, entry);
                }
                break;
            default: break;
        }
    }

    private void processIpv4(AclEntry entry, MaxMetricCommandDTO commandVars, Class<? extends ACLTYPE> aclType) {
        if (entry.getIpv4().getConfig().getAugmentation(Config3.class) != null
                && entry.getIpv4().getConfig().getAugmentation(Config3.class).getHopRange() != null) {
            commandVars.aclTtl =
                    formatTTL(entry.getIpv4().getConfig().getAugmentation(Config3.class).getHopRange().getValue());
        }
        if (entry.getAugmentation(AclEntry1.class) != null
                && entry.getAugmentation(AclEntry1.class).getIcmp() != null) {
            commandVars.aclIcmpMsgType =
                    entry.getAugmentation(AclEntry1.class).getIcmp().getConfig().getMsgType().getUint8().toString();
        }
        // src address
        Optional<String> ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getSourceAddress);
        Optional<Ipv4AddressWildcarded> ipv4WildcardedOpt = getIpv4Wildcarded(entry,
                AclSetAclEntryIpv4WildcardedAug::getSourceAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv4WildcardedOpt.isPresent(),
                    SOURCE_ADDRESS_AND_SOURCE_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclSrcAddr = info.getAddress() + " " + AclUtil.getWildcardfromSubnet(info.getNetmask());
        } else {
            Preconditions.checkArgument(ipv4WildcardedOpt.isPresent(),
                    NONE_SOURCE_ADDRESS_OR_SOURCE_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclSrcAddr = ipv4WildcardedOpt.get().getAddress().getValue() + " " + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }
        // standard ACL does not have destination address nor protocols
        if (ACLIPV4STANDARD.class.equals(aclType)) {
            return;
        }
        // dst address
        ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getDestinationAddress);
        ipv4WildcardedOpt = getIpv4Wildcarded(entry, AclSetAclEntryIpv4WildcardedAug::getDestinationAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv4WildcardedOpt.isPresent(),
                    DESTINATION_ADDRESS_AND_DESTINATION_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclDstAddr = info.getAddress() + " " + AclUtil.getWildcardfromSubnet(info.getNetmask());
        } else {
            Preconditions.checkArgument(ipv4WildcardedOpt.isPresent(),
                    NONE_DESTINATION_ADDRESS_OR_DESTINATION_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    DESTINATION_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclDstAddr = ipv4WildcardedOpt.get().getAddress().getValue() + " " + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }

        IpProtocolType ipProtocolType = Optional.ofNullable(entry.getIpv4().getConfig())
                .map(IpProtocolFieldsCommonConfig::getProtocol)
                .orElse(null);
        commandVars.aclProtocol = formatProtocol(ipProtocolType, "ip");
    }

    private Optional<String> getIpv4Prefix(AclEntry entry, Function<Ipv4ProtocolFieldsConfig, Ipv4Prefix> mapper) {
        return Optional.ofNullable(entry)
                .map(AclEntry::getIpv4)
                .map(Ipv4::getConfig)
                .map(mapper)
                .map(Ipv4Prefix::getValue);
    }

    private Optional<Ipv4AddressWildcarded> getIpv4Wildcarded(AclEntry entry,
                                                              Function<AclSetAclEntryIpv4WildcardedAug,
                                                                      Ipv4AddressWildcarded> mapper) {
        return Optional.ofNullable(entry)
                .map(AclEntry::getIpv4)
                .map(Ipv4::getConfig)
                .map(config -> config.getAugmentation(AclSetAclEntryIpv4WildcardedAug.class))
                .map(mapper);
    }

    @VisibleForTesting
    static void processIpv6(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getIpv6().getConfig().getAugmentation(Config4.class) != null
                && entry.getIpv6().getConfig().getAugmentation(Config4.class).getHopRange() != null) {
            commandVars.aclTtl =
                    formatTTL(entry.getIpv6().getConfig().getAugmentation(Config4.class).getHopRange().getValue());
        }
        if (entry.getAugmentation(AclEntry1.class) != null
                && entry.getAugmentation(AclEntry1.class).getIcmp() != null) {
            commandVars.aclIcmpMsgType =
                    entry.getAugmentation(AclEntry1.class).getIcmp().getConfig().getMsgType().getUint8().toString();
        }
        // src address
        Optional<String> ipv6PrefixOpt = getIpv6Prefix(entry, Ipv6ProtocolFieldsConfig::getSourceAddress);
        Optional<Ipv6AddressWildcarded> ipv6WildcardedOpt = getIpv6Wildcarded(entry,
                AclSetAclEntryIpv6WildcardedAug::getSourceAddressWildcarded);
        if (ipv6PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv6WildcardedOpt.isPresent(),
                    SOURCE_ADDRESS_AND_SOURCE_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            commandVars.aclSrcAddr = tryTranslateIpv6ToIpv4InIpv6(ipv6PrefixOpt.get());
        } else {
            Preconditions.checkArgument(ipv6WildcardedOpt.isPresent(),
                    NONE_SOURCE_ADDRESS_OR_SOURCE_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv6WildcardedOpt.get().getAddress() != null && ipv6WildcardedOpt.get()
                            .getWildcardMask() != null,
                    SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclSrcAddr = ipv6WildcardedOpt.get().getAddress().getValue() + " " + ipv6WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }
        // dst address
        ipv6PrefixOpt = getIpv6Prefix(entry, Ipv6ProtocolFieldsConfig::getDestinationAddress);
        ipv6WildcardedOpt = getIpv6Wildcarded(entry, AclSetAclEntryIpv6WildcardedAug::getDestinationAddressWildcarded);
        if (ipv6PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv6WildcardedOpt.isPresent(),
                    DESTINATION_ADDRESS_AND_DESTINATION_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            commandVars.aclDstAddr = tryTranslateIpv6ToIpv4InIpv6(ipv6PrefixOpt.get());
        } else {
            Preconditions.checkArgument(ipv6WildcardedOpt.isPresent(),
                    NONE_DESTINATION_ADDRESS_OR_DESTINATION_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv6WildcardedOpt.get().getAddress() != null && ipv6WildcardedOpt.get()
                            .getWildcardMask() != null,
                    DESTINATION_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclDstAddr = ipv6WildcardedOpt.get().getAddress().getValue() + " " + ipv6WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }

        IpProtocolType ipProtocolType = Optional.ofNullable(entry.getIpv6().getConfig())
                .map(IpProtocolFieldsCommonConfig::getProtocol)
                .orElse(null);
        commandVars.aclProtocol = formatProtocol(ipProtocolType, "ipv6");
    }

    private static String tryTranslateIpv6ToIpv4InIpv6(String ipv6Prefix) {
        Pattern ipv6Pattern = Pattern.compile(Ipv6Prefix.PATTERN_CONSTANTS.get(0));
        Matcher ipv6PrefixMatcher = ipv6Pattern.matcher(ipv6Prefix);
        Preconditions.checkArgument(ipv6PrefixMatcher.matches(), "unknown IPv6 with prefix: " + ipv6Prefix);
        String ipv6 = ipv6PrefixMatcher.group(1);
        String prefix = ipv6PrefixMatcher.group(17);
        Matcher ipv6Matcher = IPV4_IN_IPV6_PATTERN.matcher(ipv6);
        // ipv4 in ipv6 must have prefix 96 and must match the pattern
        if (!"96".equals(prefix) || !ipv6Matcher.matches()) {
            return ipv6Prefix;
        }

        String ipv4PartFromIpv6 = ipv6Matcher.group("ipv4Part");
        String ipv4 = InetAddresses.forString("::ffff:" + ipv4PartFromIpv6).toString().substring(1);
        String ipv6PartFromIpv6 = ipv6Matcher.group("ipv6Part");

        return ipv6PartFromIpv6 + ipv4 + "/" + prefix;
    }

    private static Optional<String> getIpv6Prefix(AclEntry entry,
                                                  Function<Ipv6ProtocolFieldsConfig, Ipv6Prefix> mapper) {
        return Optional.ofNullable(entry)
                .map(AclEntry::getIpv6)
                .map(Ipv6::getConfig)
                .map(mapper)
                .map(Ipv6Prefix::getValue);
    }

    private static Optional<Ipv6AddressWildcarded> getIpv6Wildcarded(AclEntry entry,
                                                              Function<AclSetAclEntryIpv6WildcardedAug,
                                                                      Ipv6AddressWildcarded> mapper) {
        return Optional.ofNullable(entry)
                .map(AclEntry::getIpv6)
                .map(Ipv6::getConfig)
                .map(config -> config.getAugmentation(AclSetAclEntryIpv6WildcardedAug.class))
                .map(mapper);
    }

    private void processTransport(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getTransport() != null && entry.getTransport().getConfig() != null) {
            AclSetAclEntryTransportPortNamedAug aug = entry.getTransport().getConfig()
                    .getAugmentation(AclSetAclEntryTransportPortNamedAug.class);

            if (entry.getTransport().getConfig().getSourcePort() != null) {
                commandVars.aclSrcPort = formatPort(entry.getTransport().getConfig().getSourcePort());
            } else if (aug != null && aug.getSourcePortNamed() != null) {
                commandVars.aclSrcPort = formatNamedPort(aug.getSourcePortNamed());
            }

            if (entry.getTransport().getConfig().getDestinationPort() != null) {
                commandVars.aclDstPort = formatPort(entry.getTransport().getConfig().getDestinationPort());
            } else if (aug != null && aug.getDestinationPortNamed() != null) {
                commandVars.aclDstPort = formatNamedPort(aug.getDestinationPortNamed());
            }
        }
    }

    private void processActions(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getActions() == null || entry.getActions().getConfig() == null) {
            throw new IllegalStateException(f("No actions found for entry %s", entry));
        }
        Class<? extends FORWARDINGACTION> action = entry.getActions().getConfig().getForwardingAction();
        if (action.equals(ACCEPT.class)) {
            commandVars.aclFwdAction = "permit";
        } else if (action.equals(DROP.class)) {
            commandVars.aclFwdAction = "deny";
        } else {
            throw new IllegalStateException(f("No action found for entry %s", entry));
        }
    }

    private static String formatProtocol(@Nullable IpProtocolType ipProtocolType, String type) {
        if (ipProtocolType == null) {
            return type;
        }

        Class<? extends IPPROTOCOL> protocol = ipProtocolType.getIdentityref();
        if (protocol == null) {
            return String.valueOf(ipProtocolType.getUint8());
        }

        if (protocol.equals(IPUDP.class)) {
            return "udp";
        } else if (protocol.equals(IPTCP.class)) {
            return "tcp";
        } else if (protocol.equals(IPICMP.class)) {
            return "icmp";
        }
        LOG.warn("Unknown protocol {}", protocol);
        throw new IllegalArgumentException("ACL contains unsupported protocol: " + protocol.getSimpleName());
    }

    private String formatPort(PortNumRange port) {
        if (port.getPortNumber() != null) {
            return f("eq %s", port.getPortNumber().getValue());
        } else if (port.getString() != null) {
            if (ANY.equalsIgnoreCase(port.getString())) {
                return "";
            }

            Matcher matcher = PORT_RANGE_PATTERN.matcher(port.getString());
            if (!matcher.find()) {
                LOG.warn("Wrong protocol range value: {}", port.getString());
                return "";
            }
            int from = Integer.parseInt(matcher.group("from"));
            int to = Integer.parseInt(matcher.group("to"));
            if (from == 0) {
                return f("lt %s", to);
            }
            if (to == MAX_PORT) {
                return f("gt %s", from);
            }
            if (from > to && from - 1 == to + 1) {
                return f("neq %s", from);
            }
            if (from < to || from == to) {
                return f("range %s %s", from, to);
            }
            LOG.warn("Wrong protocol range value: {}", port.getString());
            return "";
        }

        return "";
    }

    private String formatNamedPort(String port) {
        var namedRangeMatcher = PORT_RANGE_NAMED_PATTERN.matcher(port);
        var multipleRangeMatcher = PORT_RANGE_MULTIPLE_PATTERN.matcher(port);
        if (multipleRangeMatcher.find()) {
            return formatMultiplePort(port);
        }
        if (namedRangeMatcher.find()) {
            var from = namedRangeMatcher.group("from");
            var to = namedRangeMatcher.group("to");
            if (from.equals("0")) {
                return f("lt %s", to);
            }
            if (to.equals(Integer.toString(MAX_PORT))) {
                return f("gt %s", from);
            }
            if (StringUtils.isNumeric(from) && StringUtils.isNumeric(to)) {
                Integer fr = Integer.valueOf(from);
                Integer toI = Integer.valueOf(to);

                if (fr > toI && fr - 1 == toI + 1) {
                    return f("neq %s", from);
                }
            }

            return f("range %s %s", from, to);
        } else {
            if (ANY.equalsIgnoreCase(port)) {
                return "";
            } else {
                return f("eq %s", port);
            }
        }
    }

    private static String formatEstablishedState(AclEntry entry) {
        return (isTransportConfig(entry) && entry.getTransport().getConfig()
                .getAugmentation(AclEstablishedStateAug.class) != null
                && entry.getTransport().getConfig().getAugmentation(AclEstablishedStateAug.class).isEstablished())
                ? "established"
                : "";
    }

    private static String formatPrecedence(AclEntry entry) {
        if (entry.getAugmentation(AclPrecedenceAug.class) != null
                && entry.getAugmentation(AclPrecedenceAug.class).getPrecedence() != null) {
            return "precedence " + entry.getAugmentation(AclPrecedenceAug.class).getPrecedence()
                    .getName().toLowerCase(Locale.ROOT);
        }
        return "";
    }

    private static String formatOptions(AclEntry entry) {
        if (entry.getAugmentation(AclOptionAug.class) != null
                && entry.getAugmentation(AclOptionAug.class).getOption() != null) {
            var aug = entry.getAugmentation(AclOptionAug.class);
            if (aug.getOption().getEnumeration() != null && aug.getOption().getUint8() != null) {
                throw new IllegalStateException("Unsupported type...");
            }
            if (aug.getOption().getUint8() != null) {
                return "option " + aug.getOption().getUint8().toString();
            } else if (aug.getOption().getEnumeration() != null) {
                return "option " + aug.getOption().getEnumeration().getName().toLowerCase(Locale.ROOT);
            }
        }
        return "";
    }

    private static String formatTTL(final String rangeString) {
        Preconditions.checkArgument(rangeString.contains(RANGE_SEPARATOR), "incorrect range format %s", rangeString);
        final String[] rangeParams = rangeString.split("\\.\\.");
        Preconditions.checkArgument(rangeParams.length == 2, WRONG_RANGE_FORMAT_MSG.apply(rangeString));
        final int minRangeParam;
        final int maxRangeParam;
        try {
            minRangeParam = Integer.parseInt(rangeParams[0]);
            maxRangeParam = Integer.parseInt(rangeParams[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_RANGE_FORMAT_MSG.apply(rangeString), e);
        }

        String ttlString = "ttl";
        if (minRangeParam == maxRangeParam) {
            ttlString = ttlString + " eq " + minRangeParam;
        } else if (minRangeParam < maxRangeParam) {
            if (minRangeParam == 0) {
                ttlString = ttlString + " lt " + (maxRangeParam + 1);
            } else if (maxRangeParam == MAX_TTL) {
                ttlString = ttlString + " gt " + (minRangeParam - 1);
            } else {
                ttlString = ttlString + " range " + minRangeParam + " " + maxRangeParam;
            }
        } else {
            // minRangeParam > maxRangeParam
            // |0, .........., maxRangeParam, minParamRange, .........., MAX_TTL|
            Preconditions.checkArgument(minRangeParam - maxRangeParam == 2,
                    "incorrect range param for 'neq', first range param has to be greater then second by 2, entered: "
                            +
                            "%s",
                    rangeString);
            ttlString = ttlString + " neq " + (minRangeParam - 1);
        }

        return ttlString;
    }

    private String formatMultiplePort(String port) {
        List<String> ports = Arrays.stream(port.split(" ")).collect(Collectors.toList());
        var neq = Pattern.compile(" ").splitAsStream(port)
                .map(PORT_RANGE_NAMED_PATTERN::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .isPresent();
        var stringBuilder = new StringBuilder();
        if (neq) {
            formatMultipleNeq(stringBuilder, ports);
            return stringBuilder.toString();
        } else {
            stringBuilder.append("eq");
            ports.forEach(item -> {
                stringBuilder.append(" ").append(item);
            });
            return stringBuilder.toString();
        }
    }

    private static void formatMultipleNeq(StringBuilder stringBuilder, List<String> ports) {
        String from;
        String to;
        String prev = null;
        stringBuilder.append("neq");
        for (var i = 0; i < ports.size(); i++) {
            var matcher = PORT_RANGE_NAMED_PATTERN.matcher(ports.get(i));
            if (matcher.find()) {
                from = matcher.group("from");
                to = matcher.group("to");
            } else {
                from = ports.get(i);
                to = from;
            }
            if ((i == 0) && (!from.equals("0"))) {
                createInterval(0, Integer.parseInt(from) - 1, stringBuilder);
            } else if (i != 0) {
                createInterval(Integer.parseInt(prev) + 1, Integer.parseInt(from) - 1, stringBuilder);
            }
            if ((i == (ports.size() - 1)) && (!to.equals(Integer.toString(MAX_PORT)))) {
                createInterval(Integer.parseInt(to) + 1, MAX_PORT, stringBuilder);
            }
            prev = to;
        }
    }

    private static void createInterval(int from, int to, StringBuilder stringBuilder) {
        if (from == to) {
            stringBuilder.append(" ").append(from);
        } else {
            for (var i = from; i <= to; i++) {
                stringBuilder.append(" ").append(i);
            }
        }
    }

    private static boolean isTransportConfig(AclEntry entry) {
        return entry != null && entry.getTransport() != null && entry.getTransport().getConfig() != null;
    }

    @VisibleForTesting
    static class MaxMetricCommandDTO {
        String aclName = "";
        String aclSeqId = "";
        String aclFwdAction = "";
        String aclProtocol = "";
        String aclSrcAddr = "";
        String aclSrcPort = "";
        String aclDstAddr = "";
        String aclDstPort = "";
        String aclIcmpMsgType = "";
        String aclTtl = "";
        String aclPrecedence = "";
        String aclOptions = "";
        String aclEstablished = "";
    }
}