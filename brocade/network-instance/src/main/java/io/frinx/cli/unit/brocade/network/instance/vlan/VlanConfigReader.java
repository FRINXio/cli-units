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

package io.frinx.cli.unit.brocade.network.instance.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_VLAN_CONFIG = "show running-config vlan | begin ^vlan {$id}";

    private final Cli cli;

    public VlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        VlanId id = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        parseVlanConfig(blockingRead(fT(SH_VLAN_CONFIG, "id", id.getValue()),
                cli, instanceIdentifier, readContext), configBuilder, id);
    }

    @VisibleForTesting
    static void parseVlanConfig(String output, ConfigBuilder configBuilder, VlanId id) {
        output = output.substring(0, output.indexOf(Cli.NEWLINE + "!"));

        configBuilder.setVlanId(id);
        // Brocade does not support suspended vlans
        configBuilder.setStatus(VlanConfig.Status.ACTIVE);
        ParsingUtils.parseField(output,
                VlanReader.VLAN_ID_LINE::matcher, m -> m.group("name"), configBuilder::setName);

    }
}
