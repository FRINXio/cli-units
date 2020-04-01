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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cubro.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.AclCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.COUNT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.EGRESSTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.ELAG;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.IPANY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.OPERATIONTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Ipv4AddressWildcarded;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IPPROTOCOL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.packet.match.types.rev170526.IpProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryWriter implements CliListWriter<AclEntry, AclEntryKey> {

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

    private static final String IPV4_HOST_ANY = "0.0.0.0/255.255.255.255";

    private static final String ACL_ANY_ENTRY = "configure\n"
            + "access-list ipv4 {$aclName}\n"
            + "{$aclSeqId} {$aclFwdAction} {$aclEgressType} {$aclEgressValue} {$aclProtocol}"
            + " {$aclSrcAddr} {$aclDstAddr} {$aclOperation}\n"
            + "sync access-list\n"
            + "end\n";

    private static final String ACL_DELETE = "configure\n"
            + "access-list ipv4 {$aclName}\n"
            + "no {$aclSeqId}\n"
            + "sync access-list\n"
            + "end\n";

    private final Cli cli;

    public AclEntryWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                       @Nonnull AclEntry aclEntry,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        processChange(id, aclEntry);
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

        Preconditions.checkArgument(aclSetKey.getType().equals(ACLIPV4.class),
                "Unsupported ACL type: " + aclSetKey.getType());
        final String aclName = aclSetKey.getName();
        final String aclSequenceId = dataBefore.getSequenceId().toString();

        blockingWriteAndRead(fT(ACL_DELETE, "aclName", aclName, "aclSeqId", aclSequenceId), cli, id, dataBefore);
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
        } else {
            throw new IllegalStateException(f("No ipv4 container found in acl entry %s", entry));
        }

        // actions
        processActions(entry, commandVars);

        // egress type
        processEgressType(entry, commandVars);

        // operation
        processOperation(entry, commandVars);

        switch (commandVars.aclProtocol) {
            case "any":
                blockingWriteAndRead(fT(ACL_ANY_ENTRY,
                        "aclName", commandVars.aclName,
                        "aclSeqId", commandVars.aclSeqId,
                        "aclFwdAction", commandVars.aclFwdAction,
                        "aclEgressType", commandVars.aclEgressType,
                        "aclEgressValue", commandVars.aclEgressValue,
                        "aclProtocol", commandVars.aclProtocol,
                        "aclSrcAddr", commandVars.aclSrcAddr,
                        "aclDstAddr", commandVars.aclDstAddr,
                        "aclOperation", commandVars.aclOperation),
                        cli, id, entry);
                break;
            default: break;
        }
    }

    private void processEgressType(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getActions() == null || entry.getActions().getConfig() == null) {
            throw new IllegalStateException(f("No actions found for entry %s", entry));
        }
        AclCubroAug augmentation = entry.getActions().getConfig().getAugmentation(AclCubroAug.class);
        Class<? extends EGRESSTYPE> egressType = augmentation.getEgressType();
        if (egressType.equals(ELAG.class)) {
            commandVars.aclEgressType = "elag";
            commandVars.aclEgressValue = augmentation.getEgressValue().toString();
        } else {
            throw new IllegalStateException(f("No operation found for entry %s", entry));
        }
    }

    private void processOperation(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getActions() == null || entry.getActions().getConfig() == null) {
            throw new IllegalStateException(f("No actions found for entry %s", entry));
        }
        Class<? extends OPERATIONTYPE> operation = entry.getActions().getConfig()
                .getAugmentation(AclCubroAug.class).getOperation();
        if (operation.equals(COUNT.class)) {
            commandVars.aclOperation = "count";
        } else {
            throw new IllegalStateException(f("No operation found for entry %s", entry));
        }
    }

    private void processIpv4(AclEntry entry, MaxMetricCommandDTO commandVars) {
        // src address
        Optional<String> ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getSourceAddress);
        Optional<Ipv4AddressWildcarded> ipv4WildcardedOpt = getIpv4Wildcarded(entry,
                AclSetAclEntryIpv4WildcardedAug::getSourceAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv4WildcardedOpt.isPresent(),
                    SOURCE_ADDRESS_AND_SOURCE_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclSrcAddr = info.getAddress() + "/" + AclUtil.getWildcardfromSubnet(info.getNetmask());
        } else {
            Preconditions.checkArgument(ipv4WildcardedOpt.isPresent(),
                    NONE_SOURCE_ADDRESS_OR_SOURCE_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    SOURCE_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclSrcAddr = ipv4WildcardedOpt.get().getAddress().getValue() + "/" + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }

        if (commandVars.aclSrcAddr.equals(IPV4_HOST_ANY)) {
            commandVars.aclSrcAddr = "any";
        }
        // dst address
        ipv4PrefixOpt = getIpv4Prefix(entry, Ipv4ProtocolFieldsConfig::getDestinationAddress);
        ipv4WildcardedOpt = getIpv4Wildcarded(entry, AclSetAclEntryIpv4WildcardedAug::getDestinationAddressWildcarded);
        if (ipv4PrefixOpt.isPresent()) {
            Preconditions.checkArgument(!ipv4WildcardedOpt.isPresent(),
                    DESTINATION_ADDRESS_AND_DESTINATION_ADDRESS_WILDCARDED_TOGETHER_ERROR);
            SubnetUtils.SubnetInfo info = new SubnetUtils(ipv4PrefixOpt.get()).getInfo();
            commandVars.aclDstAddr = info.getAddress() + "/" + AclUtil.getWildcardfromSubnet(info.getNetmask());
        } else {
            Preconditions.checkArgument(ipv4WildcardedOpt.isPresent(),
                    NONE_DESTINATION_ADDRESS_OR_DESTINATION_ADDRESS_WILDCARDED_ERROR);
            Preconditions.checkArgument(ipv4WildcardedOpt.get().getAddress() != null && ipv4WildcardedOpt.get()
                            .getWildcardMask() != null,
                    DESTINATION_ADDRESS_WILDCARDED_MISSING_FIELDS_ERROR);
            commandVars.aclDstAddr = ipv4WildcardedOpt.get().getAddress().getValue() + "/" + ipv4WildcardedOpt.get()
                    .getWildcardMask().getValue();
        }

        if (commandVars.aclDstAddr.equals(IPV4_HOST_ANY)) {
            commandVars.aclDstAddr = "any";
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

    private static String formatProtocol(@Nullable IpProtocolType ipProtocolType, String type) {
        if (ipProtocolType == null) {
            return type;
        }

        Class<? extends IPPROTOCOL> protocol = ipProtocolType.getIdentityref();
        if (protocol == null) {
            return String.valueOf(ipProtocolType.getUint8());
        }

        if (protocol.equals(IPANY.class)) {
            return "any";
        }
        LOG.warn("Unknown protocol {}", protocol);
        throw new IllegalArgumentException("ACL contains unsupported protocol: " + protocol.getSimpleName());
    }

    private void processActions(AclEntry entry, MaxMetricCommandDTO commandVars) {
        if (entry.getActions() == null || entry.getActions().getConfig() == null) {
            throw new IllegalStateException(f("No actions found for entry %s", entry));
        }
        Class<? extends FORWARDINGACTION> action = entry.getActions().getConfig().getForwardingAction();
        if (action.equals(ACCEPT.class)) {
            commandVars.aclFwdAction = "forward";
        } else if (action.equals(DROP.class)) {
            commandVars.aclFwdAction = "deny";
        } else {
            throw new IllegalStateException(f("No action found for entry %s", entry));
        }
    }

    @VisibleForTesting
    static class MaxMetricCommandDTO {
        String aclName = "";
        String aclSeqId = "";
        String aclFwdAction = "";
        String aclProtocol = "";
        String aclSrcAddr = "";
        String aclDstAddr = "";
        String aclEgressType = "";
        String aclEgressValue = "";
        String aclOperation = "";
    }
}