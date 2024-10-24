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

package io.frinx.cli.unit.brocade.network.instance.vrf.vlan;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def.Vlan;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanRoutedTop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DefaultVlanConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    @VisibleForTesting
    static final String WRITE_TEMPLATE = """
            configure terminal
            vlan {$data.vlan_id.value}{% if ($data.name) %} name {$data.name}{% endif %}
            end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            no vlan {$data.vlan_id.value}
            end""";

    private final Cli cli;

    public DefaultVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                 @NotNull Config config,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!getCheck().canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }

        VlanConfig.Status vlanStatus = config.getStatus();
        Preconditions.checkArgument(vlanStatus == null || vlanStatus.equals(VlanConfig.Status.ACTIVE),
                "Suspended VLANs are not available");
        blockingWriteAndRead(fT(WRITE_TEMPLATE, "data", config), cli, instanceIdentifier, config);

        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!Objects.equal(dataBefore.getName(), dataAfter.getName())) {
            // Only "name" parameter can be changed for a VLAN, which is safe to update via write template
            return writeCurrentAttributesWResult(id, dataAfter, writeContext);
        }
        return false;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                  @NotNull Config config,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!getCheck().canProcess(instanceIdentifier, writeContext, true)) {
            return false;
        }

        Optional<Interfaces> allInterfaces = writeContext.readAfter(io.frinx.openconfig.openconfig.interfaces
                .IIDs.INTERFACES);
        Preconditions.checkArgument(!(allInterfaces.isPresent()
                && isAnyInterfaceInVlanId(allInterfaces.get(), config.getVlanId())),
                "Cannot delete vlan %s, it contains another interfaces", config.getVlanId().getValue());

        blockingDeleteAndRead(fT(DELETE_TEMPLATE, "data", config), cli, instanceIdentifier);
        return true;
    }

    private boolean isAnyInterfaceInVlanId(Interfaces interfaces, VlanId vlanId) {
        boolean isSwitchedIfcInVlan = interfaces.getInterface().stream()
                .map(i -> i.getAugmentation(Interface1.class))
                .filter(java.util.Objects::nonNull)
                .map(i1 -> i1.getEthernet().getAugmentation(Ethernet1.class))
                .filter(java.util.Objects::nonNull)
                .map(e -> e.getSwitchedVlan())
                .filter(java.util.Objects::nonNull)
                .map(e -> e.getConfig())
                .anyMatch(c -> containsVlan(c, vlanId));

        boolean isRoutedIfcInVlan = interfaces.getInterface().stream()
                .map(i -> i.getAugmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714
                        .Interface1.class))
                .filter(java.util.Objects::nonNull)
                .map(VlanRoutedTop::getRoutedVlan)
                .filter(java.util.Objects::nonNull)
                .anyMatch(c -> c.getConfig().getVlan().getUint16().equals(vlanId.getValue()));

        return isSwitchedIfcInVlan || isRoutedIfcInVlan;
    }

    private boolean containsVlan(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan
                                         .switched.top.switched.vlan.Config config, VlanId vlanId) {
        return (config.getNativeVlan() != null && config.getNativeVlan().equals(vlanId))
                || (config.getAccessVlan() != null && config.getAccessVlan().equals(vlanId))
                || (config.getTrunkVlans() != null && isVlanIdInTrunkVlans(config.getTrunkVlans(), vlanId));
    }

    private boolean isVlanIdInTrunkVlans(List<VlanSwitchedConfig.TrunkVlans> trunkVlans, VlanId vlanId) {
        return Vlan.parseVlanRanges(trunkVlans).stream()
                .map(VlanSwitchedConfig.TrunkVlans::getVlanId)
                .anyMatch(tv -> tv.equals(vlanId));
    }

    public Check getCheck() {
        return BasicCheck.checkData(
                ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE);
    }
}