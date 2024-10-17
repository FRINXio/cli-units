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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.L2vsicpReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2vsicpVlanReader implements CompositeListReader.Child<Vlan, VlanKey, VlanBuilder>,
        CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    private final Cli cli;

    public L2vsicpVlanReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<VlanKey> getAllIds(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                   @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(L2vsicpReader.SHOW_VC, cli, instanceIdentifier, readContext);
        String vcName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        return getVlanKeys(output, vcName);
    }

    @VisibleForTesting
    static List<VlanKey> getVlanKeys(String output, String vcName) {
        Pattern vcVlan = Pattern
                .compile("virtual-circuit ethernet create vc " + vcName + " vlan (?<vlan>\\S+).*");
        return ParsingUtils.parseFields(output, 0,
            vcVlan::matcher,
            m -> m.group("vlan"),
            k -> new VlanKey(new VlanId(Integer.valueOf(k))));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @NotNull VlanBuilder vlanBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        VlanId vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        vlanBuilder.setVlanId(vlanId);
    }

    @Override
    public Check getCheck() {
        return L2vsicpReader.L2VSICP_CHECK;
    }
}