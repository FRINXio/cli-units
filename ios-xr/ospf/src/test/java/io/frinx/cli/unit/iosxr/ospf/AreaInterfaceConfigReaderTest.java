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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceConfigReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;

class AreaInterfaceConfigReaderTest {

    private final String enablePassive = """
            Thu Dec 21 15:40:02.857 UTC
            router ospf 100
             area 0
              interface Loopback97
               cost 1
               passive enable
              !
             !
            !
            """;

    private final String disablePassive = """
            Thu Dec 21 15:40:02.857 UTC
            router ospf 100
             area 0
              interface Loopback97
               cost 1
               passive disable
              !
             !
            !
            """;

    @Test
    void testPassiveEnable() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(enablePassive, builder);
        assertEquals(Integer.valueOf(1), builder.getMetric().getValue());
        AreaInterfaceConfigReader.parsePassive(enablePassive, builder);
        assertTrue(builder.isPassive());
    }

    @Test
    void testPassiveDisable() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(disablePassive, builder);
        assertEquals(Integer.valueOf(1), builder.getMetric().getValue());
        AreaInterfaceConfigReader.parsePassive(disablePassive, builder);
        assertFalse(builder.isPassive());
    }
}
