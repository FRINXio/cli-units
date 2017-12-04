/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

public class InterfaceConfigReaderTest {

    private static final String SH_RUN_INT = "Fri Nov 24 11:50:02.834 UTC\n" +
            "interface Loopback0\n" +
            " ipv4 address 99.0.0.3 255.255.255.255\n" +
            " ipv6 address fe80::260:3eff:fe11:6770 link-local\n" +
            " shutdown\n" +
            "!\n" +
            "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setName("Loopback0")
            .setEnabled(false)
            .setType(SoftwareLoopback.class)
            .build();

    private static final String SH_RUN_INT2 = "Fri Nov 24 11:56:59.795 UTC\n" +
            "interface GigabitEthernet0/0/0/0\n" +
            " description example\n" +
            " mtu 1500\n" +
            "!\n" +
            "\n";

    private static final Config EXPECTED_CONFIG2 = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0/0")
            .setEnabled(true)
            .setDescription("example")
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    @Test
    public void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        InterfaceConfigReader.parseInterface(SH_RUN_INT, actualConfig, "Loopback0");
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());

        ConfigBuilder actualConfig2 = new ConfigBuilder();
        InterfaceConfigReader.parseInterface(SH_RUN_INT2, actualConfig2, "GigabitEthernet0/0/0/0");
        Assert.assertEquals(EXPECTED_CONFIG2, actualConfig2.build());
    }
}