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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.PhysicalPortInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortVlanMemberConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public PhysicalPortVlanMemberConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
            WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        String portId = getEthernetPortId(ifcName);

        Set<Integer> beforeVlans = getVlanIds(dataBefore);
        Set<Integer> afterVlans = getVlanIds(dataAfter);

        if (!dataBefore.getInterfaceMode().equals(dataAfter.getInterfaceMode())) {
            configureVlans(portId, dataAfter.getInterfaceMode(), id, dataAfter, beforeVlans, afterVlans);
            return;
        }

        Set<Integer> deleted = Sets.difference(beforeVlans, afterVlans);
        Set<Integer> created = Sets.difference(afterVlans, beforeVlans);

        configureVlans(portId, dataAfter.getInterfaceMode(), id, dataAfter, deleted, created);
    }

    @VisibleForTesting
    Set<Integer> getVlanIds(Config data) {
        Set<Integer> vlans = new HashSet<>();

        if (VlanModeType.ACCESS.equals(data.getInterfaceMode())) {
            vlans.add(data.getAccessVlan().getValue());
        } else if (VlanModeType.TRUNK.equals(data.getInterfaceMode())) {
            vlans = data.getTrunkVlans().stream().map(v -> v.getVlanId()).map(v -> v.getValue())
                    .collect(Collectors.toSet());
        }
        return vlans;
    }

    private void configureVlans(String portId, VlanModeType mode, InstanceIdentifier<Config> id, Config dataAfter,
            Set<Integer> deleted, Set<Integer> created) throws WriteFailedException.CreateFailedException {

        String tagtype = VlanModeType.ACCESS.equals(mode) ? "untagged" : "tagged";

        if (!deleted.isEmpty()) {
            blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                    f("vlan del %s %s", StringUtils.join(deleted, ","), portId), "end");
        }

        if (!created.isEmpty()) {
            blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                    f("vlan add %s %s %s", StringUtils.join(created, ","), portId, tagtype), "end");
        }
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        String portId = getEthernetPortId(ifcName);

        Set<Integer> afterVlans = getVlanIds(dataAfter);

        configureVlans(portId, dataAfter.getInterfaceMode(), id, dataAfter, Collections.emptySet(), afterVlans);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        String portId = getEthernetPortId(ifcName);

        Set<Integer> beforeVlans = getVlanIds(dataBefore);
        configureVlans(portId, dataBefore.getInterfaceMode(), id, dataBefore, beforeVlans, Collections.emptySet());
    }

    private static String getEthernetPortId(String ifcName) {

        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);

        Preconditions.checkState(matcher.matches(),
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);

        return matcher.group("portid");
    }
}
