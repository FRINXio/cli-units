/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.network.instance.l2vsi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

class L2VSIReaderTest {

    private static String OUTPUT = """
             vpls test 12
             vpls test2 13
             vpls tese2 14
             vpls tesr2 15
             vpls tesf2 16
             vpls testG2 17
             vpls tesj2 19
            """;

    @Test
    void readTest() {
        List<NetworkInstanceKey> keys = L2VSIReader.parseL2Vsis(OUTPUT);
        assertEquals(7, keys.size());
    }
}