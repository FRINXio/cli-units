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

import com.google.common.collect.Lists;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.PeerGroupsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddressBuilder;

public class BgpAfiSafiChecksTest {

    private static final Bgp SAMPLE_BGP_CONFIG_1 = new BgpBuilder()
            .setGlobal(new GlobalBuilder()
                    .setConfig(new ConfigBuilder()
                            .setAs(AsNumber.getDefaultInstance("100"))
                            .build())
                    .setAfiSafis(new AfiSafisBuilder()
                            .setAfiSafi(Lists.newArrayList(
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV6UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(L3VPNIPV4UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(L3VPNIPV6UNICAST.class)
                                            .build()
                            ))
                            .build())
                    .build())
            .setNeighbors(new NeighborsBuilder()
                    .setNeighbor(Collections.singletonList(new NeighborBuilder()
                            .setNeighborAddress(IpAddressBuilder.getDefaultInstance("1.1.1.1"))
                            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                    .rev170202.bgp.neighbor.base.AfiSafisBuilder()
                                    .setAfiSafi(Collections.singletonList(new org.opendaylight.yang.gen.v1.http
                                            .frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi
                                            .list.AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build()))
                                    .build())
                            .build()))
                    .build())
            .setPeerGroups(new PeerGroupsBuilder()
                    .setPeerGroup(Collections.singletonList(new PeerGroupBuilder()
                            .setPeerGroupName("group1")
                            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                    .rev170202.bgp.peer.group.base.AfiSafisBuilder()
                                    .setAfiSafi(Collections.singletonList(new org.opendaylight.yang.gen.v1.http
                                            .frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi
                                            .list.AfiSafiBuilder()
                                            .setAfiSafiName(IPV6UNICAST.class)
                                            .build()))
                                    .build())
                            .build()))
                    .build())
            .build();

    private static final Bgp SAMPLE_BGP_CONFIG_2 = new BgpBuilder()
            .setGlobal(new GlobalBuilder()
                    .setConfig(new ConfigBuilder()
                            .setAs(AsNumber.getDefaultInstance("100"))
                            .build())
                    .setAfiSafis(new AfiSafisBuilder()
                            .setAfiSafi(Lists.newArrayList(
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV6UNICAST.class)
                                            .build()
                            ))
                            .build())
                    .build())
            .setPeerGroups(new PeerGroupsBuilder()
                    .setPeerGroup(Collections.emptyList())
                    .build())
            .setNeighbors(new NeighborsBuilder()
                    .setNeighbor(Collections.emptyList())
                    .build())
            .build();

    private static final Bgp SAMPLE_BGP_CONFIG_3 = new BgpBuilder()
            .setGlobal(new GlobalBuilder()
                    .setConfig(new ConfigBuilder()
                            .setAs(AsNumber.getDefaultInstance("100"))
                            .build())
                    .setAfiSafis(new AfiSafisBuilder()
                            .setAfiSafi(Lists.newArrayList(
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV6UNICAST.class)
                                            .build()
                            ))
                            .build())
                    .build())
            .setNeighbors(new NeighborsBuilder()
                    .setNeighbor(Collections.singletonList(new NeighborBuilder()
                            .setNeighborAddress(IpAddressBuilder.getDefaultInstance("1.1.1.1"))
                            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                    .rev170202.bgp.neighbor.base.AfiSafisBuilder()
                                    .setAfiSafi(Collections.singletonList(new org.opendaylight.yang.gen.v1.http
                                            .frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi
                                            .list.AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build()))
                                    .build())
                            .build()))
                    .build())
            .setPeerGroups(new PeerGroupsBuilder()
                    .setPeerGroup(Collections.emptyList())
                    .build())
            .build();

    private static final Bgp SAMPLE_BGP_CONFIG_4 = new BgpBuilder()
            .setGlobal(new GlobalBuilder()
                    .setConfig(new ConfigBuilder()
                            .setAs(AsNumber.getDefaultInstance("100"))
                            .build())
                    .setAfiSafis(new AfiSafisBuilder()
                            .setAfiSafi(Lists.newArrayList(
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(L3VPNIPV4UNICAST.class)
                                            .build(),
                                    new AfiSafiBuilder()
                                            .setAfiSafiName(L3VPNIPV6UNICAST.class)
                                            .build()
                            ))
                            .build())
                    .build())
            .setNeighbors(new NeighborsBuilder()
                    .setNeighbor(Collections.singletonList(new NeighborBuilder()
                            .setNeighborAddress(IpAddressBuilder.getDefaultInstance("1.1.1.1"))
                            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                    .rev170202.bgp.neighbor.base.AfiSafisBuilder()
                                    .setAfiSafi(Collections.singletonList(new org.opendaylight.yang.gen.v1.http
                                            .frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi
                                            .list.AfiSafiBuilder()
                                            .setAfiSafiName(IPV4UNICAST.class)
                                            .build()))
                                    .build())
                            .build()))
                    .build())
            .setPeerGroups(new PeerGroupsBuilder()
                    .setPeerGroup(Collections.singletonList(new PeerGroupBuilder()
                            .setPeerGroupName("group1")
                            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                    .rev170202.bgp.peer.group.base.AfiSafisBuilder()
                                    .setAfiSafi(Collections.singletonList(new org.opendaylight.yang.gen.v1.http
                                            .frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi
                                            .list.AfiSafiBuilder()
                                            .setAfiSafiName(IPV6UNICAST.class)
                                            .build()))
                                    .build())
                            .build()))
                    .build())
            .build();

    /**
     * We don't expect IAE - L3VPN and L2VPN types are present in config and IPv4 and IPv6 types are explicitly
     * configured in neighbor and peer-group.
     */
    @Test
    public void checkValidAddressFamiliesCombinationTest1() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
        BgpAfiSafiChecks.checkAddressFamilies(networkInstanceKey, SAMPLE_BGP_CONFIG_1);
    }

    /**
     * We don't expect IAE - L3VPN and L2VPN types are present in config and IPv4 and IPv6 are also presented
     * in configuration of device because of non-default VRF.
     */
    @Test
    public void checkValidAddressFamiliesCombinationTest2() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("vrf01");
        BgpAfiSafiChecks.checkAddressFamilies(networkInstanceKey, SAMPLE_BGP_CONFIG_2);
    }

    /**
     * We don't expect IAE - IPv4 and IPv6 are presented in configuration of device because of non-default VRF.
     */
    @Test
    public void checkValidAddressFamiliesCombinationTest3() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("vrf02");
        BgpAfiSafiChecks.checkAddressFamilies(networkInstanceKey, SAMPLE_BGP_CONFIG_3);
    }

    /**
     * We expect IAE - not all global address-families are placed under neighbor / peer-groups (this is default VRF).
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkInvalidAddressFamiliesCombinationTest1() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
        BgpAfiSafiChecks.checkAddressFamilies(networkInstanceKey, SAMPLE_BGP_CONFIG_2);
    }

    /**
     * We expect IAE - only one of global address-families are placed under neighbor / peer-groups
     * (this is default VRF).
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkInvalidAddressFamiliesCombinationTest2() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
        BgpAfiSafiChecks.checkAddressFamilies(networkInstanceKey, SAMPLE_BGP_CONFIG_3);
    }

    /**
     * We expect IAE - only one of mandatory global address-families (IPv4 and IPv6) are placed under neighbor
     * / peer-groups (this is default VRF).
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkInvalidAddressFamiliesCombinationTest3() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
        BgpAfiSafiChecks.checkAddressFamilies(networkInstanceKey, SAMPLE_BGP_CONFIG_4);
    }
}