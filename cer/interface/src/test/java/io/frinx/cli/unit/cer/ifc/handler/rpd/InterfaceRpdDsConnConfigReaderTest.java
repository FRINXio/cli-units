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

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.config.DsConn;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.config.DsConnBuilder;

class InterfaceRpdDsConnConfigReaderTest {

    private static final DsConn EXPECTED_INTERFACE_RPD_DS_CONNS_CONFIG = new DsConnBuilder()
            .setId(0)
            .setPowerLevel(320)
            .setDsGroup(Collections.singletonList("\"SG_2827355189\""))
            .build();

    private static final String SH_INTERFACE_RPD_DS_CONNS_RUN = """
            interface rpd "MND-GT0002-RPD1"\s
             ds-conn 0 power-level 320
             ds-conn 0 ds-group "SG_2827355189"
            exit
            """;

    @Test
    void testParseInterfaceRpdDsConnsConfig() {
        final var dsConnBuilder = new DsConnBuilder();
        InterfaceRpdDsConnConfigReader.parseDsConn(SH_INTERFACE_RPD_DS_CONNS_RUN, 0, dsConnBuilder);
        assertEquals(EXPECTED_INTERFACE_RPD_DS_CONNS_CONFIG, dsConnBuilder.build());
    }
}
