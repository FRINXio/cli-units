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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.Profile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2CftProfileProtocolConfigWriter implements CliWriter<Config> {

    private static final String WRITE_PROTOCOL =
            "l2-cft protocol add profile {$profileName} ctrl-protocol {$data.name.name} "
            + "untagged-disposition {$data.disposition.name}";

    private static final String UPDATE_PROTOCOL =
            "l2-cft protocol set profile {$profileName} ctrl-protocol {$data.name.name}"
            + " untagged-disposition {$data.disposition.name}";

    private static final String DELETE_PROTOCOL =
            "l2-cft protocol remove profile {$profileName} ctrl-protocol {$data.name.name}";

    private Cli cli;

    public L2CftProfileProtocolConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String profileName = instanceIdentifier.firstKeyOf(Profile.class).getName();
        blockingWriteAndRead(fT(WRITE_PROTOCOL, "data", config, "profileName", profileName),
                cli, instanceIdentifier, config);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!dataBefore.getDisposition().getName().equals(dataAfter.getDisposition().getName())) {
            String profileName = id.firstKeyOf(Profile.class).getName();
            blockingWriteAndRead(fT(UPDATE_PROTOCOL, "data", dataAfter, "before", dataBefore,
                    "profileName", profileName), cli, id, dataAfter);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String profileName = instanceIdentifier.firstKeyOf(Profile.class).getName();
        blockingDeleteAndRead(fT(DELETE_PROTOCOL, "data", config, "profileName", profileName),
                cli, instanceIdentifier);
    }
}