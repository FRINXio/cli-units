/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.aspath;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder;

public class AsPathPrependConfigReaderTest {

    private static final String ZERO_REPEATS = "route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10 \n"
            + " set local-preference 9888\n";

    private static final String ONE_REPEAT = "route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222\n";

    private static final String FOUR_REPEATS = "route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222 65222 65222 65222\n";

    @Test
    public void testZeroRepeats() {
        ConfigBuilder builder = new ConfigBuilder();
        AsPathPrependConfigReader.parseConfig(ZERO_REPEATS, builder);
        Assert.assertNull(builder.getAsn());
        Assert.assertNull(builder.getRepeatN());
    }

    @Test
    public void parseOneRepeat() {
        ConfigBuilder builder = new ConfigBuilder();
        AsPathPrependConfigReader.parseConfig(ONE_REPEAT, builder);
        Assert.assertEquals("65222", builder.getAsn().getValue().toString());
        Assert.assertEquals("1", builder.getRepeatN().toString());
    }

    @Test
    public void parseFourRepeats() {
        ConfigBuilder builder = new ConfigBuilder();
        AsPathPrependConfigReader.parseConfig(FOUR_REPEATS, builder);
        Assert.assertEquals("65222", builder.getAsn().getValue().toString());
        Assert.assertEquals("4", builder.getRepeatN().toString());
    }

}
