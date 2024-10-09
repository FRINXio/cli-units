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

package io.frinx.cli.unit.ios.routing.policy.handlers.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ExtCommunitySetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpExtCommunityType;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class ExtCommunitySetReaderTest {

    private static final String SH_IP_VRF = """
            ip vrf vrf1
             rd 65000:101
             route-target export 100:1000
             route-target export 100:1001
             route-target export 100:1002
             route-target export 100:1003
             route-target export 100:1004
             route-target export 100:1005
             route-target export 100:1006
             route-target export 100:1007
             route-target export 100:1008
             route-target export 100:1009
             route-target import 100:1000
             route-target import 100:1001
             route-target import 100:1002
            ip vrf vrf2
             rd 65000:102
             route-target export 200:1000
             route-target export 200:1001
             route-target export 200:1002
             route-target export 200:1003
             route-target export 200:1004
             route-target export 200:1005
             route-target export 200:1006
             route-target export 200:1007
             route-target export 200:1008
             route-target export 200:1009
             route-target import 200:1000
             route-target import 200:1001
             route-target import 200:1002""";

    private static final String SH_IP_VRF1 = """
            ip vrf vrf1
             rd 65000:101
             route-target export 100:1000
             route-target export 100:1001
             route-target export 100:1002
             route-target export 100:1003
             route-target export 100:1004
             route-target export 100:1005
             route-target export 100:1006
             route-target export 100:1007
             route-target export 100:1008
             route-target export 100:1009
             route-target import 100:1000
             route-target import 100:1001
             route-target import 100:1002
            """;

    private static final String SH_IP_VRF2 = """
            ip vrf vrf2
             rd 65000:102
             route-target export 200:1000
            """;
    private static final String SH_IP_VRF3 = """
            ip vrf vrf3
             rd 65000:102
             route-target import 200:1000
            """;

    private static final List<String> VRF_RESULT = Arrays.asList("vrf1", "vrf2");
    private static final String VRF_1 = "vrf1";
    private static final String VRF_2 = "vrf2";
    private static final String VRF_3 = "vrf3";
    private static final List<ExtCommunitySetKey> VRF1_RESULT = Arrays.asList(new ExtCommunitySetKey(VRF_1
            + ExtCommunitySetReader.ROUTE_TARGET_EXPORT_SET), new ExtCommunitySetKey(VRF_1
            + ExtCommunitySetReader.ROUTE_TARGET_IMPORT_SET));

    private static final List<ExtCommunitySetKey> VRF2_RESULT = Collections.singletonList(new ExtCommunitySetKey(VRF_2
            + ExtCommunitySetReader.ROUTE_TARGET_EXPORT_SET));
    private static final List<ExtCommunitySetKey> VRF3_RESULT = Collections.singletonList(new ExtCommunitySetKey(VRF_3
            + ExtCommunitySetReader.ROUTE_TARGET_IMPORT_SET));
    private static final KeyedInstanceIdentifier<ExtCommunitySet, ExtCommunitySetKey> EXPORT_IID =
            io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EXTCOMMUNITYSETS
            .child(ExtCommunitySet.class, new ExtCommunitySetKey(VRF_1
                    + ExtCommunitySetReader.ROUTE_TARGET_EXPORT_SET));
    private static final KeyedInstanceIdentifier<ExtCommunitySet, ExtCommunitySetKey> IMPORT_IID =
            io.frinx.openconfig.openconfig.bgp.IIDs.RO_DE_AUG_DEFINEDSETS2_BG_EXTCOMMUNITYSETS
            .child(ExtCommunitySet.class, new ExtCommunitySetKey(VRF_1
                    + ExtCommunitySetReader.ROUTE_TARGET_IMPORT_SET));
    private static final List<ExtCommunitySetConfig.ExtCommunityMember> RT_EXP_RESULT = Arrays.asList(new
            ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1000")), new ExtCommunitySetConfig
            .ExtCommunityMember(new BgpExtCommunityType("100:1001")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1002")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1003")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1004")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1005")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1006")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1007")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1008")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1009")));

    private static final List<ExtCommunitySetConfig.ExtCommunityMember> RT_IMP_RESULT = Arrays.asList(new
            ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1000")), new ExtCommunitySetConfig
            .ExtCommunityMember(new BgpExtCommunityType("100:1001")),
            new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1002")));

    @Test
    void parseExtCommunityIdsTest() {
        assertFalse(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF1, VRF_1)
                .isEmpty());
        assertFalse(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF2, VRF_2)
                .isEmpty());
        assertFalse(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF3, VRF_3)
                .isEmpty());
        assertTrue(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF3, "NONEXISTING")
                .isEmpty());

        assertTrue(VRF1_RESULT.containsAll(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF1, VRF_1)));
        assertTrue(VRF2_RESULT.containsAll(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF2, VRF_2)));
        assertTrue(VRF3_RESULT.containsAll(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF3, VRF_3)));
    }

    @Test
    void parseConfigTest() {

        final List<ExtCommunitySetConfig.ExtCommunityMember> expCommunityMember
                = ExtCommunitySetReader.parseConfig(SH_IP_VRF1, EXPORT_IID)
                .getExtCommunityMember();

        final List<ExtCommunitySetConfig.ExtCommunityMember> impCommunityMember
                = ExtCommunitySetReader.parseConfig(SH_IP_VRF1, IMPORT_IID)
                .getExtCommunityMember();


        assertNotNull(expCommunityMember);
        assertFalse(expCommunityMember.isEmpty());
        assertTrue(RT_EXP_RESULT.containsAll(expCommunityMember));


        assertNotNull(impCommunityMember);
        assertFalse(impCommunityMember.isEmpty());
        assertTrue(RT_IMP_RESULT.containsAll(impCommunityMember));

    }

    @Test
    void getVrfNameTest() {
        assertEquals(VRF_1, ExtCommunitySetReader.getVrfName(EXPORT_IID.getKey())
                .get());
    }
}
