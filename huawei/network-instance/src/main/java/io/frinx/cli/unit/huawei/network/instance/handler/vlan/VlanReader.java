/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.network.instance.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanReader implements CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    private static final String DISP_VLAN = "display vlan";
    private static final Pattern VLAN_ID_LINE = Pattern.compile("^(?<id>[0-9]+).*");

    private final Cli cli;

    public VlanReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<VlanKey> getAllIds(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                   @NotNull ReadContext readContext) throws ReadFailedException {
        return parseVlans(blockingRead(DISP_VLAN, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<VlanKey> parseVlans(String output) {
        return ParsingUtils.parseFields(output, 0,
                VLAN_ID_LINE::matcher, m -> Integer.valueOf(m.group("id")), id -> new VlanKey(new VlanId(id)));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @NotNull VlanBuilder vlanBuilder,
                                      @NotNull ReadContext readContext) {
        vlanBuilder.setVlanId(instanceIdentifier.firstKeyOf(Vlan.class).getVlanId());
    }
}