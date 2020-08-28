/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.ifc.handler.ethernet;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String SPEED_TEMPLATE = "speed"
            + "{% if ($port_speed) %} {$port_speed}"
            + "{% else %} auto"
            + "{% endif %}\n";

    private static final String IFC_ETHERNET_CONFIG_TEMPLATE = "configure terminal\n"
            + "interface {$ifc_name}\n"
            + SPEED_TEMPLATE
            + "end";

    private static final String IFC_ETHERNET_CONFIG_DELETE_TEMPLATE = "configure terminal\n"
            + "interface {$ifc_name}\n"
            + "no speed\n"
            + "end";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();
        blockingWriteAndRead(cli, id, dataAfter,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "port_speed", Util.getSpeedName(dataAfter.getPortSpeed())));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();
        blockingDeleteAndRead(cli, id,
                fT(IFC_ETHERNET_CONFIG_DELETE_TEMPLATE,
                        "ifc_name", ifcName));
    }

}
