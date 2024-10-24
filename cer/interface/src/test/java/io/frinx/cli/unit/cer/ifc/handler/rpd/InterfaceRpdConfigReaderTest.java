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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.top.rpd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.top.rpd.ConfigBuilder;

class InterfaceRpdConfigReaderTest {

    private static final Config EXPECTED_INTERFACE_RPD_CONFIG = new ConfigBuilder()
            .setRpdIndex(12)
            .setUcam(2)
            .setDcam(11)
            .setMacAddress("a870.5d28.b3c8")
            .setAdpEnable(true)
            .setSsdEnable(false)
            .setEnable(true)
            .build();

    private static final String SH_INTERFACE_RPD_RUN = """
            interface rpd "MND-GT0002-RPD1"\s
             rpd-index 12
             ucam 2 dcam 11
             mac-address a870.5d28.b3c8
             adp enable
             no shutdown
            exit
            """;

    @Test
    void testParseInterfaceRpdConfig() {
        final var configBuilder = new ConfigBuilder();
        InterfaceRpdConfigReader.parseConfig(SH_INTERFACE_RPD_RUN, configBuilder);
        assertEquals(EXPECTED_INTERFACE_RPD_CONFIG, configBuilder.build());
    }
}
