/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.us.conn.config.UsConn;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.us.conn.config.UsConnBuilder;

class InterfaceRpdUsConnConfigReaderTest {

    private static final UsConn EXPECTED_INTERFACE_RPD_US_CONNS_CONFIG = new UsConnBuilder()
            .setId(0)
            .setBaseTargetPower(Short.valueOf("-60"))
            .build();

    private static final String SH_INTERFACE_RPD_US_CONNS_RUN = """
            interface rpd "MND-GT0002-RPD1"\s
             us-conn 0 base-target-power -60
            exit
            """;

    @Test
    void testParseInterfaceRpdUsConnsConfig() {
        final var usConnBuilder = new UsConnBuilder();
        InterfaceRpdUsConnConfigReader.parseUsConn(SH_INTERFACE_RPD_US_CONNS_RUN, 0, usConnBuilder);
        assertEquals(EXPECTED_INTERFACE_RPD_US_CONNS_CONFIG, usConnBuilder.build());
    }
}
