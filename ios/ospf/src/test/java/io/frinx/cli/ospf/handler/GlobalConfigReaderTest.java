/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;

public class GlobalConfigReaderTest {

    private static final String OUTPUT = "router ospf 99\r\n" +
            " router-id 5.5.5.5\r\n" +
            " router-id 5.5.5.6\r\n" +
            "router ospf 3737\n" +
            " router-id 99.6.7.4\n" +
            " bgp router-id 5.5.5.5\n";

    @Test
    public void testParse() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalConfigReader.parseGlobal(OUTPUT, builder, "99");
        Assert.assertEquals(getConfig("5.5.5.5"), builder.build());

        builder = new ConfigBuilder();
        GlobalConfigReader.parseGlobal(OUTPUT, builder, "3737");
        Assert.assertEquals(getConfig("99.6.7.4"), builder.build());

        builder = new ConfigBuilder();
        GlobalConfigReader.parseGlobal(OUTPUT, builder, "8888");
        Assert.assertEquals(new ConfigBuilder().build(), builder.build());
    }

    static Config getConfig(String rd) {
        return new ConfigBuilder()
                        .setRouterId(new DottedQuad(rd))
                        .build();
    }
}