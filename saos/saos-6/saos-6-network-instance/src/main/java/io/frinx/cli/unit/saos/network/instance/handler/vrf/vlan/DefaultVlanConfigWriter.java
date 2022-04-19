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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultVlanConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "vlan create vlan {$data.vlan_id.value}"
            + "{% if ($data.name) %} name {$data.name}{% endif %}\n"
            + "{% if ($tpid) %}vlan set vlan {$data.vlan_id.value} egress-tpid {$tpid}\n{% endif %}";

    private static final String UPDATE_TEMPLATE =
            "{$data|update(name,vlan rename vlan `$data.vlan_id.value` name `$data.name`\n,)}"
            + "{% if ($tpid) %}vlan set vlan {$data.vlan_id.value} egress-tpid {$tpid}\n{% endif %}";

    private final Cli cli;

    public DefaultVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!getCheck().canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }
        blockingWriteAndRead(cli, instanceIdentifier, data, writeTemplate(data));
        return true;
    }

    @VisibleForTesting
    String writeTemplate(Config data) {
        Config1 dataAug = data.getAugmentation(Config1.class);
        return fT(WRITE_TEMPLATE, "data", data,
                "tpid", dataAug != null ? dataAug.getEgressTpid().getSimpleName().substring(6,10) : null);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!getCheck().canProcess(id, writeContext, false)) {
            return false;
        }

        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
        return true;
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter) {
        String tpidBefore = getTpid(dataBefore);
        String tpidAfter = getTpid(dataAfter);
        return fT(UPDATE_TEMPLATE, "data", dataAfter, "before", dataBefore,
                "tpid", !tpidAfter.equals(tpidBefore) ? tpidAfter.substring(6, 10) : null);
    }

    private String getTpid(Config data) {
        Preconditions.checkNotNull(data.getAugmentation(Config1.class),
                "Missing egress-tpid in %s", data.getVlanId().getValue());
        return Optional.ofNullable(data.getAugmentation(Config1.class)).orElse(null).getEgressTpid().getSimpleName();
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                                  @Nonnull Config config,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!getCheck().canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }

        // Before removing vlan directly, we need to remove it from the interfaces
        final List<Interface> beforeInterfaces = writeContext.readBefore(IIDs.INTERFACES)
                .or(new InterfacesBuilder().build())
                .getInterface();

        Integer vlanIdForDelete = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId().getValue();
        StringBuilder deleteCmd = new StringBuilder();

        for (Interface anInterface : beforeInterfaces) {
            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top
                    .switched.vlan.Config configBefore = writeContext.readBefore(IidUtils.createIid(
                    io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                    anInterface.getKey())).orNull();

            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top
                    .switched.vlan.Config configAfter = writeContext.readAfter(IidUtils.createIid(
                    io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_ET_AUG_ETHERNET1_SW_CONFIG,
                    anInterface.getKey())).orNull();

            if (configBefore == null || configAfter == null) {
                continue;
            }
            List<TrunkVlans> trunkVlansAfter = configAfter.getTrunkVlans();
            // if after update trunk vlan command the vlan is not removed from all interfaces
            if (trunkVlansAfter != null && trunkVlansAfter.contains(new TrunkVlans(new VlanId(vlanIdForDelete)))) {
                throw new WriteFailedException.DeleteFailedException(instanceIdentifier,
                        new IllegalStateException("Vlan " + vlanIdForDelete + " must be deleted from interface "
                                + anInterface.getName() + " before its removed"));
            }
            final List<VlanSwitchedConfig.TrunkVlans> trunkVlansBefore = configBefore.getTrunkVlans();
            deleteCmd.append(addDeleteCommand(anInterface, vlanIdForDelete, trunkVlansBefore));
        }

        blockingDeleteAndRead(
                deleteCmd.append(f("vlan delete vlan %d", vlanIdForDelete)).toString(),
                cli, instanceIdentifier);
        return true;
    }

    @VisibleForTesting
    protected String addDeleteCommand(Interface anInterface, int vlanIdForDelete,
                                      List<VlanSwitchedConfig.TrunkVlans> trunkVlans) {
        if (trunkVlans == null) {
            return "";
        }
        for (VlanSwitchedConfig.TrunkVlans trunkVlan : trunkVlans) {
            if (trunkVlan.toString().contains("..") && trunkVlan.getVlanRange() != null) {
                String[] indexes = trunkVlan.getVlanRange().getValue().split("\\.\\.");
                if (Integer.parseInt(indexes[0]) <= vlanIdForDelete
                        && vlanIdForDelete <= Integer.parseInt(indexes[1])) {
                    return (f("vlan remove vlan %d port %s\n", vlanIdForDelete, anInterface.getName()));
                }
            } else if (trunkVlan.getVlanId() != null
                    && trunkVlan.getVlanId().getValue().equals(vlanIdForDelete)) {
                return (f("vlan remove vlan %d port %s\n", vlanIdForDelete, anInterface.getName()));
            }
        }
        return "";
    }

    public Check getCheck() {
        return BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE);
    }
}
