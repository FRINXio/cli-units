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

package io.frinx.cli.unit.brocade.network.instance.vrf.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultVlanConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_VLAN_CONFIG = "show running-config vlan | begin ^vlan {$id}";

    private final Cli cli;

    public DefaultVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isDefaultVlan(instanceIdentifier, readContext)) {
            VlanId id = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
            parseVlanConfig(blockingRead(fT(SH_VLAN_CONFIG, "id", id.getValue()),
                    cli, instanceIdentifier, readContext), configBuilder, id);
        }
    }

    @VisibleForTesting
    static void parseVlanConfig(String output, ConfigBuilder configBuilder, VlanId id) {
        output = output.substring(0, output.indexOf(Cli.NEWLINE + "!"));

        configBuilder.setVlanId(id);
        // Brocade does not support suspended vlans
        configBuilder.setStatus(VlanConfig.Status.ACTIVE);
        ParsingUtils.parseField(output,
                DefaultVlanReader.VLAN_ID_LINE::matcher, m -> m.group("name"), configBuilder::setName);
    }

    private boolean isDefaultVlan(InstanceIdentifier<?> id, ReadContext readContext) throws ReadFailedException {
        return DefaultVlanReader.getAllIds(id, readContext, cli, this).contains(id.firstKeyOf(Vlan.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.checkData(
                ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE);
    }
}