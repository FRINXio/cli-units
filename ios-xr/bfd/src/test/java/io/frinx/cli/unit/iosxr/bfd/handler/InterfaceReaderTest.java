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

package io.frinx.cli.unit.iosxr.bfd.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces.InterfaceKey;

class InterfaceReaderTest {

    private static final String SHOW_RUNNING_INTERFACES = """
            Mon Nov 27 14:04:50.483 UTC
            interface GigabitEthernet0/0/0/0 shutdowninterface Bundle-Ether2
             description testt
             bfd mode ietf
             bfd address-family ipv4 fast-detect
            !
            interface Bundle-Ether3
             bfd address-family ipv4 fast-detect
             bfd address-family ipv4 minimum-interval 300
            !
            interface Bundle-Ether3
             bfd mode ietf
             bfd address-family ipv4 multiplier 5
            !
            interface Bundle-Ether4
             bfd mode ietf
             bfd address-family ipv4 fast-detect
            """;

    private static List<InterfaceKey> EXPECTED_ALL_IDS = Lists.newArrayList("Bundle-Ether2", "Bundle-Ether4")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Mock
    private Cli cli;

    private InterfaceReader interfaceReader;

    @BeforeEach
    void setup() {
        interfaceReader = new InterfaceReader(cli);
    }

    @Test
    void parseInterfaceIdsTest() {
        final List<InterfaceKey> actualInterfaceKeys = interfaceReader.parseInterfaceIds(SHOW_RUNNING_INTERFACES);
        assertEquals(EXPECTED_ALL_IDS, actualInterfaceKeys);
    }
}