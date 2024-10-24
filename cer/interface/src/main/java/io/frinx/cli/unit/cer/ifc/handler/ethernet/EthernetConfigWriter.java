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

package io.frinx.cli.unit.cer.ifc.handler.ethernet;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.arris.rev220506.IfArrisExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String ETH_LINK_AGGREGATE_WRITE = """
            configure
            interface {$ifc_name}
            {% if ($link_aggregate) %}link-aggregate {$link_aggregate}
            {% endif %}end""";

    private static final String ETH_LINK_AGGREGATE_UPDATE = """
            configure
            interface {$ifc_name}
            {% if ($link_aggregate_remove) %}no link-aggregate {$link_aggregate_remove}
            {% endif %}{% if ($link_aggregate) %}link-aggregate {$link_aggregate}
            {% endif %}end""";

    private static final String ETH_LINK_AGGREGATE_DELETE = """
            configure
            interface {$ifc_name}
            {% if ($link_aggregate_remove) %}no link-aggregate {$link_aggregate_remove}
            {% endif %}end""";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext ctx) throws WriteFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, id, config, writeTemplate(config, ifcName));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext ctx) throws WriteFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter, ifcName));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext ctx) throws WriteFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, id, deleteTemplate(config, ifcName));
    }

    @VisibleForTesting
    String writeTemplate(Config config, String ifcName) {
        return fT(ETH_LINK_AGGREGATE_WRITE,
                "ifc_name", ifcName,
                "link_aggregate", getLinkAggregate(config));
    }

    @VisibleForTesting
    String updateTemplate(Config before, Config after, String ifcName) {
        return fT(ETH_LINK_AGGREGATE_UPDATE,
                "ifc_name", ifcName,
                "link_aggregate_remove", getLinkAggregate(before),
                "link_aggregate", getLinkAggregate(after));
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String ifcName) {
        return fT(ETH_LINK_AGGREGATE_DELETE,
                "ifc_name", ifcName,
                "link_aggregate_remove", getLinkAggregate(config));
    }

    private String getLinkAggregate(Config config) {
        if (config != null && config.getAugmentation(IfArrisExtensionAug.class) != null
                && config.getAugmentation(IfArrisExtensionAug.class).getLinkAggregate() != null) {
            return config.getAugmentation(IfArrisExtensionAug.class).getLinkAggregate().toString();
        }
        return null;
    }
}