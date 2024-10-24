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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingKey;
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
        String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        if (output.contains(vsName)) {
            return getAllIds(output, vsName);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<VirtualRingKey> getAllIds(String output, String vsName) {
        Pattern vrPattern = Pattern.compile("ring-protection virtual-ring add ring (?<name>\\S+)"
                + " vs " + vsName);
        return ParsingUtils.parseFields(output, 0,
            vrPattern::matcher,
            matcher -> matcher.group("name"),
            VirtualRingKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<VirtualRing> instanceIdentifier,
                                      @NotNull VirtualRingBuilder virtualRingBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        virtualRingBuilder.setKey(instanceIdentifier.firstKeyOf(VirtualRing.class));
    }
}