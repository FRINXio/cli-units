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

package io.frinx.cli.unit.iosxe.evc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.EvcKey;

class EvcReaderTest {

    private static final String SH_EVCS = """
            ethernet evc EVPN_1_2
            ethernet evc EVPN_1_3
            ethernet evc EVPN_1_4
            ethernet evc EVPN_1_5
            ethernet evc EVPN_2_5
            """;

    @Test
    void getAllIdsTest() {
        List<EvcKey> evcKeys = Arrays.asList(
                new EvcKey("EVPN_1_2"),
                new EvcKey("EVPN_1_3"),
                new EvcKey("EVPN_1_4"),
                new EvcKey("EVPN_1_5"),
                new EvcKey("EVPN_2_5")
        );
        assertEquals(evcKeys, EvcReader.getAllIds(SH_EVCS));
    }
}