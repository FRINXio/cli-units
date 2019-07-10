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
import com.google.common.net.InetAddresses;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv6WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv6AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
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

    private static final String ACL_IP_ENTRY = "configure terminal\n"
            + "ip access-list extended {$aclName}\n"
            + "{$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclTtl}\n"
            + "end\n";
    private static final String ACL_IP6_ENTRY = "configure terminal\n"
            + "ipv6 access-list {$aclName}\n"
            + "{$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclTtl} {$aclSeqId}\n"
            + "end\n";
    private static final String ACL_TCP_ENTRY = "configure terminal\n"
            + "ip access-list extended {$aclName}\n"
            + "{$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclSrcPort} {$aclDstAddr} "
            + "{$aclDstPort} {$aclTtl}\n"
            + "end\n";
    private static final String ACL_TCP_IP6_ENTRY = "configure terminal\n"
            + "ipv6 access-list {$aclName}\n"
            + "{$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclSrcPort} {$aclDstAddr} {$aclSeqId} "
            + "{$aclDstPort} {$aclTtl}\n"
            + "end\n";
    private static final String ACL_ICMP_ENTRY = "configure terminal\n"
            + "ip access-list extended {$aclName}\n"
            + "{$aclSeqId} {$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclIcmpMsgType} "
            + "{$aclTtl}\n"
            + "end\n";
    private static final String ACL_ICMP_IP6_ENTRY = "configure terminal\n"
            + "ipv6 access-list {$aclName}\n"
            + "{$aclFwdAction} {$aclProtocol} {$aclSrcAddr} {$aclDstAddr} {$aclIcmpMsgType} {$aclSeqId} "
            + "{$aclTtl}\n"
            + "end\n";
    private static final String ACL_DELETE = "configure terminal\n"
            + "ip access-list extended {$aclName}\n"
            + "no {$aclSeqId}\n"
            + "end\n";
    private static final String ACL_IP6_DELETE = "configure terminal\n"
            + "ipv6 access-list {$aclName}\n"
            + "no sequence {$aclSeqId}\n"
            + "end\n";

    private static final Pattern PORT_RANGE_PATTERN = Pattern.compile("(?<from>\\d*)..(?<to>\\d*)");
    private static final Pattern PORT_RANGE_NAMED_PATTERN = Pattern.compile("(?<from>\\S*)\\.\\.(?<to>\\S*)");
    private static final Pattern IPV4_IN_IPV6_PATTERN =
            Pattern.compile("^(?<ipv6Part>.+:(ffff|FFFF):)(?<ipv4Part>[0-9a-fA-F]{1,4}:[0-9a-fA-F]{1,4})$");

    private final Cli cli;

    public AclEntryWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                       @Nonnull AclEntry entry,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        processChange(id, entry);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                        @Nonnull AclEntry dataBefore,
                                        @Nonnull AclEntry dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        //overwrite the entry since sequence-id is the same
        processChange(id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                        @Nonnull AclEntry dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String command = aclSetKey.getType().equals(ACLIPV4.class) ? ACL_DELETE : ACL_IP6_DELETE;
        final String aclName = aclSetKey.getName();
        final String aclSequenceId = dataBefore.getSequenceId().toString();

        blockingWriteAndRead(fT(command, "aclName", aclName, "aclSeqId", aclSequenceId), cli, id, dataBefore);
    }

    private void processChange(@Nonnull InstanceIdentifier<AclEntry> id,
                               @Nonnull AclEntry entry) throws WriteFailedException.CreateFailedException {
        MaxMetricCommandDTO commandVars = new MaxMetricCommandDTO();
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        commandVars.aclName = aclSetKey.getName();
        commandVars.aclSeqId = entry.getSequenceId().toString();
        // ipv4|ipv6
        if (entry.getIpv4() != null) {
            processIpv4(entry, commandVars);
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
                        "aclTtl", commandVars.aclTtl),
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
                        "aclTtl", commandVars.aclTtl),
                        cli, id, entry);
                break;
            case "icmp":
                blockingWriteAndRead(fT(ACL_ICMP_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclIcmpMsgType", commandVars.aclIcmpMsgType,
                        "aclTtl", commandVars.aclTtl),
                        cli, id, entry);
                break;
            case "icmpv6":
                blockingWriteAndRead(fT(ACL_ICMP_IP6_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclIcmpMsgType", commandVars.aclIcmpMsgType,
                        "aclTtl", commandVars.aclTtl),
                        cli, id, entry);
                break;
            default: break;
        }
    }

    private void processIpv4(AclEntry entry, MaxMetricCommandDTO commandVars) {
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
            commandVars.aclSrcAddr = info.getAddress() + " " + info.getNetmask();
        } else {
            Preconditions.checkArgument(ipv4WildcardedOpt.isPresent(),
                    NONE_SOURCE_ADDRESS_OR_SOURCE_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclSrcAddr = ipv4WildcardedOpt.get().getAddress().getValue() + " " + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }
        // dst address
        ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getDestinationAddress);
        ipv4WildcardedOpt = getIpv4Wildcarded(entry, AclSetAclEntryIpv4WildcardedAug::getDestinationAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv4WildcardedOpt.isPresent(),
                    DESTINATION_ADDRESS_AND_DESTINATION_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclDstAddr = info.getAddress() + " " + info.getNetmask();
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

        if (protocol.equals(IPPROTOCOL.class)) {
            return type;
        } else if (protocol.equals(IPUDP.class)) {
            return "udp";
        } else if (protocol.equals(IPTCP.class)) {
            return "tcp";
        } else if (protocol.equals(IPICMP.class)) {
            switch (type) {
                case "ip":
                    return "icmp";
                case "ipv6":
                    return "icmpv6";
                default:
                    LOG.warn("Unknown protocol {}", protocol);
                    throw new IllegalArgumentException("ACL contains unsupported protocol: "
                            + protocol.getSimpleName());
            }
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
            int from = Integer.valueOf(matcher.group("from"));
            int to = Integer.valueOf(matcher.group("to"));
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
        Matcher matcher = PORT_RANGE_NAMED_PATTERN.matcher(port);
        if (matcher.find()) {
            String from = matcher.group("from");
            String to = matcher.group("to");
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

    private static String formatTTL(final String rangeString) {
        Preconditions.checkArgument(rangeString.contains(RANGE_SEPARATOR), "incorrect range format %s", rangeString);
        final String[] rangeParams = rangeString.split("\\.\\.");
        Preconditions.checkArgument(rangeParams.length == 2, WRONG_RANGE_FORMAT_MSG.apply(rangeString));
        final int minRangeParam;
        final int maxRangeParam;
        try {
            minRangeParam = Integer.valueOf(rangeParams[0]);
            maxRangeParam = Integer.valueOf(rangeParams[1]);
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
    }
}
