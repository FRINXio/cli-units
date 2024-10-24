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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;

class L2P2PVlanReaderTest {

    private static final String OUTPUT = """
            !
            !
            !
            !
            !
            !
            !
             vll 99999 5
             vll FRINXtest 66661446
              vlan 666
              vlan 667
             vll RADTEST 178
              vlan 1551
             vll Test_111 666111
             vll abcd2 45672
              vlan 44
             vll frinxtest 143124141
             vll testL2P2p 1000009999
             vll testservis 1
             vll xyz 111
             vll-local DIR0001415
              vlan 100
             vll-local MESSUNG
              vlan 1599
             vll-local TEST-fuer-NOC
              vlan 20
             vll-local Test_3903
              vlan 1500
             vll-local loopvlan
              vlan 100
             vll-local test12
              vlan 12
             vll-local testtest
              vlan 12
             vll-local xyz
              vlan 33
             vll-local xzy
             vpls managementvpls 12\s
              vlan 11
             vpls mgmtma14 14\s
              vlan 14
             vpls WBNWLC-Management 61\s
              vlan 2
             vpls WBN-APMgmt-SIM 67\s
              vlan 3123
             vpls WBN-Mgmt-SIM 98\s
              vlan 23
             vpls WBN-blizz-BB 328\s
              vlan 130
             vpls WBN-DMZ 338\s
              vlan 60
             vpls WBN-Alarmanl 339\s
              vlan 103
             vpls WBN-Mgmt-MI 457\s
              vlan 18
             vpls WBN-Mgmt-MI-2 458\s
              vlan 118
             vpls WBN-APMgmt-MI 459\s
              vlan 3118
             vpls WBN-Mgmt-Etherlink 460\s
              vlan 25
             vpls WBN-APMgmt-SUED 461\s
              vlan 3127
             vpls WBN-Mgmt-SUED 462\s
              vlan 27
             vpls WBN_LS 483\s
              vlan 100
             vpls WBN_LS_DN2 484\s
              vlan 101
             vpls WBN_VW 485\s
              vlan 200
             vpls WBN_VW_DN2 486\s
              vlan 201
             vpls WLAN-AP-Management 687\s
              vlan 5
             vpls Blizznet-WLAN-User-200 688\s
              vlan 200
              vlan 900
             vpls Blizznet-WLAN-User-201 689\s
              vlan 201
             vpls frtest 999\s
              vlan 10
             vpls secure-mgmt-vlan101 1113\s
              vlan 101
             vpls CPE-Test 1501\s
              vlan 1501
             vpls VLAN666-10.200.5.0 6571\s
              vlan 666
             vpls MAGENTATEST 8888\s
              vlan 2000
             vpls vlan128-test-labor 11211\s
              vlan 128
             vpls FRINXTEST666 666666\s
              vlan 666
            !
            !
            !
            !
            !
            !
            !
            !
            !
            !
            """;

    private static final String OUTPUT_EMPTY = "!\n";


    @Test
    void parseVlans() {
        List<VlanKey> vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT, "abcd2");
        assertEquals(1, vlanKeys.size());
        assertEquals(Sets.newHashSet(44),
                vlanKeys.stream().map(vlan -> vlan.getVlanId().getValue()).collect(Collectors.toSet()));

        vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT, "xzy");
        assertEquals(0, vlanKeys.size());

        vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT, "FRINXtest");
        assertEquals(2, vlanKeys.size());
        assertEquals(Sets.newHashSet(666, 667),
                vlanKeys.stream().map(vlan -> vlan.getVlanId().getValue()).collect(Collectors.toSet()));

        vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT_EMPTY, "abcd");
        assertEquals(0, vlanKeys.size());
    }
}