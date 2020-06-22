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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.LogicalRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.VirtualRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.VirtualRingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.VirtualRingKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingReader implements CliConfigListReader<VirtualRing, VirtualRingKey, VirtualRingBuilder> {

    private static final String SH_VIRTUAL_RING = "configuration search string \"virtual-ring create\"";

    private Cli cli;

    public VirtualRingReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<VirtualRingKey> getAllIds(@Nonnull InstanceIdentifier<VirtualRing> instanceIdentifier,
                                          @Nonnull ReadContext readContext) throws ReadFailedException {
        String logicalRingName = instanceIdentifier.firstKeyOf(LogicalRing.class).getName();
        String output = blockingRead(SH_VIRTUAL_RING, cli, instanceIdentifier, readContext);

        return getAllIds(output, logicalRingName);
    }

    @VisibleForTesting
    static List<VirtualRingKey> getAllIds(String output, String logicalRingKey) {
        Pattern pattern = Pattern.compile(".*create virtual-ring-name (?<name>\\S+) logical-ring "
                + logicalRingKey + ".*");

        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("name"),
            VirtualRingKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<VirtualRing> instanceIdentifier,
                                      @Nonnull VirtualRingBuilder virtualRingBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        virtualRingBuilder.setKey(instanceIdentifier.firstKeyOf(VirtualRing.class));
    }
}
