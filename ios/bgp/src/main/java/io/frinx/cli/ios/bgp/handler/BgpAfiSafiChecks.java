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

package io.frinx.cli.ios.bgp.handler;

import com.google.common.collect.Sets;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

/**
 * Tools for checking of consistency between configured address-families in different parts of BGP.
 */
public final class BgpAfiSafiChecks {

    private BgpAfiSafiChecks() {
    }

    /**
     * Checking if:
     * 1) All neighbor and peer-group afi-safis are placed in global afi-safis,
     * 2) Specific global afi-safis are placed somewhere under neighbors or peer-groups.
     * If the inconsistency is found, {@link IllegalArgumentException} with description will be thrown.
     *
     * @param vrfKey                Name of the network instance.
     * @param bgpConfigurationAfter BGP container that is common parent for each address-family configuration snippet.
     */
    public static void checkAddressFamilies(final NetworkInstanceKey vrfKey, final Bgp bgpConfigurationAfter) {
        final Long autonomousSystemNumber = bgpConfigurationAfter.getGlobal().getConfig().getAs().getValue();
        final Set<? extends Class<? extends AFISAFITYPE>> globalAfiSafis = getGlobalAfiSafis(bgpConfigurationAfter);
        final Set<Class<? extends AFISAFITYPE>> specificAfiSafis = getSpecificAfiSafis(bgpConfigurationAfter);

        // 1. check - all neighbor and peer-group afi-safis must be placed in global afi-safis
        checkGlobalAfiSafisContainSpecificAfiSafis(autonomousSystemNumber, globalAfiSafis, specificAfiSafis);

        // 2. check - some of global afi-safis must be placed somewhere under neighbors or peer-groups
        checkSpecificAfiSafisContainGlobalAfiSafis(autonomousSystemNumber, vrfKey, globalAfiSafis, specificAfiSafis);
    }

    private static Set<? extends Class<? extends AFISAFITYPE>> getGlobalAfiSafis(final Bgp bgpConfigurationAfter) {
        return Optional.ofNullable(bgpConfigurationAfter.getGlobal().getAfiSafis().getAfiSafi())
                .map(afiSafis -> afiSafis.stream()
                        .map(afiSafi -> afiSafi.getAfiSafiName())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    private static Set<Class<? extends AFISAFITYPE>> getSpecificAfiSafis(final Bgp bgpConfigurationAfter) {
        final Set<? extends Class<? extends AFISAFITYPE>> neighborsAfiSafis = Optional.ofNullable(
                bgpConfigurationAfter.getNeighbors().getNeighbor())
                .map(neighbors -> neighbors.stream()
                        .flatMap(neighbor -> Optional.ofNullable(neighbor.getAfiSafis().getAfiSafi())
                                .map(afiSafis -> afiSafis.stream().map(afiSafi -> afiSafi.getAfiSafiName()))
                                .orElse(Stream.empty()))
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        final Set<? extends Class<? extends AFISAFITYPE>> peerGroupsAfiSafis = Optional.ofNullable(
                bgpConfigurationAfter.getPeerGroups().getPeerGroup())
                .map(peerGroups -> peerGroups.stream()
                        .flatMap(peerGroup -> Optional.ofNullable(peerGroup.getAfiSafis().getAfiSafi())
                                .map(afiSafis -> afiSafis.stream().map(afiSafi -> afiSafi.getAfiSafiName()))
                                .orElse(Stream.empty()))
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        return Sets.union(neighborsAfiSafis, peerGroupsAfiSafis);
    }

    /**
     * Checking whether all neighbor and peer-group afi-safis are placed in global afi-safis.
     *
     * @param autonomousSystemNumber BGP autonomous system number that is used in logging message.
     * @param globalAfiSafis         List of global AFI/SAFIs that are common for all neighbors / peer-groups.
     * @param specificAfiSafis       Specific AFI/SAFIs configured under neighbors or peer-groups.
     */
    private static void checkGlobalAfiSafisContainSpecificAfiSafis(final Long autonomousSystemNumber,
            final Set<? extends Class<? extends AFISAFITYPE>> globalAfiSafis,
            final Set<Class<? extends AFISAFITYPE>> specificAfiSafis) {
        if (!globalAfiSafis.containsAll(specificAfiSafis)) {
            throw new IllegalArgumentException(String.format("Unable to configure BGP with AS %s: Global AFI-SAFIs "
                            + "don't contain all AFI-SAFIs that are placed under BGP neighbors and peer-groups - "
                            + "the following address-families are missing: %s.",
                    autonomousSystemNumber,
                    Sets.difference(specificAfiSafis, globalAfiSafis).stream()
                            .map(GlobalAfiSafiConfigWriter::toDeviceAddressFamily)
                            .collect(Collectors.toList())));
        }
    }

    /**
     * Checking whether specific global afi-safis are placed somewhere under neighbors or peer-groups. There are two
     * cases that must be treated differently:
     * A) VRF is set to 'default' (it is not configured) - VPNv4, and VPNv6 cannot be configured without
     * any nested neighbor's configuration.
     * B) VRF is not set to 'default' - IPv4, IPv6, VPNv4, and VPNv6 cannot be configured without
     * any nested neighbor's configuration.
     *
     * @param autonomousSystemNumber BGP autonomous system number that is used in logging message.
     * @param vrfKey                 Name of the network instance.
     * @param globalAfiSafis         List of global AFI/SAFIs that are common for all neighbors / peer-groups.
     * @param specificAfiSafis       Specific AFI/SAFIs configured under neighbors or peer-groups.
     */
    private static void checkSpecificAfiSafisContainGlobalAfiSafis(final Long autonomousSystemNumber,
            final NetworkInstanceKey vrfKey, final Set<? extends Class<? extends AFISAFITYPE>> globalAfiSafis,
            final Set<Class<? extends AFISAFITYPE>> specificAfiSafis) {
        Set<Class> filteredTypes;
        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            filteredTypes = Sets.newHashSet(L3VPNIPV4UNICAST.class, L3VPNIPV6UNICAST.class);
        } else {
            filteredTypes = Sets.newHashSet(IPV4UNICAST.class, IPV6UNICAST.class,
                    L3VPNIPV4UNICAST.class, L3VPNIPV6UNICAST.class);
        }
        final Set<? extends Class<? extends AFISAFITYPE>> mandatoryTypes = globalAfiSafis.stream()
                .filter(afiSafiName -> !filteredTypes.contains(afiSafiName))
                .collect(Collectors.toSet());
        if (!specificAfiSafis.containsAll(mandatoryTypes)) {
            throw new IllegalArgumentException(String.format("Unable to configure BGP with AS %s: Global AFI-SAFIs "
                            + "contain some AFI-SAFIs that should be placed under at least one neighbor "
                            + "or peer-group configuration - the following address-families are missing: %s.",
                    autonomousSystemNumber,
                    Sets.difference(mandatoryTypes, specificAfiSafis).stream()
                            .map(GlobalAfiSafiConfigWriter::toDeviceAddressFamily)
                            .collect(Collectors.toList())));
        }
    }
}