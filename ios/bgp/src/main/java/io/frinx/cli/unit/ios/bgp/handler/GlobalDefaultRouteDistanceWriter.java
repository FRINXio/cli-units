/*
 * Copyright © 2024 Frinx and others.
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

package io.frinx.cli.unit.ios.bgp.handler;

import static io.frinx.cli.unit.ios.bgp.handler.GlobalConfigWriter.getAfiSafis;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.DefaultRouteDistance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalDefaultRouteDistanceWriter implements CliWriter<DefaultRouteDistance> {

    private Cli cli;

    public GlobalDefaultRouteDistanceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<DefaultRouteDistance> id,
                                       @NotNull DefaultRouteDistance config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {

        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        Preconditions.checkState(writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get()
                        .getGlobal().getConfig().getAs() != null, "As is missing. Accessing BGP router not allowed.");
        var as = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get().getGlobal().getConfig().getAs().getValue();

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingWriteAndRead(cli, id, config,
                    "configure terminal",
                    f("router bgp %s", as),
                    f("distance bgp %s %s 200", config.getConfig().getExternalRouteDistance(),
                            config.getConfig().getInternalRouteDistance()),
                    "end");
        } else {
            Set<AfiSafi> allAfiSafis = getAfiSafis(writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).orElse(null));
            for (AfiSafi afiSafi : allAfiSafis) {
                blockingWriteAndRead(cli, id, config,
                        "configure terminal",
                        f("router bgp %s", as),
                        f("address-family %s vrf %s", toDeviceAddressFamily(afiSafi.getAfiSafiName()),
                                vrfKey.getName()),
                        f("distance bgp %s %s 200", config.getConfig().getExternalRouteDistance(),
                                config.getConfig().getInternalRouteDistance()),
                        "end");
            }
        }
    }

    private static String toDeviceAddressFamily(Class<? extends AFISAFITYPE> afiSafiName) {
        if (afiSafiName.equals(IPV4UNICAST.class)) {
            return "ipv4";
        }
        throw new IllegalStateException("Administrative distance is not supported with afi safi type: " + afiSafiName);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<DefaultRouteDistance> id,
                                        @NotNull DefaultRouteDistance dataBefore,
                                        @NotNull DefaultRouteDistance dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // Just perform write, delete not necessary (bgp router is global and encapsulates configuration for all vrfs)
        // cannot just delete and replace
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override public void deleteCurrentAttributes(@NotNull InstanceIdentifier<DefaultRouteDistance> id,
                                                  @NotNull DefaultRouteDistance config,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {

        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        Preconditions.checkArgument(writeContext.readBefore(RWUtils.cutId(id, Bgp.class)).get()
                        .getGlobal().getConfig().getAs() != null,
                "As is missing. Accessing BGP router not allowed.");
        var as = writeContext.readBefore(RWUtils.cutId(id, Bgp.class)).get().getGlobal().getConfig().getAs().getValue();

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingDeleteAndRead(cli, id,
                    "configure terminal",
                    f("router bgp %s", as),
                    f("no distance bgp"),
                    "end");
        } else {
            Set<AfiSafi> allAfiSafis = getAfiSafis(writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).orElse(null));
            for (AfiSafi afiSafi : allAfiSafis) {
                blockingWriteAndRead(cli, id, config,
                        "configure terminal",
                        f("router bgp %s", as),
                        f("address-family %s vrf %s", toDeviceAddressFamily(afiSafi.getAfiSafiName()),
                                vrfKey.getName()),
                        "no distance bgp",
                        "end");
            }
        }
    }
}