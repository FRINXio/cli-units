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

package io.frinx.cli.unit.saos.ifc.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanWriter implements CliWriter<Config> {

    private static String WRITE_TEMPLATE =
            "{% if ($vlanRemove) %}vlan remove vlan {$vlanRemove} port {$portName}\n{% endif %}"
            +   "{% if ($vlanAdd) %}vlan add vlan {$vlanAdd} port {$portName}\n{% endif %}";

    private Cli cli;

    public InterfaceVlanWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, updateTemplate(config, null, ifcName));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataAfter, dataBefore, ifcName));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, updateTemplate(null, config, ifcName));
    }

    private String updateTemplate(Config after, Config before, String port) {
        if (after != null) {
            return fT(WRITE_TEMPLATE, "portName", port,
                    "vlanRemove", getVlansDiff(before, after),
                    "vlanAdd", getVlansDiff(after, before));
        }
        return fT(WRITE_TEMPLATE, "portName", port,
                "vlanRemove", getVlansDiff(before, after),
                "vlanAdd", getVlansDiff(after, before));
    }

    private Object getVlansDiff(Config first, Config second) {
        if (first != null && first.getTrunkVlans() != null) {
            if (second != null && second.getTrunkVlans() != null) {
                List<VlanSwitchedConfig.TrunkVlans> vlanIds = first.getTrunkVlans().stream()
                        .filter(vlan_id -> !second.getTrunkVlans().contains(vlan_id))
                        .collect(Collectors.toList());
                if (vlanIds.isEmpty()) {
                    return null;
                }
                return getVlansString(vlanIds);
            }
            return getVlansString(first.getTrunkVlans());
        }
        return null;
    }

    private String getVlansString(List<VlanSwitchedConfig.TrunkVlans> vlanIds) {
        StringBuilder str = new StringBuilder();

        for (VlanSwitchedConfig.TrunkVlans trunkVlan : vlanIds) {
            str.append(trunkVlan.getVlanId().getValue()).append(",");
        }
        str.deleteCharAt(str.length() - 1);

        return str.toString();
    }
}
