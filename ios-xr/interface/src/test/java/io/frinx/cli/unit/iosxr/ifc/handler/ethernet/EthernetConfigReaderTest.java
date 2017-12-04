/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;


import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;

public class EthernetConfigReaderTest {

    private static final String SH_INT_CONFIG = "Mon Nov 27 10:22:55.365 UTC\n" +
            "interface GigabitEthernet0/0/0/3\n" +
            " bundle id 200 mode on\n" +
            " shutdown\n" +
            "!\n" +
            "\n";

    private static Config EXPECTED_CONFIG = new ConfigBuilder()
            .addAugmentation(Config1.class, new Config1Builder().setAggregateId("Bundle-Ether200").build())
            .build();

    private static final String SH_INT_CONFIG_NOT_CONFIGURED_BUNDLE_ID = "Mon Nov 27 10:30:39.554 UTC\n" +
            "interface GigabitEthernet0/0/0/2\n" +
            " shutdown\n" +
            "!\n" +
            "\n";

    private static Config EXPECTED_CONFIG_NOT_CONFIGURED_BUNDLE_ID = new ConfigBuilder()
            .build();


    @Test
    public void testParseEthernetConfig() {
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig(SH_INT_CONFIG, actualConfigBuilder);

        Assert.assertEquals(EXPECTED_CONFIG, actualConfigBuilder.build());

        actualConfigBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig(SH_INT_CONFIG_NOT_CONFIGURED_BUNDLE_ID, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_CONFIG_NOT_CONFIGURED_BUNDLE_ID, actualConfigBuilder.build());
    }
}