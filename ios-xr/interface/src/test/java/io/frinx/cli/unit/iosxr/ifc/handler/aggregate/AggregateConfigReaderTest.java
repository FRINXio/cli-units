/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.ConfigBuilder;

public class AggregateConfigReaderTest {

    private static String SH_RUN_INT = "Mon Nov 27 13:03:17.446 UTC\n" +
            "interface Bundle-Ether4\n" +
            " bundle minimum-active links 30\n" +
            "!\n" +
            "\n";

    private static Config EXPECTED_CONFIG = new ConfigBuilder()
            .setMinLinks(30)
            .build();

    private static String SH_RUN_INT_NO_BUNDLE_CONFIG = "Mon Nov 27 13:08:49.703 UTC\n" +
            "interface Bundle-Ether6\n" +
            "!\n" +
            "\n";

    private static Config EXPECTED_NO_BUNDLE_CONFIG = new ConfigBuilder()
            .build();

    @Test
    public void testParseAggregateConfig() {
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        AggregateConfigReader.parseAggregateConfig(SH_RUN_INT, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_CONFIG, actualConfigBuilder.build());

        actualConfigBuilder = new ConfigBuilder();
        AggregateConfigReader.parseAggregateConfig(SH_RUN_INT_NO_BUNDLE_CONFIG, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_NO_BUNDLE_CONFIG, actualConfigBuilder.build());
    }
}