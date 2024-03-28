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
package io.frinx.cli.unit.iosxe.cable.handler.fiber.node;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class FiberNodeWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            cable fiber-node {$config.id}
            description {$config.description}
            end""";

    private static final String UPDATE_TEMPLATE_VLAN = """
            configure terminal
            cable fiber-node {$before.id}
            no description
            {% if ($config.description) %}{% if ($config.description != '') %}description {$config.description}
            {% endif %}{% endif %}
            exit
            end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            cable fiber-node {$config.id}
            no description
            end""";

    private final Cli cli;

    public FiberNodeWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE, "before", null, "config", config));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {

        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE_VLAN, "before", dataBefore, "config", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "config", config));
    }
}