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

package io.frinx.cli.unit.saos.logical.ring.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.LogicalRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.LogicalRingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.LogicalRingKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LogicalRingReader implements CliConfigListReader<LogicalRing, LogicalRingKey, LogicalRingBuilder> {

    static final String SH_LOGICAL_RING = "configuration search string \"logical-ring\"";

    private static final Pattern LOGICAL_RING_NAME =
            Pattern.compile(".*create logical-ring-name (?<name>\\S+).*");

    private final Cli cli;

    public LogicalRingReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<LogicalRingKey> getAllIds(@NotNull InstanceIdentifier<LogicalRing> instanceIdentifier,
                                          @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_LOGICAL_RING, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<LogicalRingKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            LOGICAL_RING_NAME::matcher,
            matcher -> matcher.group("name"),
            name -> new LogicalRingKey(name));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<LogicalRing> instanceIdentifier,
                                      @NotNull LogicalRingBuilder logicalRingBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        logicalRingBuilder.setKey(instanceIdentifier.firstKeyOf(LogicalRing.class));
    }
}