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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.ifc.L2P2PInterfaceReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PVlanReader implements CompositeListReader.Child<Vlan, VlanKey, VlanBuilder>,
        CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    public static final String SH_VLL_VPLS_VLAN = "show running-config | include ^ vll|^ vpls|^  vlan|^!";
    public static final Pattern VLAN_ID_LINE = Pattern.compile("vlan (?<id>\\S+)");

    private final Cli cli;

    public L2P2PVlanReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<VlanKey> getAllIds(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                   @NotNull ReadContext readContext) throws ReadFailedException {
        if (!L2P2PInterfaceReader.L2P2P_CHECK.canProcess(instanceIdentifier, readContext)) {
            return Collections.emptyList();
        }
        String vpnName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String output = blockingRead(Command.showCommand(SH_VLL_VPLS_VLAN, readContext), cli, instanceIdentifier);
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
        int startIndex = output.contains("vll " + vpnName) ? output.indexOf("vll " + vpnName) : 0;
        if (startIndex == 0) {
            startIndex = output.contains("vll-local " + vpnName) ? output.indexOf("vll-local " + vpnName) : 0;
        }
        if (startIndex == 0) {
            return Collections.emptyList();
        }

        output = output.substring(startIndex);
        int endIndex = output.contains("\n vll") ? output.indexOf("\n vll") : 0;
        if (endIndex == 0) {
            endIndex = output.contains("\n vpls") ? output.indexOf("\n vpls") : 0;
        }
        if (endIndex == 0) {
            endIndex = output.contains("\n!") ? output.indexOf("\n!") : 0;
        }

        output = endIndex == 0 ? "" : output.substring(0, endIndex);
        return ParsingUtils.parseFields(output, 0,
            VLAN_ID_LINE::matcher,
            matcher -> matcher.group("id"),
            id -> new VlanKey(new VlanId(Integer.valueOf(id))));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @NotNull VlanBuilder vlanBuilder,
                                      @NotNull ReadContext readContext) {
        vlanBuilder.setVlanId(instanceIdentifier.firstKeyOf(Vlan.class).getVlanId());
    }

    @Override
    public Check getCheck() {
        return L2P2PInterfaceReader.L2P2P_CHECK;
    }
}