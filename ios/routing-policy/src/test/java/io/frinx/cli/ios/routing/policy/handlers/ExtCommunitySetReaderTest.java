/*
 * Copyright (c) 2017 Cisco Systems. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.routing.policy.handlers;

import static io.frinx.cli.ios.routing.policy.handlers.ExtCommunitySetReader.ROUTE_TARGET_EXPORT_SET;
import static io.frinx.cli.ios.routing.policy.handlers.ExtCommunitySetReader.ROUTE_TARGET_IMPORT_SET;

import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.DefinedSets2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ExtCommunitySetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.routing.policy.defined.sets.BgpDefinedSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpExtCommunityType;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class ExtCommunitySetReaderTest {

    private static final String SH_IP_VRF = "ip vrf vrf1\n" + " rd 65000:101\n" + " route-target export 100:1000\n"
        + " route-target export 100:1001\n" + " route-target export 100:1002\n" + " route-target export 100:1003\n"
        + " route-target export 100:1004\n" + " route-target export 100:1005\n" + " route-target export 100:1006\n"
        + " route-target export 100:1007\n" + " route-target export 100:1008\n" + " route-target export 100:1009\n"
        + " route-target import 100:1000\n" + " route-target import 100:1001\n" + " route-target import 100:1002\n"
        + "ip vrf vrf2\n" + " rd 65000:102\n" + " route-target export 200:1000\n" + " route-target export 200:1001\n"
        + " route-target export 200:1002\n" + " route-target export 200:1003\n" + " route-target export 200:1004\n"
        + " route-target export 200:1005\n" + " route-target export 200:1006\n" + " route-target export 200:1007\n"
        + " route-target export 200:1008\n" + " route-target export 200:1009\n" + " route-target import 200:1000\n"
        + " route-target import 200:1001\n" + " route-target import 200:1002";

    private static final String SH_IP_VRF1 = "ip vrf vrf1\n" + " rd 65000:101\n" + " route-target export 100:1000\n"
        + " route-target export 100:1001\n" + " route-target export 100:1002\n" + " route-target export 100:1003\n"
        + " route-target export 100:1004\n" + " route-target export 100:1005\n" + " route-target export 100:1006\n"
        + " route-target export 100:1007\n" + " route-target export 100:1008\n" + " route-target export 100:1009\n"
        + " route-target import 100:1000\n" + " route-target import 100:1001\n" + " route-target import 100:1002\n";

    private static final String SH_IP_VRF2 = "ip vrf vrf2\n" + " rd 65000:102\n" + " route-target export 200:1000\n";
    private static final String SH_IP_VRF3 = "ip vrf vrf3\n" + " rd 65000:102\n" + " route-target import 200:1000\n";

    private static final List<String> VRF_RESULT = Arrays.asList("vrf1", "vrf2");
    private static final String VRF_1 = "vrf1";
    private static final String VRF_2 = "vrf2";
    private static final String VRF_3 = "vrf3";
    private static final List<ExtCommunitySetKey> VRF1_RESULT = Arrays.asList(
        new ExtCommunitySetKey(VRF_1 + ROUTE_TARGET_EXPORT_SET),
        new ExtCommunitySetKey(VRF_1 + ROUTE_TARGET_IMPORT_SET));

    private static final List<ExtCommunitySetKey> VRF2_RESULT = Collections.singletonList(
        new ExtCommunitySetKey(VRF_2 + ROUTE_TARGET_EXPORT_SET));
    private static final List<ExtCommunitySetKey> VRF3_RESULT = Collections.singletonList(
        new ExtCommunitySetKey(VRF_3 + ROUTE_TARGET_IMPORT_SET));
    private static final KeyedInstanceIdentifier<ExtCommunitySet, ExtCommunitySetKey> EXPORT_IID =
        IIDs.RO_DEFINEDSETS.augmentation(DefinedSets2.class)
        .child(BgpDefinedSets.class)
        .child(ExtCommunitySets.class)
        .child(ExtCommunitySet.class, new ExtCommunitySetKey(VRF_1 + ROUTE_TARGET_EXPORT_SET));
    private static final KeyedInstanceIdentifier<ExtCommunitySet, ExtCommunitySetKey> IMPORT_IID =
        IIDs.RO_DEFINEDSETS.augmentation(DefinedSets2.class)
        .child(BgpDefinedSets.class)
        .child(ExtCommunitySets.class)
        .child(ExtCommunitySet.class, new ExtCommunitySetKey(VRF_1 + ROUTE_TARGET_IMPORT_SET));
    private static final List<ExtCommunitySetConfig.ExtCommunityMember> RT_EXP_RESULT = Arrays.asList(
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1000")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1001")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1002")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1003")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1004")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1005")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1006")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1007")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1008")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1009")));

    private static final List<ExtCommunitySetConfig.ExtCommunityMember> RT_IMP_RESULT = Arrays.asList(
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1000")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1001")),
        new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType("100:1002")));

    @Test
    public void parseExtCommunityIdsTest(){
        Assert.assertFalse(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF1, VRF_1).isEmpty());
        Assert.assertFalse(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF2, VRF_2).isEmpty());
        Assert.assertFalse(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF3, VRF_3).isEmpty());

        Assert.assertTrue(VRF1_RESULT.containsAll(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF1, VRF_1)));
        Assert.assertTrue(VRF2_RESULT.containsAll(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF2, VRF_2)));
        Assert.assertTrue(VRF3_RESULT.containsAll(ExtCommunitySetReader.parseExtCommunityIds(SH_IP_VRF3, VRF_3)));
    }

    @Test
    public void parseConfigTest() {

        List<ExtCommunitySetConfig.ExtCommunityMember> expCommunityMember =
            ExtCommunitySetReader.parseConfig(SH_IP_VRF1, EXPORT_IID).getExtCommunityMember();

        List<ExtCommunitySetConfig.ExtCommunityMember> impCommunityMember =
            ExtCommunitySetReader.parseConfig(SH_IP_VRF1, IMPORT_IID).getExtCommunityMember();


        Assert.assertNotNull(expCommunityMember);
        Assert.assertFalse(expCommunityMember.isEmpty());
        Assert.assertTrue(RT_EXP_RESULT.containsAll(expCommunityMember));


        Assert.assertNotNull(impCommunityMember);
        Assert.assertFalse(impCommunityMember.isEmpty());
        Assert.assertTrue(RT_IMP_RESULT.containsAll(impCommunityMember));

    }

    @Test
    public void getVrfNameTest(){
        Assert.assertEquals(VRF_1, ExtCommunitySetReader.getVrfName(EXPORT_IID.getKey()).get());
    }
}
