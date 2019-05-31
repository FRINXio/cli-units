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

package io.frinx.cli.ios.bgp.handler.neighbor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.BgpAfiSafiChecks;
import io.frinx.cli.ios.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupRouteReflectorConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonStructureNeighborGroupRouteReflector;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.route.reflector.RouteReflector;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafis;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborWriter implements CliListWriter<Neighbor, NeighborKey> {

    // TODO split this into regular smaller writers if possible. Especially the AFI SAFI handling (update and
    // removal) is
    // difficult this way, since diff has to be performed in this writer
    // Applies for PeerGroupWriter as well

    public static final String NEIGHBOR_COMMON_CONFIG = "{%if ($neighbor.config.description) %}neighbor "
            + "{$neighbor_id} description {$neighbor.config.description}\n"
            + "{% elseIf ($before.config.description) %}no neighbor {$neighbor_id} description\n{% endif %}"
            + "{%if ($neighbor.config.auth_password) %}"
            + "{%if ($neighbor.config.auth_password.encrypted_string) %}"
            + "neighbor {$neighbor_id} password "
            + "{$neighbor.config.auth_password.encrypted_string.value|s/^Encrypted\\[|\\]$/ /g|trim}\n"
            + "{% elseIf ($neighbor.config.auth_password.plain_string) %}"
            + "neighbor {$neighbor_id} password {$neighbor.config.auth_password.plain_string.value}\n"
            + "{% endif %}"
            + "{% elseIf ($before.config.auth_password) %}"
            + "no neighbor {$neighbor_id} password\n{% endif %}";

    public static final String NEIGHBOR_RR_CONFIG = "{%if ($route_reflect_client) %}neighbor {$neighbor_id} "
            + "route-reflector-client\n"
            + "{% elseIf ($before_route_reflect_client) %}no neighbor {$neighbor_id} route-reflector-client\n{% endif"
            + " %}";

    public static final String NEIGHBOR_ENABLE_CONFIG = "{%if ($enabled) %}neighbor {$neighbor_id} activate\n"
            + "{% elseIf ($before_enabled) %}no neighbor {$neighbor_id} activate\n{% endif %}";

    public static final String NEIGHBOR_SEND_COMMUNITY_CONFIG = "{%if ($neighbor.config.send_community.name) "
            + "%}neighbor {$neighbor_id} send-community {$neighbor.config.send_community.name|lc}\n"
            + "{% elseIf ($before.config.send_community.name) %}no neighbor {$neighbor_id} send-community {$before"
            + ".config.send_community.name|lc}\n{% endif %}";

    private static final String NEIGHBOR_ESSENTIAL_CONFIG = "{%if ($neighbor.config.peer_as.value) %}neighbor "
            + "{$neighbor_id} remote-as {$neighbor.config.peer_as.value}\n"
            + "{% elseIf ($before.config.peer_as.value) %}no neighbor {$neighbor_id} remote-as\n{% endif %}"
            + "{%if ($neighbor.config.peer_group) %}neighbor {$neighbor_id} peer-group {$neighbor.config.peer_group}\n"
            + "{% elseIf ($before.config.peer_group) %}no neighbor {$neighbor_id} peer-group\n{% endif %}";

    private static final String NEIGHBOR_DELETE = "{%if ($neighbor.config.peer_group) %}no neighbor {$neighbor_id} "
            + "peer-group {$neighbor.config.peer_group}\n{% endif %}"
            + "{%if ($neighbor.config.peer_as.value) %}no neighbor {$neighbor_id} remote-as {$neighbor.config.peer_as"
            + ".value}\n{% endif %}";

    public static final String NEIGHBOR_TRANSPORT =
            //Set update source
            "{%if ($neighbor.transport.config.local_address.string) %}neighbor {$neighbor_id} update-source "
                    + "{$neighbor.transport.config.local_address.string}\n"
                    + "{% elseIf ($before.transport.config.local_address.string) %}no neighbor {$neighbor_id} "
                    + "update-source {$before.transport.config.local_address.string}\n{% endif %}"
                    + "{%if ($neighbor.transport.config|lc =~ /passivemode=true/) %}neighbor {$neighbor_id} transport"
                    + " connection-mode passive\n"
                    + "{% elseIf ($before.transport.config|lc =~ /passivemode=true/) %}no neighbor {$neighbor_id} "
                    + "transport connection-mode passive\n{% endif %}";

    public static final String NEIGHBOR_POLICIES = "{% loop in $neighbor.apply_policy.config.import_policy as $im_p "
            + "%}\n"
            + "neighbor {$neighbor_id} route-map {$im_p} in\n"
            + "{% onEmpty %}"
            +
            // Remove before policies if there were any set
            "{% loop in $before.apply_policy.config.import_policy as $im_p_before %}\n"
            + "no neighbor {$neighbor_id} route-map {$im_p_before} in\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% endloop %}"
            + "{% loop in $neighbor.apply_policy.config.export_policy as $ex_p %}\n"
            + "neighbor {$neighbor_id} route-map {$ex_p} out\n"
            + "{% onEmpty %}"
            +
            // Remove before policies if there were any set
            "{% loop in $before.apply_policy.config.export_policy as $ex_p_before %}\n"
            + "no neighbor {$neighbor_id} route-map {$ex_p_before} out\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% endloop %}";

    public static final String NEIGHBOR_AFI_POLICIES = "{% loop in $af.apply_policy.config.import_policy as $im_p %}\n"
            + "neighbor {$neighbor_id} route-map {$im_p} in\n"
            + "{% onEmpty %}"
            +
            // Find the afi from before and remove its policies
            "{% loop in $before_afis as $af_name_before:af_before %}\n"
            + "{% if ($af_name == $af_name_before)}"
            + "{% loop in $af_before.apply_policy.config.import_policy as $im_p_before %}\n"
            + "no neighbor {$neighbor_id} route-map {$im_p_before} in\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% endif %}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% endloop %}"
            + "{% loop in $af.apply_policy.config.export_policy as $ex_p %}\n"
            + "neighbor {$neighbor_id} route-map {$ex_p} out\n"
            + "{% onEmpty %}"
            +
            // Find the afi from before and remove its policies
            "{% loop in $before_afis as $af_name_before:af_before %}\n"
            + "{% if ($af_name == $af_name_before)}"
            + "{% loop in $af_before.apply_policy.config.export_policy as $ex_p_before %}\n"
            + "no neighbor {$neighbor_id} route-map {$ex_p_before} out\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% endif %}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% endloop %}";

    static final String NEIGHBOR_GLOBAL = "configure terminal\n"
            + "router bgp {$as}\n"
            + NEIGHBOR_ESSENTIAL_CONFIG
            + NEIGHBOR_COMMON_CONFIG
            + NEIGHBOR_TRANSPORT
            + NEIGHBOR_POLICIES
            +

            "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name}\n"
            + NEIGHBOR_SEND_COMMUNITY_CONFIG
            + NEIGHBOR_RR_CONFIG
            + NEIGHBOR_AFI_POLICIES
            + NEIGHBOR_ENABLE_CONFIG
            + "exit\n"
            + "{% onEmpty %}"
            + NEIGHBOR_SEND_COMMUNITY_CONFIG
            + NEIGHBOR_RR_CONFIG
            + NEIGHBOR_ENABLE_CONFIG
            + "{% endloop %}"
            + "end";

    static final String NEIGHBOR_VRF = "configure terminal\n"
            + "router bgp {$as}\n"
            +

            "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name} vrf {$vrf}\n"
            + NEIGHBOR_ESSENTIAL_CONFIG
            + NEIGHBOR_COMMON_CONFIG
            + NEIGHBOR_TRANSPORT
            + NEIGHBOR_SEND_COMMUNITY_CONFIG
            + NEIGHBOR_RR_CONFIG
            + NEIGHBOR_POLICIES
            + NEIGHBOR_AFI_POLICIES
            + NEIGHBOR_ENABLE_CONFIG
            + "exit\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "end";

    static final String NEIGHBOR_GLOBAL_DELETE = "configure terminal\n"
            + "router bgp {$as}\n"
            + NEIGHBOR_DELETE
            + "end";

    static final String NEIGHBOR_GLOBAL_DELETE_AFI = "configure terminal\n"
            + "router bgp {$as}\n"
            +

            "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name}\n"
            + "no neighbor {$neighbor_id} activate\n"
            + "exit\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            +

            "end";

    static final String NEIGHBOR_VRF_DELETE = "configure terminal\n"
            + "router bgp {$as}\n"
            +

            "{% loop in $afis as $af_name:af %}\n"
            + "address-family {$af_name} vrf {$vrf}\n"
            + NEIGHBOR_DELETE
            + "exit\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            +

            "end";

    private Cli cli;

    public NeighborWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Neighbor> instanceIdentifier, Neighbor neighbor,
                                              WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();

        final Global bgpGlobal = bgp.getGlobal();
        Long bgpAs = getAsValue(bgpGlobal);
        Map<String, Object> afiSafisForNeighbor = getAfiSafisForNeighbor(neighbor.getAfiSafis());
        if (!afiSafisForNeighbor.isEmpty()) {
            BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);
        }
        Map<String, Object> neighAfiSafi = getAfiSafisForNeighbor(bgpGlobal, afiSafisForNeighbor);
        String neighborIp = getNeighborIp(instanceIdentifier);
        Boolean enabled = neighbor.getConfig().isEnabled();

        renderNeighbor(this, cli, instanceIdentifier,
                neighbor, null, enabled, null, vrfKey, bgpAs, neighAfiSafi, Collections.emptyMap(), neighborIp,
                NEIGHBOR_GLOBAL, NEIGHBOR_VRF);
    }

    public static Global getGlobalBgpForDelete(InstanceIdentifier<?> instanceIdentifier, WriteContext writeContext) {
        return writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Bgp.class)).get().getGlobal();
    }

    private static <T extends DataObject> void renderNeighbor(CliWriter<T> writer, Cli cli,
                                                              String template,
                                                              InstanceIdentifier<T> id,
                                                              T data,
                                                              Object... params) throws WriteFailedException
            .CreateFailedException {
        writer.blockingWriteAndRead(writer.fT(template, params), cli, id, data);
    }

    public static <T extends BgpCommonStructureNeighborGroupRouteReflector> void renderNeighbor(
            CliWriter<T> writer,
            Cli cli,
            InstanceIdentifier<T>
            instanceIdentifier,
            T neighbor,
            T before,
            Boolean enabled,
            Boolean beforeEnabled,
            NetworkInstanceKey
            vrfKey,
            Long bgpAs,
            Map<String, Object>
            neighAfiSafi,
            Map<String, Object>
            beforeAfiSafi,
            String neighborId,
            String globalTemplate,
            String vrfTemplate)
            throws WriteFailedException.CreateFailedException {

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            renderNeighbor(writer, cli, globalTemplate, instanceIdentifier, neighbor,
                    "as", bgpAs,
                    "neighbor_id", neighborId,
                    "neighbor", neighbor,
                    "before", before,
                    "afis", neighAfiSafi,
                    "before_afis", beforeAfiSafi,
                    "route_reflect_client", isRouteReflectClient(neighbor),
                    "before_route_reflect_client", isRouteReflectClient(before),
                    "enabled", enabled,
                    "before_enabled", beforeEnabled);
        } else {
            String vrfName = vrfKey.getName();
            Preconditions.checkArgument(!neighAfiSafi.isEmpty(), "No afi safi defined for neighbor: %s in VRF: %s",
                    neighborId, vrfName);
            renderNeighbor(writer, cli, vrfTemplate, instanceIdentifier, neighbor,
                    "as", bgpAs,
                    "vrf", vrfName,
                    "neighbor_id", neighborId,
                    "neighbor", neighbor,
                    "before", before,
                    "afis", neighAfiSafi,
                    "before_afis", beforeAfiSafi,
                    "route_reflect_client", isRouteReflectClient(neighbor),
                    "before_route_reflect_client", isRouteReflectClient(before),
                    "enabled", enabled,
                    "before_enabled", beforeEnabled);
        }
    }

    public static <T extends BgpCommonStructureNeighborGroupRouteReflector> void renderNeighborAfiRemoval(
            CliWriter<T> writer, Cli cli,
            InstanceIdentifier<T> instanceIdentifier,
            T neighbor,
            NetworkInstanceKey vrfKey,
            Long bgpAs,
            Map<String,
                    Object> neighAfiSafi,
            String
                    neighborId,
            String
                    globalTemplate,
            String
                    vrfTemplate) throws WriteFailedException.CreateFailedException {

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            renderNeighbor(writer, cli, globalTemplate, instanceIdentifier, neighbor,
                    "as", bgpAs,
                    "neighbor_id", neighborId,
                    "afis", neighAfiSafi);
        } else {
            String vrfName = vrfKey.getName();
            renderNeighbor(writer, cli, vrfTemplate, instanceIdentifier, neighbor,
                    "as", bgpAs,
                    "vrf", vrfName,
                    "neighbor_id", neighborId,
                    "neighbor", neighbor,
                    "afis", neighAfiSafi);
        }
    }

    public static Boolean isRouteReflectClient(BgpCommonStructureNeighborGroupRouteReflector neighbor) {
        return Optional.ofNullable(neighbor)
                .map(BgpCommonStructureNeighborGroupRouteReflector::getRouteReflector)
                .map(RouteReflector::getConfig)
                .map(BgpCommonNeighborGroupRouteReflectorConfig::isRouteReflectorClient)
                .orElse(null);
    }

    public static <T extends BgpCommonStructureNeighborGroupRouteReflector> void deleteNeighbor(
            CliWriter<T> writer,
            Cli cli,
            InstanceIdentifier<T>
                    instanceIdentifier,
            T neighbor,
            NetworkInstanceKey
                    vrfKey,
            Long bgpAs,
            Map<String, Object>
                    neighAfiSafi,
            String neighborId,
            String globalTemplate, String vrfTemplate) throws WriteFailedException.DeleteFailedException {
        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            deleteNeighbor(writer, cli, globalTemplate, instanceIdentifier,
                    "as", bgpAs,
                    "neighbor_id", neighborId,
                    "neighbor", neighbor,
                    "afis", neighAfiSafi,
                    "route_reflect_client", isRouteReflectClient(neighbor));
        } else {
            String vrfName = vrfKey.getName();
            deleteNeighbor(writer, cli, vrfTemplate, instanceIdentifier,
                    "as", bgpAs,
                    "vrf", vrfName,
                    "neighbor_id", neighborId,
                    "neighbor", neighbor,
                    "afis", neighAfiSafi,
                    "route_reflect_client", isRouteReflectClient(neighbor));
        }
    }

    private static <T extends DataObject> void deleteNeighbor(CliWriter<T> writer, Cli cli,
                                                              String template,
                                                              InstanceIdentifier<T> id,
                                                              Object... params) throws WriteFailedException
            .DeleteFailedException {
        writer.blockingDeleteAndRead(writer.fT(template, params), cli, id);
    }

    public static Long getAsValue(Global global) {
        return Preconditions.checkNotNull(Preconditions.checkNotNull(global.getConfig()).getAs()).getValue();
    }

    static Map<String, Object> getAfiSafisForNeighbor(AfiSafis afiSafis) {
        List<AfiSafi> configured = (afiSafis != null && afiSafis.getAfiSafi() != null) ? afiSafis.getAfiSafi() :
                Collections.emptyList();
        return configured.stream()
                .map(afi -> new AbstractMap.SimpleEntry<>(GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afi
                        .getAfiSafiName()), afi))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get neighbor specific afiSafi list or if empty, use BGP instance specific afi safi list.
     */
    public static Map<String, Object> getAfiSafisForNeighbor(Global bgpGlobal,
                                                             Map<String, Object> neighAfiSafi) {
        if (neighAfiSafi.isEmpty()) {
            if (bgpGlobal.getAfiSafis() != null && bgpGlobal.getAfiSafis().getAfiSafi() != null) {
                neighAfiSafi = bgpGlobal.getAfiSafis().getAfiSafi().stream()
                        .map(afi -> new AbstractMap.SimpleEntry<>(GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afi
                                .getAfiSafiName()), afi))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
        }
        return neighAfiSafi;
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Neighbor> instanceIdentifier,
                                               Neighbor before, Neighbor neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();
        if (afiSafisHaveChanged(before, neighbor)) {
            BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);
        }

        final Global bgpGlobal = bgp.getGlobal();
        Long bgpAs = getAsValue(bgpGlobal);
        Map<String, Object> neighAfiSafi = getAfiSafisForNeighbor(bgpGlobal, getAfiSafisForNeighbor(neighbor
                .getAfiSafis()));

        final Global bgpGlobalBefore = getGlobalBgpForDelete(instanceIdentifier, writeContext);
        Map<String, Object> neighAfiSafiBefore = getAfiSafisForNeighbor(bgpGlobalBefore, getAfiSafisForNeighbor(
                before.getAfiSafis()));
        Map<String, Object> afisToRemove = Maps.difference(neighAfiSafiBefore, neighAfiSafi).entriesOnlyOnLeft();

        String neighborIp = getNeighborIp(instanceIdentifier);
        Boolean enabled = neighbor.getConfig().isEnabled();
        Boolean beforeEnabled = before.getConfig().isEnabled();

        // This is a subtree writer which handles entire neighbor config. This means that if during update an AFI was
        // removed, it has to be detected and deleted here
        if (!afisToRemove.isEmpty()) {
            renderNeighborAfiRemoval(this, cli, instanceIdentifier,
                    neighbor, vrfKey, bgpAs, afisToRemove, neighborIp, NEIGHBOR_GLOBAL_DELETE_AFI, NEIGHBOR_VRF_DELETE);
        }

        // Then update existing attributes
        renderNeighbor(this, cli, instanceIdentifier,
                neighbor, before, enabled, beforeEnabled, vrfKey, bgpAs, neighAfiSafi, neighAfiSafiBefore, neighborIp,
                NEIGHBOR_GLOBAL, NEIGHBOR_VRF);
    }

    private static boolean afiSafisHaveChanged(final Neighbor before, final Neighbor after) {
        final List<AfiSafi> afiSafisBefore = before.getAfiSafis().getAfiSafi();
        final List<AfiSafi> afiSafisAfter = after.getAfiSafis().getAfiSafi();
        if (afiSafisBefore == null && afiSafisAfter != null || afiSafisBefore != null && afiSafisAfter == null) {
            return true;
        } else if (afiSafisAfter == null) {
            return false;
        } else {
            return !(new HashSet<>(afiSafisAfter).equals(new HashSet<>(afiSafisBefore)));
        }
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Neighbor> instanceIdentifier, Neighbor neighbor,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get();

        final Global bgpGlobal = bgp.getGlobal();
        Long bgpAs = getAsValue(bgpGlobal);

        Map<String, Object> afiSafisForNeighbor = getAfiSafisForNeighbor(neighbor.getAfiSafis());
        if (!afiSafisForNeighbor.isEmpty()) {
            BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);
        }
        Map<String, Object> neighAfiSafi = getAfiSafisForNeighbor(bgpGlobal, afiSafisForNeighbor);
        String neighborIp = getNeighborIp(instanceIdentifier);

        deleteNeighbor(this, cli, instanceIdentifier, neighbor, vrfKey, bgpAs, neighAfiSafi, neighborIp,
                NEIGHBOR_GLOBAL_DELETE, NEIGHBOR_VRF_DELETE);
    }

    public static String getNeighborIp(InstanceIdentifier<?> neigh) {
        IpAddress addr = neigh.firstKeyOf(Neighbor.class).getNeighborAddress();
        return getNeighborIp(addr);
    }

    public static String getNeighborIp(IpAddress addr) {
        return addr.getIpv4Address() != null
                ?
                addr.getIpv4Address().getValue() :
                addr.getIpv6Address().getValue();
    }
}
