/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.ProtocolKey;

public class L2CftProfileProtocolReaderTest {

    private static final String OUTPUT =
            "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-cdp untagged-disposition forward\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-dtp untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-pagp untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-udld untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-vtp untagged-disposition forward\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-pvst untagged-disposition forward\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol cisco-stp-uplink-fast untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol vlan-bridge untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol xstp untagged-disposition forward\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol lacp untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol lacp-marker untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol oam untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol lldp untagged-disposition forward\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol 802.1x untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol gmrp untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol gvrp untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol bridge-block untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol all-bridges-block untagged-disposition discard\n"
            + "l2-cft protocol add profile L2Test2 ctrl-protocol garp-block untagged-disposition discard";

    @Test
    public void getAllIdsTest() {
        List<ProtocolKey> allIds = Arrays.asList(
                new ProtocolKey(Name.CiscoCdp.getName()), new ProtocolKey(Name.CiscoDtp.getName()),
                new ProtocolKey(Name.CiscoPagp.getName()), new ProtocolKey(Name.CiscoUdld.getName()),
                new ProtocolKey(Name.CiscoVtp.getName()), new ProtocolKey(Name.CiscoPvst.getName()),
                new ProtocolKey(Name.CiscoStpUplinkFast.getName()), new ProtocolKey(Name.VlanBridge.getName()),
                new ProtocolKey(Name.Xstp.getName()), new ProtocolKey(Name.Lacp.getName()),
                new ProtocolKey(Name.LacpMarker.getName()), new ProtocolKey(Name.Oam.getName()),
                new ProtocolKey(Name.Lldp.getName()), new ProtocolKey(Name._8021x.getName()),
                new ProtocolKey(Name.Gmrp.getName()), new ProtocolKey(Name.Gvrp.getName()),
                new ProtocolKey(Name.BridgeBlock.getName()), new ProtocolKey(Name.AllBridgesBlock.getName()),
                new ProtocolKey(Name.GarpBlock.getName()));

        Assert.assertEquals(allIds, L2CftProfileProtocolReader.getAllIds(OUTPUT));
    }
}
