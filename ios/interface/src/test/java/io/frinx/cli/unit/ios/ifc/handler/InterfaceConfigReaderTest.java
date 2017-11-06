/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    private static final Config EXPECTED_INTERFACE = new ConfigBuilder()
            .setEnabled(false)
            .setName("GigabitEthernet1/0")
            .setType(EthernetCsmacd.class)
            .setDescription("asd fdsas'; dsa;d;fa'")
            .setMtu(1530)
            .build();
    private static final String SH_INTERFACE_RUN = "interface GigabitEthernet1/0\n" +
            " mtu 1530\n" +
            " no ip address\n" +
            " shutdown\n" +
            " negotiation auto\n" +
            " description asd fdsas'; dsa;d;fa'\n" +
            "end\n";


    private static final Config EXPECTED_INTERFACE2 = new ConfigBuilder()
            .setEnabled(true)
            .setName("FastEthernet0/0")
            .setType(EthernetCsmacd.class)
            .build();
    private static final String SH_INTERFACE_RUN2 = "interface FastEthernet0/0\n" +
            " ip address 192.168.56.121 255.255.255.0\n" +
            " duplex full\n" +
            "end\n\n";

    @Test
    public void testParseInterface() throws Exception {
        ConfigBuilder parsed = new ConfigBuilder();
        InterfaceConfigReader.parseInterface(SH_INTERFACE_RUN, parsed, "GigabitEthernet1/0");
        assertEquals(EXPECTED_INTERFACE, parsed.build());

        parsed = new ConfigBuilder();
        InterfaceConfigReader.parseInterface(SH_INTERFACE_RUN2, parsed, "FastEthernet0/0");
        assertEquals(EXPECTED_INTERFACE2, parsed.build());
    }

}