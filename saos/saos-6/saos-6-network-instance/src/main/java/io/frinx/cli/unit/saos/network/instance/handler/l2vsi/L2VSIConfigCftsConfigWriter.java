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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigCftsConfigWriter implements CliWriter<Config> {

    public L2VSIConfigCftsConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private Cli cli;

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (config != null && config.getMode().equals("mef-ce2")) {
            blockingWriteAndRead("l2-cft set mode mef-ce2\n",
                    cli, instanceIdentifier, config);
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (dataBefore.getMode().equals("mef-ce2")) {
            deleteCurrentAttributes(id, dataAfter, writeContext);
        } else if (dataBefore.getMode().equals("mef-ce1")) {
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead("l2-cft set mode mef-ce1\n", cli, instanceIdentifier);
    }
}
