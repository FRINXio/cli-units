/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.iosxr.bgp;

import io.frinx.cli.iosxr.bgp.handler.GlobalConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;

public class GlobalConfigReaderTest {

    private final String output = "Thu Feb 22 22:59:47.601 UTC\n"
            + "router bgp 1 instance inst\n"
            + "router bgp 65505 instance test\n"
            + "router bgp 1";

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
