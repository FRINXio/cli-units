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
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.transport.Config;
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

    private static final String RANGE_SEPARATOR = "..";
    private static final Function<String, String> WRONG_RANGE_FORMAT_MSG = rangeString -> String.format(
            "incorrect range format, range parameter should contains two numbers separated by '%s', entered: %s",
            RANGE_SEPARATOR, rangeString);

    private static final String ACL_ENTRY = "set firewall family {$family} filter {$aclName} term {$aclTermName} ";
    private static final String ACL_ACTION_ENTRY = ACL_ENTRY + "then {$value}\n";
    private static final String ACL_PROTOCOL_ENTRY = ACL_ENTRY + "from protocol {$value}\n";
    private static final String ACL_PROTOCOL_ENTRY_IPV6 = ACL_ENTRY + "from payload-protocol {$value}\n";
    private static final String ACL_ADDR_ENTRY = ACL_ENTRY + "from address {$value}\n";
    private static final String ACL_PORT_ENTRY = ACL_ENTRY + "from port {$value}\n";
    private static final String ACL_SRC_ADDR_ENTRY = ACL_ENTRY + "from source-address {$value}\n";
    private static final String ACL_SRC_PORT_ENTRY = ACL_ENTRY + "from source-port {$value}\n";
    private static final String ACL_DST_ADDR_ENTRY = ACL_ENTRY + "from destination-address {$value}\n";
    private static final String ACL_DST_PORT_ENTRY = ACL_ENTRY + "from destination-port {$value}\n";
    private static final String ACL_TTL_ENTRY = ACL_ENTRY + "from ttl {$value}\n";
    private static final String ACL_ICMP_TYPE_ENTRY = ACL_ENTRY + "from icmp-type {$value}\n";
    private static final String ACL_DELETE_ENTRY =
            "delete firewall family {$family} filter {$aclName} term {$aclTermName}\n";

    private static final Pattern PORT_RANGE_PATTERN = Pattern.compile("(?<from>\\d*)..(?<to>\\d*)");
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
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                        @Nonnull AclEntry dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        final String aclTermName = dataBefore.getConfig().getAugmentation(Config2.class).getTermName();

        blockingWriteAndRead(fT(ACL_DELETE_ENTRY, "family", AclUtil.getStringType(aclSetKey.getType()),
                "aclName", aclName, "aclTermName", aclTermName), cli, id, dataBefore);
    }

    private void processChange(@Nonnull InstanceIdentifier<AclEntry> id,
                               @Nonnull AclEntry entry) throws WriteFailedException.CreateFailedException {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        String aclName = aclSetKey.getName();
        String aclTermName = getTermName(entry);
        Map<CommandKey, String> entries = Maps.newHashMap();
        // ipv4|ipv6
        if (entry.getIpv4() != null) {
            processIpv4(entry, entries);
        } else if (entry.getIpv6() != null) {
            processIpv6(entry, entries);
        } else {
            throw new IllegalStateException(f("No ip|ipv6 container found in acl entry %s", entry));
        }
        // transport
        processTransport(entry, entries);

        // actions
        processActions(entry, entries);

        for (Map.Entry<CommandKey, String> e : entries.entrySet()) {
            blockingWriteAndRead(fT(COMMANDS.get(e.getKey()), "family", AclUtil.getStringType(aclSetKey.getType()),
                    "aclName", aclName, "aclTermName", aclTermName, "value", e.getValue()), cli, id, entry);
        }
    }

    static String getTermName(@Nonnull AclEntry entry) {
        Optional<Config2> config2 = Optional.ofNullable(entry.getConfig().getAugmentation(Config2.class));
        return config2.isPresent() ? config2.get().getTermName() : entry.getSequenceId().toString();
    }

    @VisibleForTesting
    static void processIpv4(AclEntry entry, Map<CommandKey, String> commandVars) {
        if (entry.getIpv4().getConfig().getAugmentation(Config3.class) != null
                && entry.getIpv4().getConfig().getAugmentation(Config3.class).getHopRange() != null) {
            commandVars.put(CommandKey.ACL_TTL,
                    formatTTL(entry.getIpv4().getConfig().getAugmentation(Config3.class).getHopRange().getValue()));
        }
        if (entry.getAugmentation(AclEntry1.class) != null
                && entry.getAugmentation(AclEntry1.class).getIcmp() != null) {
            commandVars.put(CommandKey.ACL_ICMP_MSG_TYPE,
                    entry.getAugmentation(AclEntry1.class).getIcmp().getConfig().getMsgType().getUint8().toString());
        }
        // src address
        Optional<String> ipv4PrefixOptSrc = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getSourceAddress);
        // dst address
        Optional<String> ipv4PrefixOptDst = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getDestinationAddress);
        if (ipv4PrefixOptSrc.isPresent() && ipv4PrefixOptDst.isPresent()
                && ipv4PrefixOptSrc.get().equals(ipv4PrefixOptDst.get())) {
            commandVars.put(CommandKey.ACL_ADDR, ipv4PrefixOptDst.get());
        } else {
            ipv4PrefixOptSrc.ifPresent(s -> commandVars.put(CommandKey.ACL_SRC_ADDR, s));
            ipv4PrefixOptDst.ifPresent(s -> commandVars.put(CommandKey.ACL_DST_ADDR, s));
        }

        IpProtocolType ipProtocolType = Optional.ofNullable(entry.getIpv4().getConfig())
                .map(IpProtocolFieldsCommonConfig::getProtocol)
                .orElse(null);
        commandVars.put(CommandKey.ACL_PROTOCOL, formatProtocol(ipProtocolType, "ip"));
    }

    private static Optional<String> getIpv4Prefix(AclEntry entry,
                                                  Function<Ipv4ProtocolFieldsConfig, Ipv4Prefix> mapper) {
        return Optional.ofNullable(entry)
                .map(AclEntry::getIpv4)
                .map(Ipv4::getConfig)
                .map(mapper)
                .map(Ipv4Prefix::getValue);
    }

    @VisibleForTesting
    static void processIpv6(AclEntry entry, Map<CommandKey, String> commandVars) {
        if (entry.getIpv6().getConfig().getAugmentation(Config4.class) != null
                && entry.getIpv6().getConfig().getAugmentation(Config4.class).getHopRange() != null) {
            commandVars.put(CommandKey.ACL_TTL,
                    formatTTL(entry.getIpv6().getConfig().getAugmentation(Config4.class).getHopRange().getValue()));
        }
        if (entry.getAugmentation(AclEntry1.class) != null
                && entry.getAugmentation(AclEntry1.class).getIcmp() != null) {
            commandVars.put(CommandKey.ACL_ICMP_MSG_TYPE,
                    entry.getAugmentation(AclEntry1.class).getIcmp().getConfig().getMsgType().getUint8().toString());
        }
        // src address
        Optional<String> ipv6PrefixOptSrc = getIpv6Prefix(entry, Ipv6ProtocolFieldsConfig::getSourceAddress);
        // dst address
        Optional<String> ipv6PrefixOptDst = getIpv6Prefix(entry, Ipv6ProtocolFieldsConfig::getDestinationAddress);
        if (ipv6PrefixOptSrc.isPresent() && ipv6PrefixOptDst.isPresent()
                && ipv6PrefixOptSrc.get().equals(ipv6PrefixOptDst.get())) {
            commandVars.put(CommandKey.ACL_ADDR, ipv6PrefixOptDst.get());
        } else {
            ipv6PrefixOptSrc.ifPresent(s -> commandVars.put(CommandKey.ACL_SRC_ADDR, tryTranslateIpv6ToIpv4InIpv6(s)));
            ipv6PrefixOptDst.ifPresent(s -> commandVars.put(CommandKey.ACL_DST_ADDR, tryTranslateIpv6ToIpv4InIpv6(s)));
        }

        IpProtocolType ipProtocolType = Optional.ofNullable(entry.getIpv6().getConfig())
                .map(IpProtocolFieldsCommonConfig::getProtocol)
                .orElse(null);
        commandVars.put(CommandKey.ACL_PROTOCOL_IPV6, formatProtocol(ipProtocolType, "ipv6"));
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

    @VisibleForTesting
    static void processTransport(AclEntry entry, Map<CommandKey, String> commandVars) {
        if (entry.getTransport() != null && entry.getTransport().getConfig() != null) {
            AclSetAclEntryTransportPortNamedAug aug = entry.getTransport().getConfig()
                    .getAugmentation(AclSetAclEntryTransportPortNamedAug.class);

            if (aug != null) {
                if (aug.getSourcePortNamed() != null
                        && aug.getSourcePortNamed().equals(aug.getDestinationPortNamed())) {
                    commandVars.put(CommandKey.ACL_PORT, aug.getSourcePortNamed());
                } else {
                    if (aug.getSourcePortNamed() != null) {
                        commandVars.put(CommandKey.ACL_SRC_PORT, aug.getSourcePortNamed());
                    }
                    if (aug.getDestinationPortNamed() != null) {
                        commandVars.put(CommandKey.ACL_DST_PORT, aug.getDestinationPortNamed());
                    }
                }
            } else {
                Config config = entry.getTransport().getConfig();
                if (config.getSourcePort() != null && config.getSourcePort().equals(config.getDestinationPort())) {
                    setPort(commandVars, CommandKey.ACL_PORT, config.getSourcePort());
                } else {
                    if (config.getSourcePort() != null) {
                        setPort(commandVars, CommandKey.ACL_SRC_PORT, config.getSourcePort());
                    }
                    if (config.getDestinationPort() != null) {
                        setPort(commandVars, CommandKey.ACL_DST_PORT, config.getDestinationPort());
                    }
                }
            }
        }
    }

    private static void setPort(Map<CommandKey, String> commandVars, CommandKey key, PortNumRange port) {
        String formattedPort = formatPort(port);
        if (formattedPort != null) {
            commandVars.put(key, formattedPort);
        }
    }

    private static String formatPort(PortNumRange port) {
        if (port.getPortNumber() != null) {
            return port.getPortNumber().getValue().toString();
        } else if (port.getString() != null) {
            Matcher matcher = PORT_RANGE_PATTERN.matcher(port.getString());
            if (!matcher.find()) {
                LOG.warn("Wrong protocol range value: {}", port.getString());
            }
            return port.getString().replace("..", "-");
        } else if (PortNumRange.Enumeration.ANY.equals(port.getEnumeration())) {
            return null;
        }
        throw new IllegalArgumentException("Missing port");
    }

    @VisibleForTesting
    static void processActions(AclEntry entry, Map<CommandKey, String> commandVars) {
        if (entry.getActions() == null || entry.getActions().getConfig() == null) {
            throw new IllegalStateException(String.format("No actions found for entry %s", entry));
        }
        Class<? extends FORWARDINGACTION> action = entry.getActions().getConfig().getForwardingAction();
        if (action.equals(ACCEPT.class)) {
            commandVars.put(CommandKey.ACL_ACTION, "accept");
        } else if (action.equals(DROP.class)) {
            commandVars.put(CommandKey.ACL_ACTION, "discard");
        } else {
            throw new IllegalStateException(String.format("No action found for entry %s", entry));
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

        String ttlString = "";
        if (minRangeParam == maxRangeParam) {
            ttlString += minRangeParam;
        } else {
            ttlString += minRangeParam + "-" + maxRangeParam;
        }
        return ttlString;
    }

    @VisibleForTesting
    enum CommandKey {
        ACL_ACTION, ACL_PROTOCOL, ACL_PROTOCOL_IPV6, ACL_ADDR, ACL_PORT, ACL_SRC_ADDR,
        ACL_SRC_PORT, ACL_DST_ADDR, ACL_DST_PORT, ACL_ICMP_MSG_TYPE, ACL_TTL;
    }

    private static Map<CommandKey, String> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put(CommandKey.ACL_ACTION, ACL_ACTION_ENTRY);
        COMMANDS.put(CommandKey.ACL_PROTOCOL_IPV6, ACL_PROTOCOL_ENTRY_IPV6);
        COMMANDS.put(CommandKey.ACL_PROTOCOL, ACL_PROTOCOL_ENTRY);
        COMMANDS.put(CommandKey.ACL_ADDR, ACL_ADDR_ENTRY);
        COMMANDS.put(CommandKey.ACL_PORT, ACL_PORT_ENTRY);
        COMMANDS.put(CommandKey.ACL_SRC_ADDR, ACL_SRC_ADDR_ENTRY);
        COMMANDS.put(CommandKey.ACL_SRC_PORT, ACL_SRC_PORT_ENTRY);
        COMMANDS.put(CommandKey.ACL_DST_ADDR, ACL_DST_ADDR_ENTRY);
        COMMANDS.put(CommandKey.ACL_DST_PORT, ACL_DST_PORT_ENTRY);
        COMMANDS.put(CommandKey.ACL_ICMP_MSG_TYPE, ACL_ICMP_TYPE_ENTRY);
        COMMANDS.put(CommandKey.ACL_TTL, ACL_TTL_ENTRY);
    }
}
