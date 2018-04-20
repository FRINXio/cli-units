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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclEntry1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryWriter implements CliListWriter<AclEntry, AclEntryKey>{

    private static final int MAX_TTL = 255;
    private static final int MAX_PORT = 65535;
    private static final String RANGE_SEPARATOR = "..";
    private static final Function<String, String> WRONG_RANGE_FORMAT_MSG = rangeString -> String.format(
        "incorrect range format, range parameter should contains two numbers separated by '%s', entered: %s",
        RANGE_SEPARATOR, rangeString);
    private static final String ANY = "any";

    private final String ACL_IP_ENTRY = "{$type} access-list {$acl_name}\n" +
            "{$acl_seq_id} {$acl_fwd_action} {$acl_protocol} {$acl_src_addr} {$acl_dst_addr} {$acl_ttl}\n" +
            "exit\n";
    private final String ACL_TCP_ENTRY = "{$type} access-list {$acl_name}\n" +
            "{$acl_seq_id} {$acl_fwd_action} {$acl_protocol} {$acl_src_addr} {$acl_src_port} {$acl_dst_addr} {$acl_dst_port} {$acl_ttl}\n" +
            "exit\n";
    private final String ACL_ICMP_ENTRY = "{$type} access-list {$acl_name}\n" +
            "{$acl_seq_id} {$acl_fwd_action} {$acl_protocol} {$acl_src_addr} {$acl_dst_addr} {$acl_icmp_msg_type} {$acl_ttl}\n" +
            "exit\n";
    private final String ACL_DELETE = "{$type} access-list {$acl_name}\n" +
            "no {$acl_seq_id}\n" +
            "exit\n";
    private final Pattern PORT_RANGE_PATTERN = Pattern.compile("(?<from>\\d*)..(?<to>\\d*)");

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
        final String aclType = aclSetKey.getType().equals(ACLIPV4.class) ? "ipv4" : "ipv6";
        final String aclName = aclSetKey.getName();
        final String aclSequenceId = dataBefore.getSequenceId().toString();

        blockingWriteAndRead(
            fT(ACL_DELETE,
                "type", aclType,
                "acl_name", aclName,
                "acl_seq_id", aclSequenceId),
            cli, id, dataBefore);
    }

    private void processChange(@Nonnull InstanceIdentifier<AclEntry> id,
                               @Nonnull AclEntry entry) throws WriteFailedException.CreateFailedException {
        MaxMetricCommandDTO commandVars = new MaxMetricCommandDTO();
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        commandVars.acl_name = aclSetKey.getName();
        commandVars.type = aclSetKey.getType().equals(ACLIPV4.class) ? "ipv4" : "ipv6";
        commandVars.acl_seq_id = entry.getSequenceId().toString();
        // ipv4|ipv6
        if (entry.getIpv4() != null) {
            processIpv4(entry, commandVars);
        } else if (entry.getIpv6() != null) {
            processIpv6(entry, commandVars);
        } else {
            throw new IllegalStateException(f("No ipv4|ipv6 container found in acl entry %s", entry));
        }
        // transport
        processTransport(entry, commandVars);

        // actions
        processActions(entry, commandVars);

        switch (commandVars.acl_protocol) {
            case "ipv4":
            case "ipv6":
                blockingWriteAndRead(fT(ACL_IP_ENTRY,
                        "type", commandVars.type,
                        "acl_name", commandVars.acl_name,
                        "acl_seq_id", commandVars.acl_seq_id,
                        "acl_fwd_action", commandVars.acl_fwd_action,
                        "acl_protocol", commandVars.acl_protocol,
                        "acl_src_addr", commandVars.acl_src_addr,
                        "acl_dst_addr", commandVars.acl_dst_addr,
                        "acl_ttl", commandVars.acl_ttl),
                        cli, id, entry);
                break;
            case "udp":
            case "tcp":
                blockingWriteAndRead(fT(ACL_TCP_ENTRY,
                        "type", commandVars.type,
                        "acl_name", commandVars.acl_name,
                        "acl_seq_id", commandVars.acl_seq_id,
                        "acl_fwd_action", commandVars.acl_fwd_action,
                        "acl_protocol", commandVars.acl_protocol,
                        "acl_src_addr", commandVars.acl_src_addr,
                        "acl_src_port", commandVars.acl_src_port,
                        "acl_dst_addr", commandVars.acl_dst_addr,
                        "acl_dst_port", commandVars.acl_dst_port,
                        "acl_ttl", commandVars.acl_ttl),
                        cli, id, entry);
                break;
            case "icmp":
            case "icmpv6":
                blockingWriteAndRead(fT(ACL_ICMP_ENTRY,
                        "type", commandVars.type,
                        "acl_name", commandVars.acl_name,
                        "acl_seq_id", commandVars.acl_seq_id,
                        "acl_fwd_action", commandVars.acl_fwd_action,
                        "acl_protocol", commandVars.acl_protocol,
                        "acl_src_addr", commandVars.acl_src_addr,
                        "acl_dst_addr", commandVars.acl_dst_addr,
                        "acl_icmp_msg_type", commandVars.acl_icmp_msg_type,
                        "acl_ttl", commandVars.acl_ttl),
                        cli, id, entry);
                break;
        }
    }

    private void processIpv4(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getIpv4().getConfig().getAugmentation(Config1.class) != null &&
                entry.getIpv4().getConfig().getAugmentation(Config1.class).getHopRange() != null) {
            commandVars.acl_ttl =
                formatTTL(entry.getIpv4().getConfig().getAugmentation(Config1.class).getHopRange().getValue());
        }
        if (entry.getAugmentation(AclEntry1.class) != null &&
            entry.getAugmentation(AclEntry1.class).getIcmp() != null) {
            commandVars.acl_icmp_msg_type =
                entry.getAugmentation(AclEntry1.class).getIcmp().getConfig().getMsgType().getUint8().toString();
        }
        commandVars.acl_src_addr = Preconditions.checkNotNull(entry.getIpv4().getConfig().getSourceAddress()).getValue();
        commandVars.acl_dst_addr = Preconditions.checkNotNull(entry.getIpv4().getConfig().getDestinationAddress()).getValue();
        commandVars.acl_protocol = Preconditions.checkNotNull(
                formatProtocol(entry.getIpv4().getConfig().getProtocol().getIdentityref(), commandVars.type));
    }

    private void processIpv6(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getIpv6().getConfig().getAugmentation(Config2.class) != null &&
                entry.getIpv6().getConfig().getAugmentation(Config2.class).getHopRange() != null) {
            commandVars.acl_ttl =
                formatTTL(entry.getIpv6().getConfig().getAugmentation(Config2.class).getHopRange().getValue());
        }
        if (entry.getAugmentation(AclEntry1.class) != null &&
            entry.getAugmentation(AclEntry1.class).getIcmp() != null) {
            commandVars.acl_icmp_msg_type =
                entry.getAugmentation(AclEntry1.class).getIcmp().getConfig().getMsgType().getUint8().toString();
        }
        commandVars.acl_src_addr = Preconditions.checkNotNull(entry.getIpv6().getConfig().getSourceAddress()).getValue();
        commandVars.acl_dst_addr = Preconditions.checkNotNull(entry.getIpv6().getConfig().getDestinationAddress()).getValue();
        commandVars.acl_protocol = Preconditions.checkNotNull(
                formatProtocol(entry.getIpv6().getConfig().getProtocol().getIdentityref(), commandVars.type));
    }

    private void processTransport(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getTransport() != null && entry.getTransport().getConfig() != null) {
            if (entry.getTransport().getConfig().getSourcePort() != null) {
                commandVars.acl_src_port = formatPort(entry.getTransport().getConfig().getSourcePort());
            }
            if (entry.getTransport().getConfig().getDestinationPort() != null) {
                commandVars.acl_dst_port = formatPort(entry.getTransport().getConfig().getDestinationPort());
            }
        }
    }

    private void processActions(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getActions() == null || entry.getActions().getConfig() == null) {
            throw new IllegalStateException(f("No actions found for entry %s", entry));
        }
        Class<? extends FORWARDINGACTION> action = entry.getActions().getConfig().getForwardingAction();
        if (action.equals(ACCEPT.class)) {
            commandVars.acl_fwd_action = "permit";
        } else if (action.equals(DROP.class)) {
            commandVars.acl_fwd_action = "deny";
        } else {
            throw new IllegalStateException(f("No action found for entry %s", entry));
        }
    }

    @Nullable
    private static String formatProtocol(Class<? extends IPPROTOCOL> protocol, String type) {
        if (protocol.equals(IPPROTOCOL.class)) {
            return type;
        } else if (protocol.equals(IPUDP.class)) {
            return "udp";
        } else if (protocol.equals(IPTCP.class)) {
            return "tcp";
        } else if (protocol.equals(IPICMP.class)) {
            switch (type) {
                case "ipv4":
                    return "icmp";
                case "ipv6":
                    return "icmpv6";
                default:
                    LOG.warn("Unknown protocol {}", protocol);
                    return null;
            }
        } else {
            LOG.warn("Unknown protocol {}", protocol);
            return null;
        }
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
                return f("lt %s", to+1);
            }
            if (to == MAX_PORT) {
                return f("gt %s", from-1);
            }
            if (from == to) {
                return f("eq %s", from);
            }
            if (from > to && from - 1 == to + 1) {
                return f("neq %s", from);
            }
            if (from < to) {
                return f("range %s %s", from, to);
            }
            LOG.warn("Wrong protocol range value: {}", port.getString());
            return "";
        }

        return "";
    }

    private String formatTTL(final String rangeString) {
        Preconditions.checkArgument(rangeString.contains(RANGE_SEPARATOR), "incorrect range format %s", rangeString);
        final String[] rangeParams = rangeString.split("\\.\\.");
        Preconditions.checkArgument(rangeParams.length == 2, WRONG_RANGE_FORMAT_MSG.apply(rangeString));
        final int minRangeParam;
        final int maxRangeParam;
        try {
            minRangeParam = Integer.valueOf(rangeParams[0]);
            maxRangeParam = Integer.valueOf(rangeParams[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_RANGE_FORMAT_MSG.apply(rangeString));
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
                "incorrect range param for 'neq', first range param has to be greater then second by 2, entered: %s",
                rangeString);
            ttlString = ttlString + " neq " + (minRangeParam - 1);
        }

        return ttlString;
    }

    private class MaxMetricCommandDTO {

        private String type = "", acl_name = "", acl_seq_id = "", acl_fwd_action = "", acl_protocol = "", acl_src_addr = "",
                acl_src_port = "", acl_dst_addr = "", acl_dst_port = "", acl_icmp_msg_type = "", acl_ttl = "";
    }
}
