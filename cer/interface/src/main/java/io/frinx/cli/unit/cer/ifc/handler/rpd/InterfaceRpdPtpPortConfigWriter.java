/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.config.PtpPort;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.top.rpd.ptp.port.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdPtpPortConfigWriter implements CliWriter<Config> {

    private static final String INTERFACE_NAME_TEMPLATE = "configure interface {$name}\n";

    private static final String WRITE_TEMPLATE = """
            ptp port {$id} role {$role}
            {% if($local_priority) %}ptp port {$id} local-priority {$local_priority}
            {% endif %}ptp port {$id} master-clock address {$master_clock_address}
            {% if($enable) %}ptp port {$id} no shutdown
            {% else %}ptp port {$id} shutdown
            {% endif %}""";

    private static final String BEFORE_UPDATE_TEMPLATE = """
            {% if($local_priority) %}ptp port {$id} no local-priority {$local_priority}
            {% endif %}{% if($master_clock_address) %}ptp port {$id} shutdown
            ptp port {$id} no master-clock
            {% endif %}ptp port {$id} shutdown
            """;

    private static final String AFTER_UPDATE_TEMPLATE = """
            ptp port {$id} role {$role}
            {% if($local_priority) %}ptp port {$id} local-priority {$local_priority}
            {% endif %}ptp port {$id} master-clock address {$master_clock_address}
            {% if($enable) %}ptp port {$id} no shutdown
            {% else %}ptp port {$id} shutdown
            {% endif %}""";

    private static final String DELETE_TEMPLATE = """
            configure interface {$name}
            {% loop in $before.ptp_port as $ptp_port %}ptp port {$ptp_port.id} shutdown
            ptp port {$ptp_port.id} no master-clock
            no ptp port {$ptp_port.id}
            {% endloop %}end""";

    private final Cli cli;

    public InterfaceRpdPtpPortConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        StringBuilder commands = new StringBuilder();

        commands.append(fT(INTERFACE_NAME_TEMPLATE,
                "name", name));
        dataAfter.getPtpPort().forEach(ptpPort -> {
            commands.append(fT(WRITE_TEMPLATE,
                    "id", ptpPort.getId(),
                    "role", ptpPort.getRole(),
                    "local_priority", ptpPort.getLocalPriority(),
                    "master_clock_address", ptpPort.getMasterClockAddress(),
                    "enable", (ptpPort.isEnable() != null && ptpPort.isEnable())
                            ? Chunk.TRUE : null));
        });
        commands.append("end");
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, commands.toString());
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore, @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        StringBuilder commands = new StringBuilder();

        commands.append(fT(INTERFACE_NAME_TEMPLATE,
                "name", name));
        commands.append(updatePtpPorts(dataBefore, dataAfter));
        commands.append("end");
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, commands.toString());
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataBefore,
                fT(DELETE_TEMPLATE,
                        "before", dataBefore,
                        "name", name));
    }

    private String updatePtpPorts(Config dataBefore, Config dataAfter) {
        List<PtpPort> ptpPortsBefore = dataBefore != null && dataBefore.getPtpPort() != null
                ? dataBefore.getPtpPort() : null;
        List<PtpPort> ptpPortsAfter = dataAfter != null && dataAfter.getPtpPort() != null
                ? dataAfter.getPtpPort() : null;

        if (!Objects.equals(ptpPortsAfter, ptpPortsBefore)) {
            StringBuilder commands = new StringBuilder();
            if (ptpPortsBefore != null) {
                for (PtpPort ptpPort : ptpPortsBefore) {
                    commands.append(fT(BEFORE_UPDATE_TEMPLATE,
                            "id", ptpPort.getId(),
                            "role", ptpPort.getRole(),
                            "local_priority", ptpPort.getLocalPriority(),
                            "master_clock_address", ptpPort.getMasterClockAddress(),
                            "enable", (ptpPort.isEnable() != null && ptpPort.isEnable())
                                    ? Chunk.TRUE : null));
                }
            }

            if (ptpPortsAfter != null) {
                for (PtpPort ptpPort : ptpPortsAfter) {
                    commands.append(fT(AFTER_UPDATE_TEMPLATE,
                            "id", ptpPort.getId(),
                            "role", ptpPort.getRole(),
                            "local_priority", ptpPort.getLocalPriority(),
                            "master_clock_address", ptpPort.getMasterClockAddress(),
                            "enable", (ptpPort.isEnable() != null && ptpPort.isEnable())
                                    ? Chunk.TRUE : null));
                }
            }
            return commands.toString();
        }
        return null;
    }
}