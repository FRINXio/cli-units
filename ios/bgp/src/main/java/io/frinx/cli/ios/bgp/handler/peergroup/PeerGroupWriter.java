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

package io.frinx.cli.ios.bgp.handler.peergroup;

import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_AFI_POLICIES;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_COMMON_CONFIG;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_POLICIES;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_RR_CONFIG;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_SEND_COMMUNITY_CONFIG;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_TRANSPORT;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.deleteNeighbor;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.getAfiSafisForNeighbor;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.getAsValue;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.getGlobalBgp;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.getGlobalBgpForDelete;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.renderNeighbor;
import static java.util.stream.Collectors.toMap;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpListWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.GlobalAfiSafiConfigWriter;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.AfiSafis;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupWriter implements BgpListWriter<PeerGroup, PeerGroupKey> {

    private static final String PEER_GROUP_ESSENTIAL_CONFIG =
            "neighbor {$neighbor_id} peer-group\n" +
            "{.if ($neighbor.config.peer_as.value) }neighbor {$neighbor_id} remote-as {$neighbor.config.peer_as.value}\n{/if}";

    private static final String PEER_GROUP_DELETE = "no neighbor {$neighbor_id} peer-group\n";

    static final String PEER_GROUP_GLOBAL = "configure terminal\n" +
            "router bgp {$as}\n" +
            PEER_GROUP_ESSENTIAL_CONFIG +
            NEIGHBOR_COMMON_CONFIG +
            NEIGHBOR_TRANSPORT +
            NEIGHBOR_POLICIES +

            "{% loop in $afis as $af_name:af %}\n" +
            "address-family {$af_name}\n" +
            NEIGHBOR_SEND_COMMUNITY_CONFIG +
            NEIGHBOR_RR_CONFIG +
            NEIGHBOR_AFI_POLICIES +
            "exit\n" +
            "{% onEmpty %}" +
            NEIGHBOR_SEND_COMMUNITY_CONFIG +
            NEIGHBOR_RR_CONFIG +
            "{% endloop %}" +
            "end";

    static final String PEER_GROUP_VRF = "configure terminal\n" +
            "router bgp {$as}\n" +
            "{% loop in $afis as $af_name:af %}\n" +
            "address-family {$af_name} vrf {$vrf}\n" +
            PEER_GROUP_ESSENTIAL_CONFIG +
            NEIGHBOR_COMMON_CONFIG +
            NEIGHBOR_TRANSPORT +
            NEIGHBOR_SEND_COMMUNITY_CONFIG +
            NEIGHBOR_RR_CONFIG +
            NEIGHBOR_POLICIES +
            NEIGHBOR_AFI_POLICIES +
            "exit\n" +
            "{% onEmpty %}" +
            "{% endloop %}" +
            "end";

    static final String PEER_GROUP_GLOBAL_DELETE = "configure terminal\n" +
            "router bgp {$as}\n" +
            PEER_GROUP_DELETE +
            "end";

    static final String PEER_GROUP_VRF_DELETE = "configure terminal\n" +
            "router bgp {$as}\n" +
            "{% loop in $afis as $af_name:af %}\n" +
            "address-family {$af_name} vrf {$vrf}\n" +
            PEER_GROUP_DELETE +
            "exit\n" +
            "{% onEmpty %}" +
            "{% endloop %}" +
            "end";

    private Cli cli;

    public PeerGroupWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<PeerGroup> instanceIdentifier, PeerGroup neighbor,
                                              WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        final Global bgpGlobal = getGlobalBgp(instanceIdentifier, writeContext);
        Long bgpAs = getAsValue(bgpGlobal);
        Map<String, Object> groupAfiSafi = getAfiSafisForNeighbor(bgpGlobal, getAfiSafisForPeerGroup(neighbor.getAfiSafis()));
        String groupId = getPeerGroupId(instanceIdentifier);

        renderNeighbor(this, cli, instanceIdentifier,
                neighbor, null, null, null, vrfKey, bgpAs, groupAfiSafi, Collections.emptyMap(), groupId,
                PEER_GROUP_GLOBAL, PEER_GROUP_VRF);
    }

    static Map<String, Object> getAfiSafisForPeerGroup(AfiSafis afiSafis) {
        List<AfiSafi> configured = (afiSafis != null && afiSafis.getAfiSafi() != null) ? afiSafis.getAfiSafi() : Collections.emptyList();
        return configured.stream()
                .map(afi -> new AbstractMap.SimpleEntry<>(GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afi.getAfiSafiName()), afi))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<PeerGroup> instanceIdentifier,
                                               PeerGroup before, PeerGroup neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        final Global bgpGlobal = getGlobalBgp(instanceIdentifier, writeContext);
        final Global bgpGlobalBefore = getGlobalBgpForDelete(instanceIdentifier, writeContext);
        Long bgpAs = getAsValue(bgpGlobal);

        Map<String, Object> groupAfiSafi = getAfiSafisForNeighbor(bgpGlobal, getAfiSafisForPeerGroup(neighbor.getAfiSafis()));
        Map<String, Object> groupAfiSafiBefore = getAfiSafisForNeighbor(bgpGlobalBefore, getAfiSafisForPeerGroup(before.getAfiSafis()));
        String groupId = getPeerGroupId(instanceIdentifier);

        renderNeighbor(this, cli, instanceIdentifier,
                neighbor, before, null, null, vrfKey, bgpAs, groupAfiSafi, groupAfiSafiBefore, groupId,
                PEER_GROUP_GLOBAL, PEER_GROUP_VRF);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<PeerGroup> instanceIdentifier, PeerGroup neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        final Global bgpGlobal = getGlobalBgpForDelete(instanceIdentifier, writeContext);
        Long bgpAs = getAsValue(bgpGlobal);

        Map<String, Object> groupAfiSafi = getAfiSafisForNeighbor(bgpGlobal, getAfiSafisForPeerGroup(neighbor.getAfiSafis()));
        String groupId = getPeerGroupId(instanceIdentifier);

        deleteNeighbor(this, cli, instanceIdentifier, neighbor, vrfKey, bgpAs, groupAfiSafi, groupId,
                PEER_GROUP_GLOBAL_DELETE, PEER_GROUP_VRF_DELETE);
    }

    static String getPeerGroupId(InstanceIdentifier<?> group) {
        return group.firstKeyOf(PeerGroup.class).getPeerGroupName();
    }

}
