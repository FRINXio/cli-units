/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.system.handler.terminal;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.Terminal;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TerminalConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            {% if($data_after.last_ui_number) %}user-interface {$type} {$data_after.first_ui_number} {$data_after.last_ui_number}
            {% else %}user-interface {$type} {$data_after.first_ui_number}
            {% endif %}{% if($data_after.acl) %}acl {$data_after.acl.acl_id} {$data_after.acl.direction}
            {% else if($data_before.acl) %}undo acl {$data_before.acl.direction}
            {% endif %}{% if($data_after.auth_name) %}authentication-mode {$data_after.auth_name}
            {% else if($data_before.auth_name) %}undo authentication-mode
            {% endif %}{% if($data_after.privilege_level) %}user privilege level {$data_after.privilege_level}
            {% else if($data_before.privilege_level) %}undo user privilege level
            {% endif %}{% if($data_after.timeout_min) %}{% if($data_after.timeout_sec) %}idle-timeout {$data_after.timeout_min} {$data_after.timeout_sec}
            {% else %}idle-timeout {$data_after.timeout_min}
            {% endif %}{% else if($data_before.timeout_min) %}undo idle-timeout
            {% endif %}{% if($data_after.protocol_inbound) %}protocol inbound {$data_after.protocol_inbound}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo user-interface maximum-vty
            return""";

    private final Cli cli;

    public TerminalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String terminalType = id.firstKeyOf(Terminal.class).getType();
        blockingWriteAndRead(cli, id, config, fT(WRITE_UPDATE_TEMPLATE,
                "type", terminalType, "data_before", null, "data_after", config));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String terminalType = id.firstKeyOf(Terminal.class).getType();
        blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_UPDATE_TEMPLATE,
                "type", terminalType, "data_before", dataBefore, "data_after", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String terminalType = id.firstKeyOf(Terminal.class).getType();
        if (!Objects.equals(terminalType, "maximum-vty")) {
            throw new WriteFailedException.DeleteFailedException(id, new IllegalArgumentException(
                    "Unsupported terminal type for deleting: " + terminalType
                            + ". Type of terminal only which can be deleted: maximum-vty"));
        }
        blockingDeleteAndRead(cli, id, DELETE_TEMPLATE);
    }
}