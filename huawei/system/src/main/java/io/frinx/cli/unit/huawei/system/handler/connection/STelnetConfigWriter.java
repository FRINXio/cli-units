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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.stelnet.top.stelnet.server.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class STelnetConfigWriter  implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            {% if($enable == TRUE) %}stelnet server enable
            {% else %}undo stelnet server enable
            {% endif %}return""";

    private final Cli cli;

    public STelnetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean enable = config.isEnable();
        blockingWriteAndRead(cli, id, config, fT(WRITE_UPDATE_TEMPLATE, "enable", enable));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean enable = dataAfter.isEnable();
        blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_UPDATE_TEMPLATE, "enable", enable));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        throw new WriteFailedException.DeleteFailedException(id,
                new IllegalArgumentException("Deleting STelnet is not permitted"));
    }
}