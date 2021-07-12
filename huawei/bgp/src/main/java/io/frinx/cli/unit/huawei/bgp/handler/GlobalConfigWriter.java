/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.huawei.bgp.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpGlobalBase;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpNeighborAfiSafiList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpNeighborBase;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Neighbors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_UPDATE_TEMPLATE = "system-view\n"
            + "bgp {$bgp}\n"
            + "{$routerId}\n"
            + "{% loop in $importRoute as $im_r %}\n"
            + "{$im_r}\n"
            + "{% onEmpty %}"
            + "undo import-route static\n"
            + "undo import-route direct\n"
            + "{% endloop %}"
            + "commit\n"
            + "return";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        ProtocolKey protoKey = id.firstKeyOf(Protocol.class);
        Preconditions.checkArgument(protoKey.getName().equals(BgpProtocolReader.DEFAULT_BGP_INSTANCE),
                "BGP protocol instance has to be named: %s. Not: %s", BgpProtocolReader.DEFAULT_BGP_INSTANCE, protoKey);
        List<String> importRouteList = getImportRouteList(config.getAugmentation(BgpGlobalConfigAug.class));
        String routerId = getRouterId(config);
        if (!vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            // Compare AS for global and current VRF. Must match for IOS
            writeContext.readAfter(IIDs.NETWORKINSTANCES.child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                    .child(Protocols.class))
                    .get()
                    .getProtocol().stream()
                    .filter(protocol -> protocol.getConfig().getIdentifier().equals(BGP.class))
                    .findFirst()
                    .map(bgp -> bgp.getBgp().getGlobal().getConfig().getAs())
                    .ifPresent(globalAs -> Preconditions.checkArgument(globalAs.equals(config.getAs()),
                            "BGP for VRF contains different AS: %s than global BGP: %s", config.getAs(), globalAs));
        }
        blockingWriteAndRead(cli, id, config,
                fT(WRITE_UPDATE_TEMPLATE, "bgp", config.getAs().getValue(), "routerId", routerId,
                "importRoute", importRouteList));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        // Just perform write, delete not necessary (bgp router is global and encapsulates configuration for all vrfs)
        // cannot just delete and replace
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config config,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {

        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        // Only delete BGP global configuration for default network. Since removing an l3vpn configuration (VRF) would
        // delete global BGP
        // TODO add a check, if this is the last VRF to have BGP configured (also not in default), delete
        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingDeleteAndRead(cli, id,
                    "system-view",
                    f("undo bgp %s", config.getAs().getValue()),
                    "commit",
                    "return");
        }
    }

    private List<String> getImportRouteList(BgpGlobalConfigAug configAug) {
        List<String> importRouteList = new ArrayList<>();
        if (configAug != null && configAug.getImportRoute() != null) {
            if (configAug.getImportRoute().size() != 2) {
                if (configAug.getImportRoute().contains("direct")) {
                    importRouteList.add("import-route direct");
                    importRouteList.add("undo import-route static");
                }
                else {
                    importRouteList.add("import-route static");
                    importRouteList.add("undo import-route direct");
                }
            }
            else {
                importRouteList.add("import-route static");
                importRouteList.add("import-route direct");
            }
        }
        return importRouteList;
    }

    private String getRouterId(Config config) {
        String routerId;
        if (config.getRouterId() == null) {
            routerId = "undo router-id";
        } else {
            routerId = "router-id " + config.getRouterId().getValue();
        }
        return routerId;
    }

    /**
     * Collect all afi safi referenced in this instance.
     */
    public static Set<AfiSafi> getAfiSafis(@Nullable Bgp bgp) {
        List<AfiSafi> globalAfiSafi = Optional.ofNullable(bgp)
                .map(Bgp::getGlobal)
                .map(BgpGlobalBase::getAfiSafis)
                .orElse(new AfiSafisBuilder().setAfiSafi(Collections.emptyList()).build())
                .getAfiSafi();

        Set<AfiSafi> afiSafi = new HashSet<>(globalAfiSafi);

        // Add all neighbor specific families, so that the network can be applied to all families
        afiSafi.addAll(getAfiSafisForNeighbors(bgp));

        return afiSafi;
    }

    public static Collection<? extends AfiSafi> getAfiSafisForNeighbors(@Nullable Bgp bgpGlobal) {
        List<Neighbor> neighbors = Optional.ofNullable(bgpGlobal)
                .map(Bgp::getNeighbors)
                .map(Neighbors::getNeighbor)
                .orElse(Collections.emptyList());

        return neighbors.stream()
                .map(BgpNeighborBase::getAfiSafis)
                .filter(Objects::nonNull)
                .map(BgpNeighborAfiSafiList::getAfiSafi)
                .flatMap(a -> (a == null ? Collections.<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                        .bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi>emptyList() : a).stream())
                .map(a -> new AfiSafiBuilder().setAfiSafiName(a.getAfiSafiName()).build())
                .collect(Collectors.toSet());
    }
}
