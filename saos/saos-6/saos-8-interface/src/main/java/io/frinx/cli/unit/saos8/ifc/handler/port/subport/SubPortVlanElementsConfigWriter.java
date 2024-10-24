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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.element.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortVlanElementsConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "sub-port add sub-port {$subIfcName} class-element {$data.id}"
            + "{% if ($data.vtag_stack) %} vtag-stack {$data.vtag_stack}{% endif %}"
            + "{% if ($untagged_data) %} vlan-untagged-data{% endif %}";

    @SuppressWarnings("checkstyle:linelength")
    private static final String UPDATE_TEMPLATE = """
            sub-port remove sub-port {$subIfcName} class-element {$before.id}
            sub-port add sub-port {$subIfcName} class-element {$data.id}{% if ($data.vtag_stack) %} vtag-stack {$data.vtag_stack}{% endif %}{% if ($untagged_data) %} vlan-untagged-data{% endif %}
            """;

    private static final String DELETE_TEMPLATE =
            "sub-port remove sub-port {$subIfcName} class-element {$data.id}";

    private Cli cli;

    public SubPortVlanElementsConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            var subPort = writeContext
                    .readAfter(instanceIdentifier.firstIdentifierOf(Subinterface.class));
            if (subPort.isPresent()) {
                final var subPortName = subPort.get().getConfig().getName();
                blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, subPortName));
            } else {
                throw new IllegalStateException("Cannot read sub-interface name");
            }
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String subIfcName) {
        return fT(WRITE_TEMPLATE, "data", config,
                    "untagged_data", config.isVlanUntaggedData() != null && config.isVlanUntaggedData()
                        ? Chunk.TRUE : null,
                    "subIfcName", subIfcName);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(id, writeContext, false)) {
            var subPort = writeContext.readAfter(id.firstIdentifierOf(Subinterface.class));
            if (subPort.isPresent()) {
                final var subPortName = subPort.get().getConfig().getName();
                blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter, subPortName));
            } else {
                throw new IllegalStateException("Cannot read sub-interface name");
            }
        }
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter, String subIfcName) {
        return fT(UPDATE_TEMPLATE, "data", dataAfter,
                    "before", dataBefore,
                    "subIfcName", subIfcName,
                    "untagged_data", dataAfter.isVlanUntaggedData() ? Chunk.TRUE : null);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            var subPort = writeContext
                    .readBefore(instanceIdentifier.firstIdentifierOf(Subinterface.class));
            if (subPort.isPresent()) {
                final var subPortName = subPort.get().getConfig().getName();
                blockingDelete(deleteTemplate(config, subPortName), cli, instanceIdentifier);
            } else {
                throw new IllegalStateException("Cannot read sub-interface name");
            }
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String subIfcName) {
        return fT(DELETE_TEMPLATE, "data", config,
                "subIfcName", subIfcName);
    }
}