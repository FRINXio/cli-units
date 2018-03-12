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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.cp;

import java.util.Optional;
import org.junit.Test;

public class ConnectionPointsReaderTest {

    public static final String VLL_LOCAL_OUTPUT = "VLL DIR0001415 VLL-ID 3 IFL-ID --\n" +
            "  State: DOWN - endpoint port is down\n" +
            "  End-point 1:      tagged  vlan 100   e 3/15        \n" +
            "                    COS: --              \n" +
            "  End-point 2:      untagged  e 3/14    \n" +
            "                    COS: --              \n" +
            "  Extended Counters: Enabled\n";

    @Test
    public void testExtractVllLocalIfc() throws Exception {
        Optional<String> ifc = ConnectionPointsReader.extractVllLocalIfc(VLL_LOCAL_OUTPUT,
                ConnectionPointsReader.ENDPOINT_1_LINE,
                ConnectionPointsReader.VLL_LOCAL_IFC_LINE,
                m -> m.group("ifc"));

        ifc = ConnectionPointsReader.extractVllLocalIfc(VLL_LOCAL_OUTPUT,
                ConnectionPointsReader.ENDPOINT_1_LINE,
                ConnectionPointsReader.VLL_LOCAL_SUBIFC_LINE,
                m -> m.group("ifc"));

        System.err.println(ifc);
    }


}