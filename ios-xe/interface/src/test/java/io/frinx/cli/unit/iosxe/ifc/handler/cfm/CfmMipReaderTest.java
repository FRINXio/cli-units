/*
 * Copyright © 2021 Frinx and others.
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


package io.frinx.cli.unit.iosxe.ifc.handler.cfm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.mip.LevelBuilder;

class CfmMipReaderTest {
    private static final String OUTPUT = """
            interface GigabitEthernet0/15
             port-type nni
             switchport port-security aging time 5
             ethernet cfm mip level 2 vlan 2702-2703
            """;

    @Test
    void getAllLevelsTest() {
        LevelBuilder builder = new LevelBuilder();
        CfmMipReader reader = new CfmMipReader(Mockito.mock(Cli.class));

        List<String> expected = Arrays.asList("2702", "2703");
        reader.parseLevelMip((short) 2, OUTPUT, builder);
        assertEquals(expected, builder.getVlan());
    }
}
