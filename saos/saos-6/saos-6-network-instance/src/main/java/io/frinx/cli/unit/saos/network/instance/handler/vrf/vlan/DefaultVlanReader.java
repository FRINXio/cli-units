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
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultVlanReader implements CompositeListReader.Child<Vlan, VlanKey, VlanBuilder>,
        CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    private static final Pattern VLAN_IDS = Pattern.compile("\\|\\s+(?<id>\\d+)\\|\\w+.*");
    private static final String SHOW_VLANS = "vlan show";

    private final Cli cli;

    public DefaultVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<VlanKey> getAllIds(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {
        if (!instanceIdentifier.firstKeyOf(NetworkInstance.class).equals(NetworInstance.DEFAULT_NETWORK)) {
            return Collections.emptyList();
        }

        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    static List<VlanKey> getAllIds(Cli cli, CliReader cliReader,
                                   @Nonnull InstanceIdentifier<?> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {

        String output = cliReader.blockingRead(SHOW_VLANS, cli, instanceIdentifier, readContext);
        return ParsingUtils.parseFields(output,
            0,
            VLAN_IDS::matcher,
            matcher -> matcher.group("id"),
            (id) -> new VlanKey(new VlanId(Integer.parseInt(id))));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @Nonnull VlanBuilder vlanBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        vlanBuilder.setKey(instanceIdentifier.firstKeyOf(Vlan.class)).getVlanId();
    }

    @Override
    public Check getCheck() {

        // TODO: fill in
        return null;
    }
}
