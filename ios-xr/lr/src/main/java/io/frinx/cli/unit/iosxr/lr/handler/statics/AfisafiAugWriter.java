/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lr.handler.statics;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHops;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AfisafiAugWriter implements CliWriter<AfiSafiAug> {

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<AfiSafiAug> id,
                                       @NotNull AfiSafiAug data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.STATIC_ROUTING.canProcess(id, writeContext, false)) {
            return;
        }

        String vrfId = id.firstKeyOf(NetworkInstance.class).getName();
        Preconditions.checkArgument(vrfId.equals(NetworInstance.DEFAULT_NETWORK_NAME),
                "Static routing is only available for default network-instance now.");
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<AfiSafiAug> id,
                                        @NotNull AfiSafiAug dataBefore,
                                        @NotNull AfiSafiAug dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.STATIC_ROUTING.canProcess(id, writeContext, false)) {
            return;
        }

        Preconditions.checkArgument(Objects.equals(dataBefore.getAfiSafiType(), dataAfter.getAfiSafiType()),
                "Changing afi-safi type is not permitted. Before: %s, After: %s.",
                dataBefore.getAfiSafiType(), dataAfter.getAfiSafiType());
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<AfiSafiAug> id,
                                        @NotNull AfiSafiAug dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.STATIC_ROUTING.canProcess(id, writeContext, true)) {
            return;
        }

        String prefix = new String(id.firstKeyOf(Static.class).getPrefix().getValue());
        Preconditions.checkArgument(!hasNextHop(id, writeContext),
            "Cannot delete afi-safi type from a prefix with next-hop. prefix: %s.", prefix);
    }

    private static boolean hasNextHop(InstanceIdentifier<AfiSafiAug> id, WriteContext context) {

        Optional<Static> data = context.readAfter(RWUtils.cutId(id, Static.class));
        if (!data.isPresent()) {
            return false;
        }
        NextHops nextHops = data.get().getNextHops();
        if (nextHops == null) {
            return false;
        }
        if (nextHops.getNextHop() == null || nextHops.getNextHop().isEmpty()) {
            return false;
        }
        return true;
    }
}