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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocolKey;


class L2ProtocolReaderTest {

    private static final String SERVICE_INSTANCE_L2PROTOCOL_OUTPUT = """
             service instance 200 ethernet EVC
              l2protocol tunnel lldp stp
              l2protocol peer lldp stp
            """;

    @Test
    void testParseL2protocol() {
        List<L2protocolKey> expected = Stream.of("tunnel", "peer")
                .map(L2protocolKey::new)
                .collect(Collectors.toList());
        List<L2protocolKey> l2protocolKeys =  L2protocolReader.parseL2protocolNames(SERVICE_INSTANCE_L2PROTOCOL_OUTPUT);
        assertEquals(expected, l2protocolKeys);
    }
}
