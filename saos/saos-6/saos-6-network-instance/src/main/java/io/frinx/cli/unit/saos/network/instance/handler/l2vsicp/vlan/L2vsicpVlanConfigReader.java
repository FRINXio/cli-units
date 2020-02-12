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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2vsicpVlanConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String PATTERN = "virtual-circuit ethernet create vc %s vlan %s statistics on";

    private final Cli cli;

    public L2vsicpVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return L2vsicpReader.L2VSICP_CHECK;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        VlanId vlanId = id.firstKeyOf(Vlan.class).getVlanId();
        String name = id.firstKeyOf(NetworkInstance.class).getName();

        String output = blockingRead(L2vsicpReader.SHOW_VC, cli, id, ctx);

        fillBuilder(builder, output, vlanId, name);
    }

    @VisibleForTesting
    static void fillBuilder(@Nonnull ConfigBuilder builder, String output, VlanId vlanId, String name) {
        builder.setVlanId(vlanId);

        Config2Builder statBuilder = new Config2Builder();
        statBuilder.setStatistics(false);

        Pattern statisticsPattern = Pattern.compile(String.format(PATTERN, name, vlanId.getValue()));

        ParsingUtils.parseField(output,
            statisticsPattern::matcher,
            m -> true,
            statBuilder::setStatistics);

        builder.addAugmentation(Config2.class, statBuilder.build());
    }
}