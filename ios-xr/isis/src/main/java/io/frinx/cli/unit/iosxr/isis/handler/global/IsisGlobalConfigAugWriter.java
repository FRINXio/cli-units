/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.isis.handler.global;

import com.google.common.collect.Iterables;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisGlobalConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisInternalLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisGlobalConfigAugWriter implements CliWriter<IsisGlobalConfAug> {
    private final Cli cli;

    private static final String COMMAND_TEMPLATE = """
            router isis {$instanceName}
            {% loop in $removedMetrics as $level %}no max-link-metric {$level}
            {% onEmpty %}{% endloop %}{% loop in $addedMetrics as $level %}max-link-metric {$level}
            {% onEmpty %}{% endloop %}root""";

    public IsisGlobalConfigAugWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<IsisGlobalConfAug> iid,
            @NotNull IsisGlobalConfAug data,
            @NotNull WriteContext context) throws WriteFailedException {

        String instanceName = iid.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, iid, data,
            fT(COMMAND_TEMPLATE,
                "instanceName", instanceName,
                "removedMetrics", Collections.emptyList(),
                "addedMetrics", data.getMaxLinkMetric().stream()
                    .map(IsisGlobalConfigAugWriter::convertIsisInternalLevelToString)
                    .collect(Collectors.toList())));
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<IsisGlobalConfAug> iid,
            @NotNull IsisGlobalConfAug dataBefore,
            @NotNull IsisGlobalConfAug dataAfter, @NotNull WriteContext context) throws WriteFailedException {

        final String instanceName = iid.firstKeyOf(Protocol.class).getName();

        List<String> removedMetrics = dataBefore.getMaxLinkMetric().stream()
            .filter(v -> !Iterables.contains(dataAfter.getMaxLinkMetric(), v))
            .map(IsisGlobalConfigAugWriter::convertIsisInternalLevelToString)
            .collect(Collectors.toList());

        List<String> addedMetrics = dataAfter.getMaxLinkMetric().stream()
            .filter(v -> !Iterables.contains(dataBefore.getMaxLinkMetric(), v))
            .map(IsisGlobalConfigAugWriter::convertIsisInternalLevelToString)
            .collect(Collectors.toList());

        blockingWriteAndRead(cli, iid, dataAfter,
            fT(COMMAND_TEMPLATE,
                "instanceName", instanceName,
                "removedMetrics", removedMetrics,
                "addedMetrics", addedMetrics));
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<IsisGlobalConfAug> iid,
            @NotNull IsisGlobalConfAug data,
            @NotNull WriteContext context) throws WriteFailedException {

        String instanceName = iid.firstKeyOf(Protocol.class).getName();

        blockingDeleteAndRead(cli, iid,
            fT(COMMAND_TEMPLATE,
                "instanceName", instanceName,
                "removedMetrics", data.getMaxLinkMetric().stream()
                    .map(IsisGlobalConfigAugWriter::convertIsisInternalLevelToString)
                    .collect(Collectors.toList()),
                "addedMetrics", Collections.emptyList()));
    }

    private static String convertIsisInternalLevelToString(IsisInternalLevel level) {
        return switch (level) {
            case LEVEL1 -> "level 1";
            case LEVEL2 -> "level 2";
            default -> "";  // NOTSET
        };
    }
}