/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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