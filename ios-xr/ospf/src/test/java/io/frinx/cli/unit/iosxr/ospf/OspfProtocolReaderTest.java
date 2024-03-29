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

import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.ospf.handler.OspfProtocolReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;

class OspfProtocolReaderTest {
    private static final String SH_RUN_OSPF = """
            Mon Feb 12 14:57:11.223 UTC
            router ospf 97
            router ospf EXAMPLE_OSPF
            """;

    private static final List<ProtocolKey> EXPECTED_KEYES = Lists.newArrayList("97", "EXAMPLE_OSPF")
            .stream()
            .map(ospfId -> new ProtocolKey(OSPF.class, ospfId))
            .collect(Collectors.toList());

    @Test
    void testParseOspfIds() {
        assertEquals(EXPECTED_KEYES, OspfProtocolReader.parseOspfIds(SH_RUN_OSPF));
    }

}
