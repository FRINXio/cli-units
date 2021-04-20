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

package io.frinx.cli.unit.iosxe.evc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.evc.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class EvcConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = "configure terminal\n"
            + "ethernet evc {$evc_name}\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no ethernet evc {$evc_name}\n"
            + "end";

    private final Cli cli;

    public EvcConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_UPDATE_TEMPLATE, "evc_name", config.getName()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, dataAfter,
                fT(WRITE_UPDATE_TEMPLATE,
                        "before", dataBefore,
                        "data", dataAfter,
                        "evc_name", dataAfter.getName()));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "evc_name", config.getName()));
    }
}