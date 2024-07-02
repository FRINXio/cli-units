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

package io.frinx.cli.unit.ios.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.View;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ViewWriter implements CliWriter<View> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure terminal
            {% if ($config.mib) %}{% loop in $config.mib as $mib %}snmp-server view {$name} {$mib.name} {$mib.inclusion.name}
            {% endloop %}{% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            no snmp-server view {$name}
            end""";

    private final Cli cli;

    public ViewWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<View> instanceIdentifier,
                                       @NotNull View view,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, view,
                fT(WRITE_TEMPLATE,
                        "name", view.getName(),
                        "config", view.getConfig()));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<View> id,
                                        @NotNull View dataBefore,
                                        @NotNull View dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<View> instanceIdentifier,
                                        @NotNull View view,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, view,
                fT(DELETE_TEMPLATE, "name", view.getName()));
    }
}