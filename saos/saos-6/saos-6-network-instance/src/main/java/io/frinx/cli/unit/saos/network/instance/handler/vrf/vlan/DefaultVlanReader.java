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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
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

    private static final String SHOW_VLANS = "configuration search running-config string \"create vlan\"";
    private static final Pattern VLAN_ONE = Pattern.compile("vlan create vlan (?<id>\\w+(-\\w+)?).*");
    private static final Pattern VLANS_TWO = Pattern.compile("vlan create vlan .*,(?<id>\\w+(-\\w+)?)");
    private static final Check CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE);

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

        return getIds(cli, this, instanceIdentifier, readContext);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @Nonnull VlanBuilder vlanBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        vlanBuilder.setKey(instanceIdentifier.firstKeyOf(Vlan.class)).getVlanId();
    }

    @VisibleForTesting
    static List<VlanKey> getIds(Cli cli, CliReader cliReader, @Nonnull InstanceIdentifier<?> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {

        String output = cliReader.blockingRead(SHOW_VLANS, cli, instanceIdentifier, readContext);
        List<VlanKey> ids = new ArrayList<>();

        ids.addAll(parseAllIds(output, VLAN_ONE));
        ids.addAll(parseAllIds(output, VLANS_TWO));

        return ids;
    }

    static List<VlanKey> parseAllIds(String output, Pattern pattern) {
        List<VlanKey> list = new ArrayList<>();
        ParsingUtils.parseFields(output,
            0,
            pattern::matcher,
            matcher -> matcher.group("id"),
            n -> {
                if (n.contains("-")) {
                    String[] split = n.split("-");
                    for (int i = Integer.parseInt(split[0]); i <= Integer.parseInt(split[1]); i++) {
                        list.add(new VlanKey(new VlanId(i)));
                    }
                } else {
                    list.add(new VlanKey(new VlanId(Integer.parseInt(n))));
                }
                return null;
            });
        return list;
    }

    @Override
    public Check getCheck() {
        return CHECK;
    }
}
