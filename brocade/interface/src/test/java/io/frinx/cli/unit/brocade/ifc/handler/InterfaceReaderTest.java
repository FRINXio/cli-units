/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.ifc.handler;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = "GigabitEthernet1/1 is up, line protocol is up \n" +
            "GigabitEthernet1/2 is up, line protocol is up \n" +
            "GigabitEthernet3/20 is down, line protocol is down \n" +
            "10GigabitEthernet4/1 is up, line protocol is up \n" +
            "10GigabitEthernet4/2 is up, line protocol is up \n" +
            "Ethernetmgmt1 is down, line protocol is down \n" +
            "Ve3 is down, line protocol is down \n" +
            "Ve210 is down, line protocol is down \n" +
            "Loopback1 is up, line protocol is up \n" +
            "Loopback2 is up, line protocol is up \n";

    @Test
    public void testAllIds() throws Exception {
        List<InterfaceKey> interfaceKeys = InterfaceReader.parseAllInterfaceIds(OUTPUT);
        assertThat(interfaceKeys, hasItem(new InterfaceKey("10GigabitEthernet4/2")));
    }
}