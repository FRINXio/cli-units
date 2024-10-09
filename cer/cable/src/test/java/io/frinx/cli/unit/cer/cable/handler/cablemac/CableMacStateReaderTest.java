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

package io.frinx.cli.unit.cer.cable.handler.cablemac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.cable.mac.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.cable.mac.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.state.extension.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.state.extension.interfaces.InterfaceBuilder;

class CableMacStateReaderTest {

    private static final State EXPECTED_CABLE_MAC = new StateBuilder()
            .setInterfaces(new InterfacesBuilder().setInterface(Arrays.asList(
                    new InterfaceBuilder()
                    .setId("11/scq/8-2/scq/0")
                    .setName("11/scq/8-2/scq/0")
                    .setBonded("-")
                    .setState("Ranged")
                    .setDocSis("1.0")
                    .setQos("-")
                    .setCpe("0")
                    .setMacAddress("1c3a.dede.5555")
                    .setIpAddress("-")
                    .build(),
                    new InterfaceBuilder()
                    .setId("11/scq/8-2/scq/1")
                    .setName("11/scq/8-2/scq/1")
                    .setBonded("-")
                    .setState("Ranged")
                    .setDocSis("1.0")
                    .setQos("-")
                    .setCpe("0")
                    .setMacAddress("1c3a.dede.ffff")
                    .setIpAddress("-")
                    .build())).build())
            .build();

    private static final String SH_CABLE_MAC_RUN = """
            Feb 12 23:21:28

            Interface                                                                                           \s
            (DS-US)                                          DOC                                                \s
            S/C/CH-S/CG/CH          Mac   Bonded State       SIS  Qos(DS-US)     CPE  MAC address     IP Address\s
            ----------------------- ----- ------ ----------- --- --------------- ---  --------------- -----------
            11/scq/8-2/scq/0        127    -     Ranged      1.0        -          0  1c3a.dede.5555  -         \s
            11/scq/8-2/scq/1        127    -     Ranged      1.0        -          0  1c3a.dede.ffff  -         \s

                          Total    Oper  Disable    Init  Offline
            ---------------------------------------------------------
            Found             2       0        0       2        0\s
            """;

    @Test
    void testParseCableMac() {
        final var stateBuilder = new StateBuilder();
        CableMacStateReader.parseCableMac(SH_CABLE_MAC_RUN, stateBuilder);
        assertEquals(EXPECTED_CABLE_MAC, stateBuilder.build());
    }
}
