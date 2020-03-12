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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol.Disposition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocolKey;

public class L2VSIConfigCftCtrlProtocolsReaderTest {

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
        List<CtrlProtocolKey> allIds = Arrays.asList(
                new CtrlProtocolKey(Name.CiscoCdp), new CtrlProtocolKey(Name.CiscoDtp),
                new CtrlProtocolKey(Name.CiscoPagp), new CtrlProtocolKey(Name.CiscoUdld),
                new CtrlProtocolKey(Name.CiscoVtp), new CtrlProtocolKey(Name.CiscoPvst),
                new CtrlProtocolKey(Name.CiscoStpUplinkFast), new CtrlProtocolKey(Name.VlanBridge),
                new CtrlProtocolKey(Name.Xstp), new CtrlProtocolKey(Name.Lacp),
                new CtrlProtocolKey(Name.LacpMarker), new CtrlProtocolKey(Name.Oam),
                new CtrlProtocolKey(Name.Lldp), new CtrlProtocolKey(Name._8021x),
                new CtrlProtocolKey(Name.Gmrp), new CtrlProtocolKey(Name.Gvrp),
                new CtrlProtocolKey(Name.BridgeBlock), new CtrlProtocolKey(Name.AllBridgesBlock),
                new CtrlProtocolKey(Name.GarpBlock));

        Assert.assertEquals(allIds, L2VSIConfigCftProtocolsReader.getAllIds(OUTPUT, "L2Test2"));
    }

    @Test
    public void setProtocolTest() {
        buildAndTest(Name.CiscoCdp, Disposition.FORWARD);
        buildAndTest(Name.CiscoStpUplinkFast, Disposition.DISCARD);
        buildAndTest(Name.LacpMarker, Disposition.DISCARD);
        buildAndTest(Name.GarpBlock, Disposition.DISCARD);
    }

    private void buildAndTest(Name protocolName, Disposition expected) {
        CtrlProtocolBuilder builder = new CtrlProtocolBuilder();
        L2VSIConfigCftProtocolsReader.setProtocol(OUTPUT, protocolName, "L2Test2", builder);

        Assert.assertEquals(expected, builder.getDisposition());
    }
}
