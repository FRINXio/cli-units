/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpListWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborWriter implements BgpListWriter<Neighbor, NeighborKey> {

    private static final String NEIGHBOR_POLICIES =
            // Set import/export policies
            "{% loop in $neighbor.apply_policy.config.import_policy as $im_p %}\n" +
                    "peer {$neighbor_ip} route-policy {$im_p} import\n" +
                    "{% onEmpty %}" +
                    "{% endloop %}" +
                    "{% loop in $neighbor.apply_policy.config.export_policy as $ex_p %}\n" +
                    "peer {$neighbor_ip} route-policy {$ex_p} export\n" +
                    "{% onEmpty %}" +
                    "{% endloop %}";

    static final String NEIGHBOR_GLOBAL = "system-view\n" +
            "bgp {$as}\n" +
            "peer {$neighbor_ip} as-remote {$neighbor.config.peer_as.value}\n" +

            // //Active the neighbor. Either under address family, or globally if no family present
            "{.if ($afi_safi) }ipv4-family {$afi_safi}\n{/if}" +

            //Set policies
            NEIGHBOR_POLICIES +

            "peer {$neighbor_ip} enable\n" +

            "{.if ($afi_safi) }quit\n{/if}" +

            "commit\n" +
            "return";

    static final String NEIGHBOR_GLOBAL_DELETE = "system-view\n" +
            "bgp {$as}\n" +

            "{.if ($afi_safi) }ipv4-family {$afi_safi}\n{/if}" +
            "undo neighbor {$neighbor_ip} enable\n" +
            "{.if ($afi_safi) }quit\n{/if}" +

            "undo peer {$neighbor_ip} as-remote {$neighbor.config.peer_as.value}\n" +

            "commit\n" +
            "return";

    static final String NEIGHBOR_VRF = "system-view\n" +
            "bgp {$as}\n" +

            // //Active the neighbor. Either under address family, or globally if no family present
            "ipv4-family vpn-instance {$vrf}\n" +

            "peer {$neighbor_ip} as-remote {$neighbor.config.peer_as.value}\n" +

            //Set policies
            NEIGHBOR_POLICIES +

            "peer {$neighbor_ip} enable\n" +

            "commit\n" +
            "return";

    static final String NEIGHBOR_VRF_DELETE = "system-view\n" +
            "bgp {$as}\n" +

            "ipv4-family vpn-instance {$vrf}\n" +
            "undo peer {$neighbor_ip} enable\n" +
            "undo peer {$neighbor_ip} as-remote {$neighbor.config.peer_as.value}\n" +

            "commit\n" +
            "return";

    private Cli cli;

    public NeighborWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Neighbor> instanceIdentifier, Neighbor neighbor,
                                              WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        final Global bgpGlobal = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get().getGlobal();
        Long bgpAs = getAsValue(bgpGlobal);

        List<Class<? extends AFISAFITYPE>> neighAfiSafi = getAfiSafisForNeighbor(bgpGlobal, neighbor);
        String neighborIp = getNeighborIp(instanceIdentifier);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            if (neighAfiSafi.isEmpty()) {
                renderNeighbor(NEIGHBOR_GLOBAL, instanceIdentifier, neighbor,
                        "as", bgpAs,
                        "neighbor_ip", neighborIp,
                        "neighbor", neighbor);
            } else {
                for (Class<? extends AFISAFITYPE> afiClass : neighAfiSafi) {
                    renderNeighbor(NEIGHBOR_GLOBAL, instanceIdentifier, neighbor,
                            "as", bgpAs,
                            "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiClass),
                            "neighbor_ip", neighborIp,
                            "neighbor", neighbor);
                }
            }
        } else {
            String vrfName = vrfKey.getName();
            checkArgument(!neighAfiSafi.isEmpty(), "No afi safi defined for neighbor: %s in VRF: %s", neighborIp, vrfName);

            for (Class<? extends AFISAFITYPE> afiClass : neighAfiSafi) {
                renderNeighbor(NEIGHBOR_VRF, instanceIdentifier, neighbor,
                        "as", bgpAs,
                        "vrf", vrfName,
                        "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiClass),
                        "neighbor_ip", neighborIp,
                        "neighbor", neighbor);
            }
        }
    }

    private void renderNeighbor(String template, InstanceIdentifier<Neighbor> id, Neighbor data, Object... params) throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(fT(template, params), cli, id, data);
    }

    private void deleteNeighbor(String template, InstanceIdentifier<Neighbor> id, Object... params) throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(fT(template, params), cli, id);
    }

    private Long getAsValue(Global global) {
        return checkNotNull(checkNotNull(global.getConfig()).getAs()).getValue();
    }

    /**
     * Get neighbor specific afiSafi list or if empty, use BGP instance specific afi safi list
     */
    private List<Class<? extends AFISAFITYPE>> getAfiSafisForNeighbor(Global bgpGlobal, Neighbor neighbor) {
        List<Class<? extends AFISAFITYPE>> neighAfiSafi = Collections.emptyList();

        if (neighbor.getAfiSafis() != null && neighbor.getAfiSafis().getAfiSafi() != null) {
            neighAfiSafi = neighbor.getAfiSafis().getAfiSafi()
                    .stream()
                    .map(AfiSafi::getAfiSafiName)
                    .collect(Collectors.toList());
        }

        if (neighAfiSafi.isEmpty()) {
            if (bgpGlobal.getAfiSafis() != null && bgpGlobal.getAfiSafis().getAfiSafi() != null) {
                neighAfiSafi = bgpGlobal.getAfiSafis().getAfiSafi()
                        .stream()
                        .map(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi::getAfiSafiName)
                        .collect(Collectors.toList());
            }
        }
        return neighAfiSafi;
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Neighbor> id, Neighbor dataBefore, Neighbor dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Neighbor> instanceIdentifier, Neighbor neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        final Global bgpGlobal = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Bgp.class)).get().getGlobal();
        Long bgpAs = getAsValue(bgpGlobal);

        List<Class<? extends AFISAFITYPE>> neighAfiSafi = getAfiSafisForNeighbor(bgpGlobal, neighbor);
        String neighborIp = getNeighborIp(instanceIdentifier);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            if (neighAfiSafi.isEmpty()) {
                deleteNeighbor(NEIGHBOR_GLOBAL_DELETE, instanceIdentifier,
                        "as", bgpAs,
                        "neighbor_ip", neighborIp,
                        "neighbor", neighbor);
            } else {
                for (Class<? extends AFISAFITYPE> afiClass : neighAfiSafi) {
                    deleteNeighbor(NEIGHBOR_GLOBAL_DELETE, instanceIdentifier,
                            "as", bgpAs,
                            "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiClass),
                            "neighbor_ip", neighborIp,
                            "neighbor", neighbor);
                }
            }
        } else {
            String vrfName = vrfKey.getName();

            for (Class<? extends AFISAFITYPE> afiClass : neighAfiSafi) {
                deleteNeighbor(NEIGHBOR_VRF_DELETE, instanceIdentifier,
                        "as", bgpAs,
                        "vrf", vrfName,
                        "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiClass),
                        "neighbor_ip", neighborIp,
                        "neighbor", neighbor);
            }
        }
    }

    static String getNeighborIp(InstanceIdentifier<?> neigh) {
        IpAddress addr = neigh.firstKeyOf(Neighbor.class).getNeighborAddress();
        return getNeighborIp(addr);
    }

    static String getNeighborIp(IpAddress addr) {
        return addr.getIpv4Address() != null ?
                addr.getIpv4Address().getValue() :
                addr.getIpv6Address().getValue();
    }
}
