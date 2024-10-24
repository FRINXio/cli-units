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
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.bonding.group.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableUpstreamWriter implements CliWriter<Config> {
    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            cable upstream bonding-group {$config.id}
            {% loop in $upstreams as $upstream %} upstream {$upstream}
            {% endloop %}
            attributes {$config.attributes}
            end""";

    private static final String UPDATE_TEMPLATE_VLAN = """
            configure terminal
            interface {$ifc_name}
            no cable upstream bonding-group {$before.id}
            cable upstream bonding-group {$config.id}
            {% loop in $upstreams as $upstream %} upstream {$upstream}
            {% endloop %}
            attributes {$config.attributes}
            exit
            end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            no cable upstream bonding-group {$config.id}
            end""";

    private final Cli cli;

    public CableUpstreamWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String[] str = config.getUpstream().split(" ");
        List<String> upstreams = Arrays.asList(str);
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE, "before", null, "config", config, "ifc_name", ifcName,
                        "upstreams", upstreams));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String[] str = dataAfter.getUpstream().split(" ");
        List<String> upstreams = Arrays.asList(str);

        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE_VLAN, "before", dataBefore, "config", dataAfter, "ifc_name", ifcName,
                        "upstreams", upstreams));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "config", config,
                "ifc_name", ifcName));
    }
}