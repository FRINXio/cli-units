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

package io.frinx.cli.unit.ios.privilege.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.Levels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.Level;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.top.Privilege;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrivilegeWriter implements CliWriter<Privilege> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure terminal
            {% if ($levels.level) %}{% loop in $levels.level as $level %}{% if ($level.config.id) %}{% if ($level.config.commands) %}{% loop in $level.config.commands as $command %}privilege exec level {$level.config.id.value} {$command}
            {% endloop %}{% endif %}{% endif %}{% endloop %}{% endif %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String DELETE_TEMPLATE = """
            configure terminal
            {% if ($levels.level) %}{% loop in $levels.level as $level %}{% if ($level.config.commands) %}{% loop in $level.config.commands as $command %}privilege exec reset {$command}
            {% endloop %}{% endif %}{% endloop %}{% endif %}end""";

    private final Cli cli;

    public PrivilegeWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Privilege> instanceIdentifier,
                                       @NotNull Privilege privilege,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        checkCommands(privilege.getLevels());
        blockingWriteAndRead(cli, instanceIdentifier, privilege,
                fT(WRITE_TEMPLATE, "levels", privilege.getLevels()));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Privilege> id,
                                        @NotNull Privilege dataBefore,
                                        @NotNull Privilege dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        checkCommands(dataAfter.getLevels());
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Privilege> instanceIdentifier,
                                        @NotNull Privilege privilege,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, privilege,
                fT(DELETE_TEMPLATE, "levels", privilege.getLevels()));
    }

    public void checkCommands(final Levels levels) {
        final List<String> commandsWithoutParent = getCommandsWithoutParent(levels);
        if (commandsWithoutParent.size() > 0) {
            final String commands = StringUtils.join(commandsWithoutParent, ", ");
            throw new IllegalStateException(f("No subcommands found for these privilege commands: %s", commands));
        }
    }

    public static List<String> getCommandsWithoutParent(final Levels levels) {
        // for example command "show running-config" also needs command "show"
        // on device it adds command "show" automatically but here it does not
        // to prevent this, we check for this inconsistency
        final List<String> commandsWithoutParent = new ArrayList<>();
        if (levels != null && levels.getLevel() != null) {
            for (final Level level : levels.getLevel()) {
                if (level.getConfig() != null && level.getConfig().getCommands() != null) {
                    for (final String command : level.getConfig().getCommands()) {
                        if (command.split(" ").length > 1) {
                            int index = command.lastIndexOf(' ');
                            final String subcommand = command.substring(0, index);
                            if (!listContainsSubcommand(level.getConfig().getCommands(), subcommand)) {
                                commandsWithoutParent.add(command);
                            }
                        }
                    }
                }
            }
        }
        return commandsWithoutParent;
    }

    private static boolean listContainsSubcommand(final List<String> commands, final String subcommand) {
        for (final String command : commands) {
            if (subcommand.equalsIgnoreCase(command)) {
                return true;
            }
        }
        return false;
    }
}