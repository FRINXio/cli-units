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
package io.frinx.cli.unit.iosxe.system.handler.ntp;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.cisco.system.ntp.access.group.access.group.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class NtpAccessGroupConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            {% if ($config.peer) %}ntp access-group peer {$config.peer}
            {% endif %}{% if ($config.serve) %}ntp access-group serve {$config.serve}
            {% endif %}{% if ($config.serve_only) %}ntp access-group serve-only {$config.serve_only}
            {% endif %}{% if ($config.query_only) %}ntp access-group query-only {$config.query_only}
            {% endif %}end""";

    private static final String UPDATE_TEMPLATE = """
            configure terminal
            {% if ($config.peer) %}ntp access-group peer {$config.peer}
            {% else %}{% if ($before.peer) %}no ntp access-group peer
            {% endif %}{% endif %}{% if ($config.serve) %}ntp access-group serve {$config.serve}
            {% else %}{% if ($before.serve) %}no ntp access-group serve
            {% endif %}{% endif %}{% if ($config.serve_only) %}ntp access-group serve-only {$config.serve_only}
            {% else %}{% if ($before.serve_only) %}no ntp access-group serve-only
            {% endif %}{% endif %}{% if ($config.query_only) %}ntp access-group query-only {$config.query_only}
            {% else %}{% if ($before.query_only) %}no ntp access-group query-only
            {% endif %}{% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            {% if ($config.peer) %}no ntp access-group peer
            {% endif %}{% if ($config.serve) %}no ntp access-group serve
            {% endif %}{% if ($config.serve_only) %}no ntp access-group serve-only
            {% endif %}{% if ($config.query_only) %}no ntp access-group query-only
            {% endif %}end""";

    private final Cli cli;

    public NtpAccessGroupConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "config", config));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {

        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "before", dataBefore, "config", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "config", config));
    }
}