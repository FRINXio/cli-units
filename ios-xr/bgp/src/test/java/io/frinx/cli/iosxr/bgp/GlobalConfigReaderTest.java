/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp;

import io.frinx.cli.iosxr.bgp.handler.GlobalConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;

public class GlobalConfigReaderTest {

    private final String output = "Thu Feb 22 22:59:47.601 UTC\n" +
            "router bgp 1 instance inst\n" +
            "router bgp 65505 instance test\n" +
            "router bgp 1";

    @Test
    public void testGlobal() {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalConfigReader.parseAs(output, "inst", builder);
        Assert.assertEquals(Long.valueOf(1), builder.getAs().getValue());

        builder = new ConfigBuilder();
        GlobalConfigReader.parseDefaultAs(output,  builder);
        Assert.assertEquals(Long.valueOf(1), builder.getAs().getValue());

        builder = new ConfigBuilder();
        GlobalConfigReader.parseAs(output, "test", builder);
        Assert.assertEquals(Long.valueOf(65505), builder.getAs().getValue());
    }
}
