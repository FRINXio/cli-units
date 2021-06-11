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

package io.frinx.cli.unit.iosxe.bgp.handler.peergroup;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.bgp.handler.BgpAfiSafiChecks;
import io.frinx.cli.unit.iosxe.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.iosxe.bgp.handler.neighbor.NeighborWriter;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.AfiSafis;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupWriter implements CliListWriter<PeerGroup, PeerGroupKey> {

    private static final String PEER_GROUP_ESSENTIAL_CONFIG = "neighbor {$neighbor_id} peer-group\n"
            + "{.if ($neighbor.config.peer_as.value) }neighbor {$neighbor_id} remote-as {$neighbor.config.peer_as"
            + ".value}\n{/if}";

    private static final String PEER_GROUP_DELETE = "no neighbor {$neighbor_id} peer-group\n";

    static final String PEER_GROUP_GLOBAL = "configure terminal\n"
            + "router bgp {$as}\n"
            + PEER_GROUP_ESSENTIAL_CONFIG
            + NeighborWriter.NEIGHBOR_COMMON_CONFIG
            + NeighborWriter.NEIGHBOR_TRANSPORT
            + NeighborWriter.NEIGHBOR_POLICIES
            +

            "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name}\n"
            + NeighborWriter.NEIGHBOR_SEND_COMMUNITY_CONFIG
            + NeighborWriter.NEIGHBOR_RR_CONFIG
            + NeighborWriter.NEIGHBOR_AFI_POLICIES
            + "exit\n"
            + "{% onEmpty %}"
            + NeighborWriter.NEIGHBOR_SEND_COMMUNITY_CONFIG
            + NeighborWriter.NEIGHBOR_RR_CONFIG
            + "{% endloop %}"
            + "end";

    static final String PEER_GROUP_VRF = "configure terminal\n"
            + "router bgp {$as}\n"
            + "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name} vrf {$vrf}\n"
            + PEER_GROUP_ESSENTIAL_CONFIG
            + NeighborWriter.NEIGHBOR_COMMON_CONFIG
            + NeighborWriter.NEIGHBOR_TRANSPORT
            + NeighborWriter.NEIGHBOR_SEND_COMMUNITY_CONFIG
            + NeighborWriter.NEIGHBOR_RR_CONFIG
            + NeighborWriter.NEIGHBOR_POLICIES
            + NeighborWriter.NEIGHBOR_AFI_POLICIES
            + "exit\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "end";

    static final String PEER_GROUP_GLOBAL_DELETE = "configure terminal\n"
            + "router bgp {$as}\n"
            + PEER_GROUP_DELETE
            + "end";

    static final String PEER_GROUP_VRF_DELETE = "configure terminal\n"
            + "router bgp {$as}\n"
            + "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name} vrf {$vrf}\n"
            + PEER_GROUP_DELETE
            + "exit\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "end";

    private Cli cli;

    public PeerGroupWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<PeerGroup> instanceIdentifier, PeerGroup neighbor,
                                              WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();

        final Global bgpGlobal = bgp.getGlobal();
        Long bgpAs = NeighborWriter.getAsValue(bgpGlobal);
        NeighborWriter.checkLocalAsAgainstRemoteAsWithinRoutReflector(neighbor, bgpAs, neighbor.getConfig());
        Map<String, Object> afiSafisForPeerGroup = getAfiSafisForPeerGroup(neighbor.getAfiSafis());
        if (!afiSafisForPeerGroup.isEmpty()) {
            BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);
        }
        Map<String, Object> groupAfiSafi = NeighborWriter.getAfiSafisForNeighbor(bgpGlobal, afiSafisForPeerGroup);
        String groupId = getPeerGroupId(instanceIdentifier);

        NeighborWriter.renderNeighbor(this, cli, instanceIdentifier,
                neighbor, null, null, null,
                vrfKey, bgpAs, groupAfiSafi, Collections.emptyMap(), groupId,
                null, null, null, null, null, null, null,
                PEER_GROUP_GLOBAL, PEER_GROUP_VRF);
    }

    static Map<String, Object> getAfiSafisForPeerGroup(AfiSafis afiSafis) {
        List<AfiSafi> configured = (afiSafis != null && afiSafis.getAfiSafi() != null) ? afiSafis.getAfiSafi() :
                Collections.emptyList();
        return configured.stream()
                .map(afi -> new AbstractMap.SimpleEntry<>(GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afi
                        .getAfiSafiName()), afi))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<PeerGroup> instanceIdentifier,
                                               PeerGroup before, PeerGroup neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();
        if (afiSafisHaveChanged(before, neighbor)) {
            BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);
        }

        final Global bgpGlobal = bgp.getGlobal();
        final Global bgpGlobalBefore = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Bgp.class))
                .get().getGlobal();
        Long bgpAs = NeighborWriter.getAsValue(bgpGlobal);
        NeighborWriter.checkLocalAsAgainstRemoteAsWithinRoutReflector(neighbor, bgpAs, neighbor.getConfig());

        Map<String, Object> groupAfiSafi = NeighborWriter.getAfiSafisForNeighbor(bgpGlobal,
                getAfiSafisForPeerGroup(neighbor.getAfiSafis()));
        Map<String, Object> groupAfiSafiBefore = NeighborWriter.getAfiSafisForNeighbor(bgpGlobalBefore,
                getAfiSafisForPeerGroup(before.getAfiSafis()));
        String groupId = getPeerGroupId(instanceIdentifier);

        NeighborWriter.renderNeighbor(this, cli, instanceIdentifier,
                neighbor, before, null, null,
                vrfKey, bgpAs, groupAfiSafi, groupAfiSafiBefore, groupId,
                null, null, null, null, null, null, null,
                PEER_GROUP_GLOBAL, PEER_GROUP_VRF);
    }

    private static boolean afiSafisHaveChanged(final PeerGroup before, final PeerGroup after) {
        final List<AfiSafi> afiSafisBefore = before.getAfiSafis() != null ? before.getAfiSafis().getAfiSafi() : null;
        final List<AfiSafi> afiSafisAfter = after.getAfiSafis() != null ? after.getAfiSafis().getAfiSafi() : null;
        if (afiSafisBefore == null && afiSafisAfter != null || afiSafisBefore != null && afiSafisAfter == null) {
            return true;
        } else if (afiSafisAfter == null) {
            return false;
        } else {
            return !(new HashSet<>(afiSafisAfter).equals(new HashSet<>(afiSafisBefore)));
        }
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<PeerGroup> instanceIdentifier, PeerGroup neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();

        final Global bgpGlobal = bgp.getGlobal();
        Long bgpAs = NeighborWriter.getAsValue(bgpGlobal);

        Map<String, Object> afiSafisForPeerGroup = getAfiSafisForPeerGroup(neighbor.getAfiSafis());
        if (!afiSafisForPeerGroup.isEmpty()) {
            BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);
        }
        Map<String, Object> groupAfiSafi = NeighborWriter.getAfiSafisForNeighbor(bgpGlobal, afiSafisForPeerGroup);
        String groupId = getPeerGroupId(instanceIdentifier);

        NeighborWriter.deleteNeighbor(this, cli, instanceIdentifier, neighbor,
                vrfKey, bgpAs, groupAfiSafi, groupId,
                null, null, null, null,
                PEER_GROUP_GLOBAL_DELETE, PEER_GROUP_VRF_DELETE);
    }

    static String getPeerGroupId(InstanceIdentifier<?> group) {
        return group.firstKeyOf(PeerGroup.class).getPeerGroupName();
    }

}
