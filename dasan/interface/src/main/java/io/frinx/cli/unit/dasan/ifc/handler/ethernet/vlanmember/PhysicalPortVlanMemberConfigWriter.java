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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.PhysicalPortInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortVlanMemberConfigWriter implements CliWriter<Config> {

    private final Cli cli;
    private static final String DELETE_CMD_FORMAT =  "vlan del %s %s";
    private static final String ADD_TRUNK_CMD_FORMAT =  "vlan add %s %s tagged";
    private static final String ADD_NATIVE_CMD_FORMAT =  "vlan add %s %s untagged";

    public PhysicalPortVlanMemberConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);
        if (!matcher.matches()) {
            return;
        }

        final String portId = matcher.group("portid");
        VlanId vlBefore = dataBefore.getNativeVlan();
        VlanId vlAfter = dataAfter.getNativeVlan();
        List<Integer> vlanIdAddTrunk = new ArrayList<>();
        List<Integer> vlanIdDel = new ArrayList<>();

        // get the native port to be delete or add
        if (vlAfter != null) {
            if (vlBefore != null) {
                if (!vlAfter.getValue().equals(vlBefore.getValue())) {
                    vlanIdDel.add(vlBefore.getValue());
                } else {
                    vlAfter = null;
                }
            }
        } else {
            if (vlBefore != null) {
                vlanIdDel.add(vlBefore.getValue());
            }
        }

        // get the trunk port to be delete or add
        List<TrunkVlans> lstBefore = dataBefore.getTrunkVlans();
        List<TrunkVlans> lstAfter = dataAfter.getTrunkVlans();
        List<Integer> trunkVlanIdBefore = new ArrayList<>();
        if (lstBefore != null && !lstBefore.isEmpty()) {
            trunkVlanIdBefore = lstBefore.stream().map(v -> v.getVlanId().getValue()).collect(Collectors.toList());
        }
        if (lstAfter != null && !lstAfter.isEmpty()) {
            vlanIdAddTrunk = lstAfter.stream().map(v -> v.getVlanId().getValue()).collect(Collectors.toList());
        }
        List<Integer> backup = new ArrayList<>(vlanIdAddTrunk);
        vlanIdAddTrunk.removeAll(trunkVlanIdBefore);
        trunkVlanIdBefore.removeAll(backup);
        vlanIdDel.addAll(trunkVlanIdBefore);

        // delete
        Set<Integer> run = new HashSet<>(vlanIdDel);
        if (!run.isEmpty()) {
            blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                    f(DELETE_CMD_FORMAT, StringUtils.join(run, ","), portId), "end");
        }

        // add native ports
        if (vlAfter != null) {
            blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                    f(ADD_NATIVE_CMD_FORMAT, vlAfter.getValue(), portId), "end");
        }

        // add trunk ports
        run = new HashSet<>(vlanIdAddTrunk);
        if (!run.isEmpty()) {
            blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                    f(ADD_TRUNK_CMD_FORMAT, StringUtils.join(run, ","), portId), "end");
        }
    }

    @Override
    public void writeCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id, @NotNull Config data, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);
        if (!matcher.matches()) {
            return;
        }

        String portId = matcher.group("portid");
        // add trunk ports
        List<TrunkVlans> list = data.getTrunkVlans();
        if (list != null && !list.isEmpty()) {
            List<Integer> idList = list.stream().map(v -> v.getVlanId().getValue())
                    .collect(Collectors.toList());
            Set<Integer> run = new HashSet<>(idList);
            if (!run.isEmpty()) {
                blockingWriteAndRead(cli, id, data, "configure terminal", "bridge",
                        f(ADD_TRUNK_CMD_FORMAT, StringUtils.join(run, ","), portId), "end");
            }
        }
        // add native ports
        VlanId vlNative = data.getNativeVlan();
        if (vlNative != null) {
            blockingWriteAndRead(cli, id, data, "configure terminal", "bridge",
                    f(ADD_NATIVE_CMD_FORMAT, vlNative.getValue(), portId), "end");
        }
    }

    @Override
    public void deleteCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore, @NotNull WriteContext writeContext)
            throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);
        if (!matcher.matches()) {
            return;
        }

        List<Integer> delList = new ArrayList<>();
        // add trunk ports
        List<TrunkVlans> list = dataBefore.getTrunkVlans();
        if (list != null && !list.isEmpty()) {
            delList = list.stream().map(v -> v.getVlanId().getValue()).collect(Collectors.toList());
        }
        // add native ports
        VlanId vlAfter = dataBefore.getNativeVlan();
        if (vlAfter != null) {
            delList.add(vlAfter.getValue());
        }
        // do delete
        Set<Integer> delSet = new HashSet<>(delList);
        if (delSet.isEmpty()) {
            return;
        }

        String portId = matcher.group("portid");
        blockingWriteAndRead(cli, id, dataBefore, "configure terminal", "bridge",
                f(DELETE_CMD_FORMAT, StringUtils.join(delSet, ","), portId), "end");
    }
}