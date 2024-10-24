/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.huawei.ifc.handler.ethernet;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String IFC_ETHERNET_CONFIG_TEMPLATE = """
            system-view
            interface {$ifc_name}
            {% if ($channel_id_before) %}undo eth-trunk
            {% endif %}{% if ($channel_id) %}eth-trunk {$channel_id}
            {% endif %}return""";

    private static final String IFC_ETHERNET_CONFIG_DELETE_TEMPLATE = """
            system-view
            interface {$ifc_name}
            {% if ($channel_id_before) %}undo eth-trunk
            {% endif %}return""";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        final var channelId = getChannelId(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "channel_id", channelId.orElse(null),
                        "channel_id_before", null));
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                        @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter,
                                        @NotNull final WriteContext writeContext) throws WriteFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        var channelId = getChannelId(dataAfter);
        var chIdBefore = getChannelId(dataBefore);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "channel_id", channelId.orElse(null),
                        "channel_id_before", chIdBefore.orElse(null)));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        var chIdBefore = getChannelId(dataBefore);

        blockingDeleteAndRead(cli, id,
                fT(IFC_ETHERNET_CONFIG_DELETE_TEMPLATE,
                        "ifc_name", ifcName,
                        "channel_id_before", chIdBefore.orElse(null)));
    }

    private static Optional<Long> getChannelId(Config dataAfter) {
        var aggregationAug = dataAfter.getAugmentation(Config1.class);
        if (aggregationAug == null || aggregationAug.getAggregateId() == null) {
            return Optional.empty();
        }
        var aggregateIfcName = aggregationAug.getAggregateId();
        return Optional.of(Long.valueOf(aggregateIfcName));
    }
}