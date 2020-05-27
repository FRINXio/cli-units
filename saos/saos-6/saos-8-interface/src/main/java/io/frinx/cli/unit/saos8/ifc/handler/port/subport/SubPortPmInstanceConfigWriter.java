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
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortPmInstanceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_PM_INSTANCE = "pm create sub-port {$subPortName} pm-instance {$data.name} "
            + "profile-type BasicTxRx bin-count {$data.bin_count}\n"
            + "configuration save";

    private static final String DELETE_PM_INSTANCE = "pm delete pm-instance {$data.name}\nconfiguration save";

    private final Cli cli;

    public SubPortPmInstanceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)) {
            final String subPortName = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Subinterface.class))
                    .get().getConfig().getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

            blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, subPortName));
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String subPortName) {
        return fT(WRITE_PM_INSTANCE, "data", config,
                "subPortName", subPortName);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)) {
            blockingWriteAndRead(cli, instanceIdentifier, config, deleteTemplate(config));
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_PM_INSTANCE, "data", config);
    }
}