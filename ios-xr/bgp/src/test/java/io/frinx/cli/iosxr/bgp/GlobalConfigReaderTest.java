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
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;

public class GlobalConfigReaderTest {

    private final String output = "Fri Nov 24 13:51:29.234 UTC\n" +
            "1   bgp2_1      default             2525      0       none";

    @Test
    public void testGlobal() {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalConfigReader.parseAs(output, builder);
        Assert.assertEquals(Long.valueOf(2525), builder.getAs().getValue());
    }
}
