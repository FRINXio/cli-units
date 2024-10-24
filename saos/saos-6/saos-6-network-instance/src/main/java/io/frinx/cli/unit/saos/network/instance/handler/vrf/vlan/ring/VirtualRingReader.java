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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.ring;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingReader implements CliConfigListReader<VirtualRing, VirtualRingKey, VirtualRingBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"virtual-ring add\"";

    private Cli cli;

    public VirtualRingReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<VirtualRingKey> getAllIds(@NotNull InstanceIdentifier<VirtualRing> instanceIdentifier,
                                          @NotNull ReadContext readContext) throws ReadFailedException {
        String vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId().getValue().toString();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        return getAllIds(output, vlanId);
    }

    @VisibleForTesting
    static List<VirtualRingKey> getAllIds(String output, String vlanId) {
        String vlan = getVlan(output, vlanId);
        Pattern vrPattern = Pattern.compile("ring-protection virtual-ring add ring (?<name>\\S+)"
                + " vid " + vlan);
        return ParsingUtils.parseFields(output, 0,
            vrPattern::matcher,
            matcher -> matcher.group("name"),
            VirtualRingKey::new);
    }

    static String getVlan(String output, String vlanId) {
        String[] vlans = output.split("\r?\n");
        for (String vlan: vlans) {
            String vlanNumber = vlan.substring(vlan.lastIndexOf(' ') + 1);
            if (vlanNumber.contains("-")) {
                List<Integer> range = getRange(vlanNumber);
                for (int index = range.get(0); index <= range.get(1); index++) {
                    if (vlanId.equals(String.valueOf(index))) {
                        return vlanNumber;
                    }
                }
            }
        }
        return vlanId;
    }

    private static List<Integer> getRange(String vlanNumber) {
        return Arrays.stream(vlanNumber.split("-")).map(Integer::parseInt).collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<VirtualRing> instanceIdentifier,
                                      @NotNull VirtualRingBuilder virtualRingBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        virtualRingBuilder.setKey(instanceIdentifier.firstKeyOf(VirtualRing.class));
    }
}