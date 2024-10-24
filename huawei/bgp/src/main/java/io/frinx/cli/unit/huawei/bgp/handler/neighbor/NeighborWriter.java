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

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAug;
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

public class NeighborWriter implements CliListWriter<Neighbor, NeighborKey> {

    private static final String NEIGHBOR_POLICIES =
            // Set import/export policies
            """
                    {% loop in $neighbor.apply_policy.config.import_policy as $im_p %}
                    peer {$neighbor_ip} route-policy {$im_p} import
                    {% onEmpty %}{% endloop %}{% loop in $neighbor.apply_policy.config.export_policy as $ex_p %}
                    peer {$neighbor_ip} route-policy {$ex_p} export
                    {% onEmpty %}{% endloop %}""";

    private static final String NEIGHBOR_COMMON_CONFIG = """
            {% if ($description) %}peer {$neighbor_ip} description {$description}
            {% else %}undo peer {$neighbor_ip} description
            {% endif %}{% if ($password) %}peer {$neighbor_ip} password cipher {$password}
            {% else %}undo peer {$neighbor_ip} password cipher
            {% endif %}{% if ($timer_mode) %}peer {$neighbor_ip} timer {$timer_mode} {$time_before} hold {$time_after}
            {% else %}undo peer {$neighbor_ip} timer
            {% endif %}{% if ($transport) %}peer {$neighbor_ip} path-mtu {$transport}
            {% else %}undo peer {$neighbor_ip} path-mtu
            {% endif %}""";

    static final String NEIGHBOR_GLOBAL = "system-view\n"
            + "bgp {$as}\n"
            + "peer {$neighbor_ip} as-number {$neighbor.config.peer_as.value}\n"
            +
            NEIGHBOR_COMMON_CONFIG
            +

            // //Active the neighbor. Either under address family, or globally if no family present
            "{.if ($afi_safi) }ipv4-family {$afi_safi}\n{/if}"
            +

            //Set policies
            NEIGHBOR_POLICIES
            +

            "peer {$neighbor_ip} enable\n"
            +

            "{.if ($afi_safi) }quit\n{/if}"
            +

            "commit\n"
            + "return";

    static final String NEIGHBOR_GLOBAL_DELETE = "system-view\n"
            + "bgp {$as}\n"
            +
            NEIGHBOR_COMMON_CONFIG
            +
            "{.if ($afi_safi) }ipv4-family {$afi_safi}\n{/if}"
            + "undo peer {$neighbor_ip} enable\n"
            + "{.if ($afi_safi) }quit\n{/if}"
            +

            "undo peer {$neighbor_ip}\n"
            + "Y\n"
            +

            "commit\n"
            + "return";

    static final String NEIGHBOR_VRF = "system-view\n"
            + "bgp {$as}\n"
            +

            // //Active the neighbor. Either under address family, or globally if no family present
            "ipv4-family vpn-instance {$vrf}\n"
            +

            "peer {$neighbor_ip} as-number {$neighbor.config.peer_as.value}\n"
            +
            NEIGHBOR_COMMON_CONFIG
            +
            //Set policies
            NEIGHBOR_POLICIES
            +

            "peer {$neighbor_ip} enable\n"
            +

            "commit\n"
            + "return";

    static final String NEIGHBOR_VRF_DELETE = "system-view\n"
            + "bgp {$as}\n"
            +

            "ipv4-family vpn-instance {$vrf}\n"
            + "undo peer {$neighbor_ip} enable\n"
            +
            NEIGHBOR_COMMON_CONFIG

            + "undo peer {$neighbor_ip} \n"
            + "Y\n"
            + "commit\n"
            + "return";

    private Cli cli;

    public NeighborWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Neighbor> instanceIdentifier,
            @NotNull Neighbor neighbor, @NotNull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();
        final Global bgpGlobal = bgp.getGlobal();
        Long bgpAs = getAsValue(bgpGlobal);

        List<Class<? extends AFISAFITYPE>> neighAfiSafi = getAfiSafisForNeighbor(bgpGlobal, neighbor);
        String neighborIp = getNeighborIp(instanceIdentifier);
        String description = neighbor.getConfig().getDescription();
        String password = getPasswordOrNull(neighbor);

        BgpNeighborConfigAug configAug = neighbor.getConfig().getAugmentation(BgpNeighborConfigAug.class);
        String timerMode = null;
        String transport = null;
        Short timeBefore = null;
        Short timeAfter = null;
        if (configAug != null) {
            if (configAug.getTimerConfiguration() != null) {
                timerMode = configAug.getTimerConfiguration().getTimerMode();
                timeBefore = configAug.getTimerConfiguration().getTimeBefore();
                timeAfter = configAug.getTimerConfiguration().getTimeAfter();
            }
            if (configAug.getTransport() != null) {
                transport = configAug.getTransport().getName();
            }
        }
        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            if (neighAfiSafi.isEmpty()) {
                renderNeighbor(NEIGHBOR_GLOBAL, instanceIdentifier, neighbor,
                        "as", bgpAs,
                        "neighbor_ip", neighborIp,
                        "neighbor", neighbor,
                        "description", description,
                        "password", password,
                        "timer_mode", timerMode,
                        "time_before", timeBefore,
                        "time_after", timeAfter,
                        "transport", transport);
            } else {
                for (Class<? extends AFISAFITYPE> afiClass : neighAfiSafi) {
                    renderNeighbor(NEIGHBOR_GLOBAL, instanceIdentifier, neighbor,
                            "as", bgpAs,
                            "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiClass),
                            "neighbor_ip", neighborIp,
                            "neighbor", neighbor,
                            "description", description,
                            "password", password,
                            "timer_mode", timerMode,
                            "time_before", timeBefore,
                            "time_after", timeAfter,
                            "transport", transport);
                }
            }
        } else {
            String vrfName = vrfKey.getName();
            Preconditions.checkArgument(!neighAfiSafi.isEmpty(),
                    "No afi safi defined for neighbor: %s in VRF: %s", neighborIp, vrfName);

            for (Class<? extends AFISAFITYPE> afiClass : neighAfiSafi) {
                renderNeighbor(NEIGHBOR_VRF, instanceIdentifier, neighbor,
                        "as", bgpAs,
                        "vrf", vrfName,
                        "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiClass),
                        "neighbor_ip", neighborIp,
                        "neighbor", neighbor,
                        "description", description,
                        "password", password,
                        "timer_mode", timerMode,
                        "time_before", timeBefore,
                        "time_after", timeAfter,
                        "transport", transport);
            }
        }
    }

    private void renderNeighbor(String template, InstanceIdentifier<Neighbor> id, Neighbor data, Object... params)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(fT(template, params), cli, id, data);
    }

    private void deleteNeighbor(String template, InstanceIdentifier<Neighbor> id, Object... params) throws
            WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(fT(template, params), cli, id);
    }

    private Long getAsValue(Global global) {
        return Preconditions.checkNotNull(Preconditions.checkNotNull(global.getConfig()).getAs()).getValue();
    }

    /**
     * Get neighbor specific afiSafi list or if empty, use BGP instance specific afi safi list.
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
                        .map(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi
                                .safi.list.AfiSafi::getAfiSafiName)
                        .collect(Collectors.toList());
            }
        }
        return neighAfiSafi;
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Neighbor> id, @NotNull Neighbor dataBefore,
            @NotNull Neighbor dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        // FI-789 investigate if this pattern is avoidable or not
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Neighbor> instanceIdentifier,
            @NotNull Neighbor neighbor, @NotNull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();
        final Global bgpGlobal = bgp.getGlobal();
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

    private String getPasswordOrNull(Neighbor neighbor) {
        if (neighbor.getConfig().getAuthPassword() != null) {
            return neighbor.getConfig().getAuthPassword().getPlainString().getValue();
        }
        return null;
    }

    static String getNeighborIp(InstanceIdentifier<?> neigh) {
        IpAddress addr = neigh.firstKeyOf(Neighbor.class).getNeighborAddress();
        return addr.getIpv4Address() != null ? addr.getIpv4Address().getValue() : addr.getIpv6Address().getValue();
    }
}