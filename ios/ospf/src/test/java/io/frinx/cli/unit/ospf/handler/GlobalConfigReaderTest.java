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

package io.frinx.cli.unit.ospf.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;

class GlobalConfigReaderTest {

    private static final String OUTPUT = """
            router ospf 99\r
             router-id 5.5.5.5\r
             router-id 5.5.5.6\r
            router ospf 3737
             router-id 99.6.7.4
             bgp router-id 5.5.5.5
            """;

    @Test
    void testParse() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        GlobalConfigReader.parseGlobal(OUTPUT, builder, "99");
        assertEquals(getConfig("5.5.5.5"), builder.build());

        builder = new ConfigBuilder();
        GlobalConfigReader.parseGlobal(OUTPUT, builder, "3737");
        assertEquals(getConfig("99.6.7.4"), builder.build());

        builder = new ConfigBuilder();
        GlobalConfigReader.parseGlobal(OUTPUT, builder, "8888");
        assertEquals(new ConfigBuilder().build(), builder.build());
    }

    static Config getConfig(String rd) {
        return new ConfigBuilder()
                        .setRouterId(new DottedQuad(rd))
                        .build();
    }
}