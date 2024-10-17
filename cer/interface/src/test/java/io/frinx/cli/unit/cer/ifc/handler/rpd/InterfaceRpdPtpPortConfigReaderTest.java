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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.config.PtpPort;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.config.PtpPortBuilder;

class InterfaceRpdPtpPortConfigReaderTest {

    private static final PtpPort EXPECTED_INTERFACE_RPD_PTP_PORTS_CONFIG = new PtpPortBuilder()
            .setId(0)
            .setRole("slave")
            .setLocalPriority(1)
            .setMasterClockAddress("2001:b88:8005:fff1::2")
            .setEnable(false)
            .build();

    private static final String SH_INTERFACE_RPD_PTP_PORTS_RUN = """
            interface rpd "MND-GT0002-RPD1"\s
             ptp port 0
              role slave
              local-priority 1
              master-clock address 2001:b88:8005:fff1::2
              shutdown
             exit
            exit
            """;

    @Test
    void testParseInterfaceRpdPtpPortsConfig() {
        final var ptpPortBuilder = new PtpPortBuilder();
        InterfaceRpdPtpPortConfigReader.parsePtpPorts(SH_INTERFACE_RPD_PTP_PORTS_RUN, 0, ptpPortBuilder);
        assertEquals(EXPECTED_INTERFACE_RPD_PTP_PORTS_CONFIG, ptpPortBuilder.build());
    }
}
