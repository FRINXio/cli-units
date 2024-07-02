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

package io.frinx.cli.unit.saos.logical.ring.handler.virtual;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.VirtualRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.Config.RingType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_VIRT_RING_CONF = "configuration search string \"%s\"";
    private static final Pattern RING_TYPE_FROM_CONFIG = Pattern
            .compile("ring-protection virtual-ring create virtual-ring-name \\S+ .+? sub-ring .+?");

    private Cli cli;

    public VirtualRingConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var virtualRing = instanceIdentifier.firstKeyOf(VirtualRing.class).getName();
        configBuilder.setName(virtualRing);
        final var ringOutput = blockingRead(f(SH_VIRT_RING_CONF, virtualRing),
                cli, instanceIdentifier, readContext);
        parseConfig(ringOutput, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String ringOutput, ConfigBuilder configBuilder) {
        var matcher = RING_TYPE_FROM_CONFIG.matcher(ringOutput);
        if (matcher.find()) {
            configBuilder.setRingType(RingType.SubRing);
        } else {
            configBuilder.setRingType(RingType.MajorRing);
        }
    }
}