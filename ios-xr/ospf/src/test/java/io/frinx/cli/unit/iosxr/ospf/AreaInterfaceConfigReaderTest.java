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

package io.frinx.cli.unit.iosxr.ospf;

import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;

public class AreaInterfaceConfigReaderTest {

    private final String enablePassive = "Thu Dec 21 15:40:02.857 UTC\n"
            + "router ospf 100\n"
            + " area 0\n"
            + "  interface Loopback97\n"
            + "   cost 1\n"
            + "   passive enable\n"
            + "  !\n"
            + " !\n"
            + "!\n";

    private final String disablePassive = "Thu Dec 21 15:40:02.857 UTC\n"
            + "router ospf 100\n"
            + " area 0\n"
            + "  interface Loopback97\n"
            + "   cost 1\n"
            + "   passive disable\n"
            + "  !\n"
            + " !\n"
            + "!\n";

    @Test
    public void testPassiveEnable() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(enablePassive, builder);
        Assert.assertEquals(Integer.valueOf(1), builder.getMetric().getValue());
        AreaInterfaceConfigReader.parsePassive(enablePassive, builder);
        Assert.assertTrue(builder.isPassive());
    }

    @Test
    public void testPassiveDisable() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(disablePassive, builder);
        Assert.assertEquals(Integer.valueOf(1), builder.getMetric().getValue());
        AreaInterfaceConfigReader.parsePassive(disablePassive, builder);
        Assert.assertFalse(builder.isPassive());
    }
}
