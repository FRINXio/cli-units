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

package io.frinx.cli.unit.ios.lldp.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;

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