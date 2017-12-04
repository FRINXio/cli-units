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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.hold.time.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.hold.time.ConfigBuilder;

public class HoldTimeConfigReaderTest {

    private static final String SH_RUN_INTERFACE = "Fri Nov 24 14:29:00.530 UTC\n" +
            "interface GigabitEthernet0/0/0/5\n" +
            " carrier-delay up 100 down 0\n" +
            " shutdown\n" +
            "!\n" +
            "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setUp(100L)
            .setDown(0L)
            .build();

    private static final String SH_RUN_INTERFACE2 = "Fri Nov 24 14:29:00.530 UTC\n" +
            "interface GigabitEthernet0/0/0/4\n" +
            " shutdown\n" +
            " load-interval 0\n" +
            " dampening\n" +
            "!\n" +
            "\n";

    private static final Config EXPECTED_CONFIG2 = new ConfigBuilder()

            .build();

    @Test
    public void testParseHoldTime() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        HoldTimeConfigReader.parseHoldTime(SH_RUN_INTERFACE, actualConfig);

        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());

        ConfigBuilder actualConfig2 = new ConfigBuilder();
        HoldTimeConfigReader.parseHoldTime(SH_RUN_INTERFACE2, actualConfig2);

        Assert.assertEquals(EXPECTED_CONFIG2, actualConfig2.build());
    }
}