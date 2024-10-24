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
package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.upstream.cables.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$ifcName}
            upstream {$config.id} Upstream-Cable {$name} us-channel {$config.us_channel}
            end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            interface {$ifcName}
            no upstream {$config.id}
            end""";

    private static final String UPDATE_TEMPLATE = """
            configure terminal
            interface {$ifcName}
            no upstream {$before.id}
            upstream {$config.id} Upstream-Cable {$name} us-channel {$config.us_channel}
            end""";

    private final Cli cli;

    public CableInterfaceUpstreamConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String name = ParsingUtils.parseField(config.getName(), 0,
            Pattern.compile("Upstream-Cable(?<id>.+)")::matcher,
            matcher -> matcher.group("id")).get();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE, "before", null, "config", config, "ifcName", ifcName, "name", name));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String name = ParsingUtils.parseField(dataAfter.getName(), 0,
            Pattern.compile("Upstream-Cable(?<id>.+)")::matcher,
            matcher -> matcher.group("id")).get();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "before", dataBefore, "config", dataAfter, "ifcName", ifcName, "name", name));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "config", config,
                "ifcName", ifcName));
    }
}