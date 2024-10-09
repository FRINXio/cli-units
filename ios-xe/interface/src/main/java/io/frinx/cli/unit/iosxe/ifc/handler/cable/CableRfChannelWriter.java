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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.RfChannels;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class CableRfChannelWriter implements CliWriter<RfChannels> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            cable rf-channels channel-list {$config.channel_list} bandwidth-percent {$config.bandwidth_percent}
            end""";

    private static final String UPDATE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            no cable rf-channels channel-list {$before.channel_list}
            cable rf-channels channel-list {$config.channel_list} bandwidth-percent {$config.bandwidth_percent}
            exit
            end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            no cable rf-channels channel-list {$config.channel_list}
            end""";

    private final Cli cli;


    public CableRfChannelWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<RfChannels> instanceIdentifier,
                                       @NotNull RfChannels config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE, "before", null, "config", config, "ifc_name", ifcName));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<RfChannels> instanceIdentifier,
                                        @NotNull RfChannels dataBefore,
                                        @NotNull RfChannels dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "before", dataBefore, "config", dataAfter, "ifc_name", ifcName));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<RfChannels> instanceIdentifier,
                                        @NotNull RfChannels config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE, "config", config, "ifc_name", ifcName));
    }
}