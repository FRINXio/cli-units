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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.DEFAULTINSTANCE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.routed.top.routed.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RoutedVlanConfigWriter implements CliWriter<Config> {

    private static String WRITE_TEMPLATE = "configure terminal\n"
            + "{$data|update(vlan,vlan `$data.vlan.uint16`\nrouter-interface `$ifc`\n,"
            + "vlan `$before.vlan.uint16`\nno router-interface `$ifc`\n,true)}"
            + "end";

    private final Cli cli;

    public RoutedVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = processCheck(id, config, writeContext);
        blockingWriteAndRead(getWriteCommand(null, config, ifcName), cli, id, config);
    }

    private String processCheck(@Nonnull InstanceIdentifier<Config> id,
                                  @Nonnull Config config,
                                  @Nonnull WriteContext writeContext) {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        Preconditions.checkArgument(L3ipvlan.class.equals(Util.parseType(ifcName)),
                f("Interface '%s' must be of type L3IPVLAN", ifcName));

        boolean contains = writeContext.readAfter(IIDs.NETWORKINSTANCES)
                .get()
                .getNetworkInstance().stream()
                .filter(i -> DEFAULTINSTANCE.class.equals(i.getConfig().getType()))
                .flatMap(i -> i.getVlans().getVlan().stream())
                .map(Vlan::getVlanId)
                .map(VlanId::getValue)
                .anyMatch(i -> config.getVlan().getUint16().equals(i));
        Preconditions.checkArgument(contains,
                f("All vlans used in interface '%s' must be part of DEFAULT network instance", ifcName));
        return ifcName;
    }

    @VisibleForTesting
    String getWriteCommand(Config dataBefore, Config dataAfter, String ifcName) {
        return fT(WRITE_TEMPLATE, "ifc", ifcName, "data", dataAfter, "before", dataBefore);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore, @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = processCheck(id, dataAfter, writeContext);
        blockingWriteAndRead(getWriteCommand(dataBefore, dataAfter, ifcName), cli, id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(getWriteCommand(config, null, ifcName), cli, id);
    }
}
