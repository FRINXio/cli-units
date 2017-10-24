/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.lldp.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;

public class NeighborStateReaderTest {

    private static final String IOS_OUTPUT = "Port id: Gi0/0/0/1\n" +
            "System Name: PE3.demo.frinx.io\n" +
            "Port id: Fa0/1\n" +
            "System Name: XE2.FRINX\n" +
            "Port id: Fa0/0\n" +
            "System Name: XE1.FRINX\n" +
            "Port id: Gi0/0/0/3\n" +
            "System Name: PE1.demo.frinx.io";

    private static final State IOS_EXPECTED1 = new StateBuilder()
            .setId("PE3.demo.frinx.io")
            .setPortId("Gi0/0/0/1")
            .build();

    private static final State IOS_EXPECTED2 = new StateBuilder()
            .setId("PE1.demo.frinx.io")
            .setPortId("Gi0/0/0/3")
            .build();

    @Test
    public void testParseNeighborStateFields() {
        StateBuilder stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(IOS_OUTPUT, "PE3.demo.frinx.io", stateBuilder);
        assertEquals(IOS_EXPECTED1, stateBuilder.build());

        stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(IOS_OUTPUT, "PE1.demo.frinx.io", stateBuilder);
        assertEquals(IOS_EXPECTED2, stateBuilder.build());
    }

}