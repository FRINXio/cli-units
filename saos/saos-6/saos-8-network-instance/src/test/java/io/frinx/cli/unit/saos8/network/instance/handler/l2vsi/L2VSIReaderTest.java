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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

class L2VSIReaderTest {

    private static final String OUTPUT =
            """
                    virtual-switch create vs IPVPN_1201
                    virtual-switch create vs IPVPN_1202
                    virtual-switch create vs IPVPN_1203
                    virtual-switch create vs IPVPN_1204
                    virtual-switch create vs TEST-AMELAND
                    virtual-switch create vs AMELAND_1010
                    """;

    @Test
    void getAllIds() {
        List<NetworkInstanceKey> expected = Arrays.asList(
                new NetworkInstanceKey("IPVPN_1201"),
                new NetworkInstanceKey("IPVPN_1202"),
                new NetworkInstanceKey("IPVPN_1203"),
                new NetworkInstanceKey("IPVPN_1204"),
                new NetworkInstanceKey("TEST-AMELAND"),
                new NetworkInstanceKey("AMELAND_1010"));
        assertEquals(expected, L2VSIReader.getAllIds(OUTPUT));
    }
}
