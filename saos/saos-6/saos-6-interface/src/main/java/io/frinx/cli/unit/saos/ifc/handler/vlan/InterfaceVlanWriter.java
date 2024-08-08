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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.Vlans;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanWriter implements CliWriter<Config> {

    private static String WRITE_TEMPLATE =
            """
                    {% if ($vlanAdd) %}vlan add vlan {$vlanAdd} port {$portName}
                    {% endif %}{% if ($vlanRemove) %}vlan remove vlan {$vlanRemove} port {$portName}
                    {% endif %}""";

    private Cli cli;

    public InterfaceVlanWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, updateTemplate(config, null, ifcName, writeContext));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataAfter, dataBefore, ifcName, writeContext));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, updateTemplate(null, config, ifcName, writeContext));
    }

    private String updateTemplate(Config after, Config before, String port, WriteContext writeContext) {
        if (after != null) {
            return fT(WRITE_TEMPLATE, "portName", port,
                    "vlanRemove", getVlansDiff(before, after, writeContext),
                    "vlanAdd", getVlansDiff(after, before, writeContext));
        }
        return fT(WRITE_TEMPLATE, "portName", port,
                "vlanRemove", getVlansDiff(before, after, writeContext),
                "vlanAdd", getVlansDiff(after, before, writeContext));
    }

    private Object getVlansDiff(Config before, Config after, WriteContext writeContext) {
        if (before != null && before.getTrunkVlans() != null) {
            if (after != null && after.getTrunkVlans() != null) {
                List<VlanSwitchedConfig.TrunkVlans> vlanIds = before.getTrunkVlans().stream()
                        .filter(vlan_id -> !after.getTrunkVlans().contains(vlan_id))
                        .filter(vlan_id -> !compareVlans(writeContext).contains(vlan_id.getVlanId().getValue()))
                        .collect(Collectors.toList());
                if (vlanIds.isEmpty()) {
                    return null;
                }
                return getVlansString(vlanIds);
            }
            return getVlansString(before.getTrunkVlans());
        }
        return null;
    }

    private List<Integer> compareVlans(WriteContext writeContext) {
        var keyId = InstanceIdentifier.create(NetworkInstances.class).child(NetworkInstance.class,
                new NetworkInstanceKey("default")).child(Vlans.class);
        var afterIds = writeContext.readAfter(keyId)
                .map(Vlans::getVlan)
                .map(v -> v.stream()
                        .map(val -> val.getVlanId().getValue())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return writeContext.readBefore(keyId)
                .map(Vlans::getVlan)
                .map(v -> v.stream()
                        .map(val -> val.getVlanId().getValue())
                        .filter(f -> !afterIds.contains(f))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
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