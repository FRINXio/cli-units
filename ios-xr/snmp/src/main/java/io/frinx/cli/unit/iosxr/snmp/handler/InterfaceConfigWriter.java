/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {
    /*  XR6 has a timing problem for inserting multiple Snmp commands in one string unit
        It throws "Invalid input detected"
        Added "?" command as a workaround to make gap between commands  */
    static final String SNMP_IFC_TEMPLATE =
            """
                    snmp-server interface {$config.interface_id.value}
                    {% if ($enable_linkup) %}?
                    no {% endif %}notification linkupdown disable
                    root""";

    static final String SNMP_IFC_TEMPLATE_DELETE = "no snmp-server interface {$config.interface_id.value}";

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config
            config, @NotNull WriteContext writeContext) throws WriteFailedException {
        if (config.getEnabledTrapForEvent() == null
                ||
                config.getEnabledTrapForEvent()
                        .isEmpty()
                ||
                !LINKUPDOWN.class.equals(config.getEnabledTrapForEvent()
                        .get(0)
                        .getEventName())) {
            return;
        }

        blockingWriteAndRead(cli, instanceIdentifier, config, fT(SNMP_IFC_TEMPLATE,
                "config", config,
                "enable_linkup", config.getEnabledTrapForEvent()
                        .get(0)
                        .isEnabled() ? true : null));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore, @NotNull
            Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config
            config, @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(SNMP_IFC_TEMPLATE_DELETE,
                "config", config));
    }
}