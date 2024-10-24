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
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.banner.Config;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class BannerConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            header login information "
            {% if($data.banner_text) %}{$data.banner_text}{% endif %}"
            return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo header login
            return""";

    private final Cli thisCli;

    public BannerConfigWriter(Cli cli) {
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

    @Override
    public String blockingWriteAndRead(@NotNull final String command,
                                       @NotNull final Cli cli,
                                       @NotNull final Path<?> id,
                                       @NotNull final Object data) throws WriteFailedException.CreateFailedException {
        try {
            LOG.debug("{}: Writing: {}, {} by executing command: '{}'", cli, id, data, command);
            String output = cli.executeAndRead(Command.writeCommandWithWhitespacesAtStart(command))
                    .toCompletableFuture().get();
            LOG.debug("{}: Result of writing: {}, {} is: {}", cli, id, data, output);
            return output;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        } catch (ExecutionException e) {
            LOG.warn("{}: Unable to write: {}, {} by executing: {}", cli, id, data, command, e);
            throw new WriteFailedException.CreateFailedException(id, data, e);
        }
    }
}