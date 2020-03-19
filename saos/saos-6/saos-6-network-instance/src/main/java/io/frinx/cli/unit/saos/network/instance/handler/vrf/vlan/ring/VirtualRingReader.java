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
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.rings.Ring;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.rings.RingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.rings.RingKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingReader implements CliConfigListReader<Ring, RingKey, RingBuilder> {

    private static final String SHOW_COMMAND = "configuration search running-config string \"ring-protection\"";

    private Cli cli;

    public VirtualRingReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<RingKey> getAllIds(@Nonnull InstanceIdentifier<Ring> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {
        String vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId().getValue().toString();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        return getAllIds(output, vlanId);
    }

    @VisibleForTesting
    static List<RingKey> getAllIds(String output, String vlanId) {
        Pattern vrPattern = Pattern.compile("ring-protection virtual-ring add ring (?<name>\\S+)"
                + " vid " + vlanId);
        return ParsingUtils.parseFields(output, 0,
            vrPattern::matcher,
            matcher -> matcher.group("name"),
            RingKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Ring> instanceIdentifier,
                                      @Nonnull RingBuilder ringBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ringBuilder.setKey(instanceIdentifier.firstKeyOf(Ring.class));
    }
}