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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultVlanConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_RENAMES = "configuration search running-config string \"rename vlan %d \"";
    private static final String SH_TPID = "configuration search running-config string \"%d egress-tpid\"";
    private static final Pattern VLAN_NAME_PATTERN =
            Pattern.compile("vlan rename vlan \\d+ name (?<name>\\w+(.*\\w+)*)");
    private static final Pattern VLAN_TPID_PATTERN = Pattern.compile("vlan set vlan \\d+ egress-tpid (?<tpid>\\w+)");

    private Cli cli;

    public DefaultVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        if (!instanceIdentifier.firstKeyOf(NetworkInstance.class).equals(NetworInstance.DEFAULT_NETWORK)) {
            return;
        }

        VlanId vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        String output = blockingRead(f(SH_RENAMES, vlanId.getValue()), cli, instanceIdentifier, readContext)
                + "\n" + blockingRead(f(SH_TPID, vlanId.getValue()), cli, instanceIdentifier, readContext);

        parseVlanConfig(output, configBuilder, new Config1Builder(), vlanId);
    }

    @VisibleForTesting
    static void parseVlanConfig(String output, ConfigBuilder configBuilder,
                                Config1Builder builder, VlanId vlanId) {
        configBuilder.setVlanId(vlanId);
        configBuilder.setName(String.format("VLAN#%d", vlanId.getValue()));
        builder.setEgressTpid(parseTpid("8100"));

        ParsingUtils.parseField(output, VLAN_NAME_PATTERN::matcher, matcher -> matcher.group("name"),
            configBuilder::setName);

        ParsingUtils.parseField(output, VLAN_TPID_PATTERN::matcher, matcher -> matcher.group("tpid"),
            tpid -> builder.setEgressTpid(parseTpid(tpid)));

        configBuilder.addAugmentation(Config1.class, builder.build());
    }

    static Class<? extends TPIDTYPES> parseTpid(String tpid) {
        switch (tpid) {
            case "9100":
                return TPID0X9100.class;
            case "8100":
                return TPID0X8100.class;
            default:
                return TPID0X88A8.class;
        }
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
