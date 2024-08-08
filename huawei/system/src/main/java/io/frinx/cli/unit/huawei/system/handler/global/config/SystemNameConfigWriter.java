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

package io.frinx.cli.unit.huawei.system.handler.global.config;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.name.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SystemNameConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            sysname {% if($data.system_name) %}{$data.system_name}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo sysname
            return""";

    private final Cli thisCli;

    public SystemNameConfigWriter(Cli cli) {
        this.thisCli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(thisCli, id, config, fT(WRITE_UPDATE_TEMPLATE, "data", config));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(thisCli, id, dataAfter, fT(WRITE_UPDATE_TEMPLATE, "data", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(thisCli, id, DELETE_TEMPLATE);
    }
}