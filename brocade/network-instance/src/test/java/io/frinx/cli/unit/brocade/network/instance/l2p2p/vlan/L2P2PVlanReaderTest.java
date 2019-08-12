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

import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;

public class L2P2PVlanReaderTest {

    private static final String OUTPUT = "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + " vll 99999 5\n"
            + " vll FRINXtest 66661446\n"
            + "  vlan 666\n"
            + "  vlan 667\n"
            + " vll RADTEST 178\n"
            + "  vlan 1551\n"
            + " vll Test_111 666111\n"
            + " vll abcd2 45672\n"
            + "  vlan 44\n"
            + " vll frinxtest 143124141\n"
            + " vll testL2P2p 1000009999\n"
            + " vll testservis 1\n"
            + " vll xyz 111\n"
            + " vll-local DIR0001415\n"
            + "  vlan 100\n"
            + " vll-local MESSUNG\n"
            + "  vlan 1599\n"
            + " vll-local TEST-fuer-NOC\n"
            + "  vlan 20\n"
            + " vll-local Test_3903\n"
            + "  vlan 1500\n"
            + " vll-local loopvlan\n"
            + "  vlan 100\n"
            + " vll-local test12\n"
            + "  vlan 12\n"
            + " vll-local testtest\n"
            + "  vlan 12\n"
            + " vll-local xyz\n"
            + "  vlan 33\n"
            + " vll-local xzy\n"
            + " vpls managementvpls 12 \n"
            + "  vlan 11\n"
            + " vpls mgmtma14 14 \n"
            + "  vlan 14\n"
            + " vpls WBNWLC-Management 61 \n"
            + "  vlan 2\n"
            + " vpls WBN-APMgmt-SIM 67 \n"
            + "  vlan 3123\n"
            + " vpls WBN-Mgmt-SIM 98 \n"
            + "  vlan 23\n"
            + " vpls WBN-blizz-BB 328 \n"
            + "  vlan 130\n"
            + " vpls WBN-DMZ 338 \n"
            + "  vlan 60\n"
            + " vpls WBN-Alarmanl 339 \n"
            + "  vlan 103\n"
            + " vpls WBN-Mgmt-MI 457 \n"
            + "  vlan 18\n"
            + " vpls WBN-Mgmt-MI-2 458 \n"
            + "  vlan 118\n"
            + " vpls WBN-APMgmt-MI 459 \n"
            + "  vlan 3118\n"
            + " vpls WBN-Mgmt-Etherlink 460 \n"
            + "  vlan 25\n"
            + " vpls WBN-APMgmt-SUED 461 \n"
            + "  vlan 3127\n"
            + " vpls WBN-Mgmt-SUED 462 \n"
            + "  vlan 27\n"
            + " vpls WBN_LS 483 \n"
            + "  vlan 100\n"
            + " vpls WBN_LS_DN2 484 \n"
            + "  vlan 101\n"
            + " vpls WBN_VW 485 \n"
            + "  vlan 200\n"
            + " vpls WBN_VW_DN2 486 \n"
            + "  vlan 201\n"
            + " vpls WLAN-AP-Management 687 \n"
            + "  vlan 5\n"
            + " vpls Blizznet-WLAN-User-200 688 \n"
            + "  vlan 200\n"
            + "  vlan 900\n"
            + " vpls Blizznet-WLAN-User-201 689 \n"
            + "  vlan 201\n"
            + " vpls frtest 999 \n"
            + "  vlan 10\n"
            + " vpls secure-mgmt-vlan101 1113 \n"
            + "  vlan 101\n"
            + " vpls CPE-Test 1501 \n"
            + "  vlan 1501\n"
            + " vpls VLAN666-10.200.5.0 6571 \n"
            + "  vlan 666\n"
            + " vpls MAGENTATEST 8888 \n"
            + "  vlan 2000\n"
            + " vpls vlan128-test-labor 11211 \n"
            + "  vlan 128\n"
            + " vpls FRINXTEST666 666666 \n"
            + "  vlan 666\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "!\n";

    private static final String OUTPUT_EMPTY = "!\n";


    @Test
    public void parseVlans() {
        List<VlanKey> vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT, "abcd2");
        Assert.assertEquals(1, vlanKeys.size());
        Assert.assertEquals(Sets.newHashSet(44),
                vlanKeys.stream().map(vlan -> vlan.getVlanId().getValue()).collect(Collectors.toSet()));

        vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT, "xzy");
        Assert.assertEquals(0, vlanKeys.size());

        vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT, "FRINXtest");
        Assert.assertEquals(2, vlanKeys.size());
        Assert.assertEquals(Sets.newHashSet(666, 667),
                vlanKeys.stream().map(vlan -> vlan.getVlanId().getValue()).collect(Collectors.toSet()));

        vlanKeys = L2P2PVlanReader.parseVlans(OUTPUT_EMPTY, "abcd");
        Assert.assertEquals(0, vlanKeys.size());
    }
}