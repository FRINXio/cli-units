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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.virtual.ring.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingConfigWriter implements CliWriter<Config> {

    private static final String WRITE_RING = "ring-protection virtual-ring add ring {$data.name} vs {$vsName}";

    private static final String REMOVE_RING = "ring-protection virtual-ring remove ring {$data.name} vs {$vsName}";

    private Cli cli;

    public VirtualRingConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        blockingWriteAndRead(fT(WRITE_RING, "data", config, "vsName", vsName),
                cli, instanceIdentifier, config);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(fT(REMOVE_RING, "data", config, "vsName", vsName),
                cli, instanceIdentifier);
    }
}
