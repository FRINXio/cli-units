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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.elements._class.element.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortVlanElementConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "sub-port add sub-port {$subIfcName} class-element {$data.id} vtag-stack {$data.vtag_stack}\n"
            + "configuration save";

    private static final String UPDATE_TEMPLATE =
            "{$data|update(vtag_stack,sub-port remove sub-port `$subIfcName` class-element `$data.id`\n,)}"
            + "{$data|update(vtag_stack,sub-port add sub-port `$subIfcName` class-element `$data.id` "
            + "vtag-stack `$data.vtag_stack`\n,)}"
            + "configuration save";

    private static final String DELETE_TEMPLATE =
            "sub-port remove sub-port {$subIfcName} class-element {$data.id}\n"
            + "configuration save";

    private Cli cli;

    public SubPortVlanElementConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext
                    .readAfter(instanceIdentifier.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final String subPortName = subPort.get().getConfig()
                        .getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

                blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, subPortName));
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String subIfcName) {
        return fT(WRITE_TEMPLATE, "data", config,
                    "subIfcName", subIfcName);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(id, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext.readAfter(id.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final String subPortName = subPort.get().getConfig()
                        .getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

                blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter, subPortName));
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter, String subIfcName) {
        return fT(UPDATE_TEMPLATE, "data", dataAfter, "before", dataBefore,
            "subIfcName", subIfcName);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext
                    .readBefore(instanceIdentifier.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final String subPortName = subPort.get().getConfig()
                        .getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

                blockingDelete(deleteTemplate(config, subPortName), cli, instanceIdentifier);
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String subIfcName) {
        return fT(DELETE_TEMPLATE, "data", config,
                "subIfcName", subIfcName);
    }
}
