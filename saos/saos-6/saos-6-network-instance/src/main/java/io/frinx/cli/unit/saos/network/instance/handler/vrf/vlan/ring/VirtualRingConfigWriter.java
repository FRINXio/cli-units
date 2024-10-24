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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.ring;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.virtual.ring.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingConfigWriter implements CliWriter<Config> {

    private static final String WRITE_RING =
            "ring-protection virtual-ring add ring {$data.name} vid {$vlanId}";

    private static final String REMOVE_RING =
            "ring-protection virtual-ring remove ring {$data.name} vid {$vlanId}";

    private Cli cli;

    public VirtualRingConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId().getValue().toString();
        blockingWriteAndRead(fT(WRITE_RING, "data", config, "vlanId", vlanId),
                cli, instanceIdentifier, config);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId().getValue().toString();
        blockingDeleteAndRead(fT(REMOVE_RING, "data", config, "vlanId", vlanId),
                cli, instanceIdentifier);
    }
}