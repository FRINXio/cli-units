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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryTransportPortNamedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.IpProtocolFieldsCommonConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.Ipv4ProtocolFieldsConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPICMP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPTCP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPUDP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.PortNumRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



public class AclEntryWriter implements CliListWriter<AclEntry, AclEntryKey> {

    public static final String SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR = "source-address-wildcarded must "
            + "contain address and wildcard-mask";
    public static final String DESTINATION_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR = "destination-address-wildcarded "
            + "must contain address and wildcard-mask";

    private static final String ACL_IP_ENTRY = "system-view\n"
            + "acl name {$aclName}\n"
            + "rule {$aclSeqId} {$aclFwdAction} {$aclProtocol}{$aclSrcAddr}{$aclDstAddr}{$aclSrcPort}{$aclDstPort}\n"
            + "return\n";

    private static final String ACL_EXTENDED_DELETE = "system-view\n"
            + "acl name {$aclName}\n"
            + "undo rule {$aclSeqId}\n"
            + "return\n";

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
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                        @Nonnull AclEntry dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        final String aclSequenceId = dataBefore.getSequenceId().toString();

        blockingWriteAndRead(fT(ACL_EXTENDED_DELETE, "aclName", aclName, "aclSeqId", aclSequenceId),
                cli, id, dataBefore);
    }

    private void processChange(@Nonnull InstanceIdentifier<AclEntry> id,
                               @Nonnull AclEntry entry) throws WriteFailedException.CreateFailedException {
        MaxMetricCommandDTO commandVars = new MaxMetricCommandDTO();
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        commandVars.aclName = aclSetKey.getName();
        commandVars.aclSeqId = entry.getSequenceId().toString();

        processIpv4(entry, commandVars);

        // transport
        processTransport(entry, commandVars);

        // actions
        processActions(entry, commandVars);

        blockingWriteAndRead(fT(ACL_IP_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclSrcPort", commandVars.aclSrcPort,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclDstPort", commandVars.aclDstPort),
                cli, id, entry);
    }

    private void processIpv4(AclEntry entry, MaxMetricCommandDTO commandVars) {
        // src address
        Optional<String> ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getSourceAddress);
        Optional<Ipv4AddressWildcarded> ipv4WildcardedOpt = getIpv4Wildcarded(entry,
                AclSetAclEntryIpv4WildcardedAug::getSourceAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclSrcAddr =
                    " source " + info.getAddress() + " " + AclUtil.getWildcardfromSubnet(info.getNetmask());
        } else if (ipv4WildcardedOpt.isPresent()) {
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclSrcAddr =
                    " source " + ipv4WildcardedOpt.get().getAddress().getValue() + " " + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }

        // dst address
        ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getDestinationAddress);
        ipv4WildcardedOpt = getIpv4Wildcarded(entry, AclSetAclEntryIpv4WildcardedAug::getDestinationAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclDstAddr =
                    " destination " + info.getAddress() + " " + AclUtil.getWildcardfromSubnet(info.getNetmask());
        } else if (ipv4WildcardedOpt.isPresent()) {
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    DESTINATION_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclDstAddr =
                    " destination " + ipv4WildcardedOpt.get().getAddress().getValue() + " " + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }

        IpProtocolType ipProtocolType = Optional.ofNullable(entry.getIpv4().getConfig())
                .map(IpProtocolFieldsCommonConfig::getProtocol)
                .orElse(null);
        commandVars.aclProtocol = formatProtocol(ipProtocolType, "ip");
    }

    private void processTransport(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getTransport() != null && entry.getTransport().getConfig() != null) {
            AclSetAclEntryTransportPortNamedAug aug = entry.getTransport().getConfig()
                    .getAugmentation(AclSetAclEntryTransportPortNamedAug.class);

            if (entry.getTransport().getConfig().getSourcePort() != null) {
                commandVars.aclSrcPort = " source-port " + formatPort(entry.getTransport().getConfig().getSourcePort());
            } else if (aug != null && aug.getSourcePortNamed() != null) {
                commandVars.aclSrcPort = " source-port " + formatNamedPort(aug.getSourcePortNamed());
            }

            if (entry.getTransport().getConfig().getDestinationPort() != null) {
                commandVars.aclDstPort =
                        " destination-port " + formatPort(entry.getTransport().getConfig().getDestinationPort());
            } else if (aug != null && aug.getDestinationPortNamed() != null) {
                commandVars.aclDstPort = " destination-port " + formatNamedPort(aug.getDestinationPortNamed());
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

    private String formatPort(PortNumRange port) {
        if (port.getPortNumber() != null) {
            return f("eq %s", port.getPortNumber().getValue());
        }
        return "";
    }

    private String formatNamedPort(String port) {
        return f("eq %s", port);
    }

    private Optional<String> getIpv4Prefix(AclEntry entry, Function<Ipv4ProtocolFieldsConfig, Ipv4Prefix> mapper) {
        return Optional.ofNullable(entry)
                .map(AclEntry::getIpv4)
                .map(Ipv4::getConfig)
                .map(mapper)
                .map(Ipv4Prefix::getValue);
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
    static class MaxMetricCommandDTO {
        String aclName = "";
        String aclSeqId = "";
        String aclFwdAction = "";
        String aclProtocol = "";
        String aclSrcAddr = "";
        String aclSrcPort = "";
        String aclDstAddr = "";
        String aclDstPort = "";
    }
}
