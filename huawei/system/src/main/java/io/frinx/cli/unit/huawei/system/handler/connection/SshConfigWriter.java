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

package io.frinx.cli.unit.huawei.system.handler.connection;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.rev210923.system.ssh.server.top.ssh.server.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SshConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            {% if($config.timeout) %}ssh server timeout {$config.timeout}
            {% else %}undo ssh server timeout
            {% endif %}{% if($config.session_limit) %}ssh server authentication-retries {$config.session_limit}
            {% endif %}return""";

    private final Cli cli;

    public SshConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean enable = config.isEnable();
        blockingWriteAndRead(cli, id, config, fT(WRITE_UPDATE_TEMPLATE, "enable", enable, "config", config));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean enable = dataAfter.isEnable();
        blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_UPDATE_TEMPLATE, "enable", enable, "config", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        throw new WriteFailedException.DeleteFailedException(id,
                new IllegalArgumentException("Deleting SSH is not permitted"));
    }
}