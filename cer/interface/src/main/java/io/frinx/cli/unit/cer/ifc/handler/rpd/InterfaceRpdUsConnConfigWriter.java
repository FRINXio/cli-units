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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.us.conn.top.rpd.us.conn.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdUsConnConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure interface {$name}
            {% loop in $config.us_conn as $us_conn %}us-conn {$us_conn.id} base-target-power {$us_conn.base_target_power}
            {% onEmpty %}{% endloop %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String UPDATE_TEMPLATE = """
            configure interface {$name}
            {% loop in $before.us_conn as $us_conn %}{% if($us_conn.base_target_power) %}us-conn {$us_conn.id} no base-target-power {$us_conn.base_target_power}
            {% endif %}{% onEmpty %}{% endloop %}{% loop in $config.us_conn as $us_conn %}us-conn {$us_conn.id} base-target-power {$us_conn.base_target_power}
            {% onEmpty %}{% endloop %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String DELETE_TEMPLATE = """
            configure interface {$name}
            {% loop in $before.us_conn as $us_conn %}{% if($us_conn.base_target_power) %}us-conn {$us_conn.id} no base-target-power {$us_conn.base_target_power}
            {% endif %}{% onEmpty %}{% endloop %}end""";

    private final Cli cli;

    public InterfaceRpdUsConnConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(WRITE_TEMPLATE,
                        "config", dataAfter,
                        "name", name));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore, @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE,
                        "before", dataBefore,
                        "config", dataAfter,
                        "name", name));
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
}