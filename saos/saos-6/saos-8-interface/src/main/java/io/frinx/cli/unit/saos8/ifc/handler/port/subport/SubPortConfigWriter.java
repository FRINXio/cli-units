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
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortConfigWriter implements CliWriter<Config> {

    private static final String WRITE_SUBPORT =
            "sub-port create sub-port {$subPort} parent-port {$parentPort} classifier-precedence {$data.index}";

    private static final String UPDATE_SUBPORT = "sub-port set sub-port {$nameBefore} name {$nameAfter}";

    private static final String DELETE_SUBPORT = "sub-port delete sub-port {$subPort}";

    private Cli cli;

    public SubPortConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ethernetCheck.canProcess(instanceIdentifier, writeContext, false)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, parentPort));
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String parentPort) {
        return fT(WRITE_SUBPORT, "data", config,
                "subPort", config.getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName(),
                "parentPort", parentPort);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(id, writeContext, false)) {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
        }
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter) {
        String nameBefore = dataBefore.getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();
        String nameAfter = dataAfter.getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();
        if (!nameBefore.equals(nameAfter)) {
            return fT(UPDATE_SUBPORT, "data", dataAfter, "before", dataBefore,
                    "nameBefore", nameBefore, "nameAfter", nameAfter);
        }
        return null;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ethernetCheck.canProcess(instanceIdentifier, writeContext, false)) {
            blockingDelete(deleteTemplate(config), cli, instanceIdentifier);
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_SUBPORT, "data", config,
                "subPort", config.getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName());
    }
}
