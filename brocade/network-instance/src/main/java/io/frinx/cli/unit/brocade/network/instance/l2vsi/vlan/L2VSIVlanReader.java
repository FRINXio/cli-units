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

package io.frinx.cli.unit.brocade.network.instance.l2vsi.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.vlan.L2P2PVlanReader;
import io.frinx.cli.unit.brocade.network.instance.l2vsi.ifc.L2VSIInterfaceReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIVlanReader implements CompositeListReader.Child<Vlan, VlanKey, VlanBuilder>,
        CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    private final Cli cli;

    public L2VSIVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<VlanKey> getAllIds(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {
        if (!L2VSIInterfaceReader.L2VSI_CHECK.canProcess(instanceIdentifier, readContext)) {
            return Collections.emptyList();
        }
        String vpnName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String output = blockingRead(Command.showCommand(L2P2PVlanReader.SH_VLL_VPLS_VLAN, readContext),
                cli, instanceIdentifier);
        return parseVlans(output, vpnName);
    }

    /*
    !
    !
     vll 99999 5
     vll FRINXtest 66661446
      vlan 666
     vll RADTEST 178
      vlan 1551
     vpls managementvpls 12
      vlan 11
     vpls mgmtma14 14
      vlan 14
    !
    */

    @VisibleForTesting
    static List<VlanKey> parseVlans(String output, String vpnName) {
        int startIndex = output.contains("vpls " + vpnName) ? output.indexOf("vpls " + vpnName) : 0;
        if (startIndex == 0) {
            return Collections.emptyList();
        }

        output = output.substring(startIndex);
        int endIndex = output.contains("\n vpls") ? output.indexOf("\n vpls") : 0;
        if (endIndex == 0) {
            endIndex = output.contains("\n!") ? output.indexOf("\n!") : 0;
        }
        output = endIndex == 0 ? "" : output.substring(0, endIndex);
        return ParsingUtils.parseFields(output, 0,
            L2P2PVlanReader.VLAN_ID_LINE::matcher,
            matcher -> matcher.group("id"),
            id -> new VlanKey(new VlanId(Integer.valueOf(id))));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @Nonnull VlanBuilder vlanBuilder,
                                      @Nonnull ReadContext readContext) {
        vlanBuilder.setVlanId(instanceIdentifier.firstKeyOf(Vlan.class).getVlanId());
    }

    @Override
    public Check getCheck() {
        return L2VSIInterfaceReader.L2VSI_CHECK;
    }
}