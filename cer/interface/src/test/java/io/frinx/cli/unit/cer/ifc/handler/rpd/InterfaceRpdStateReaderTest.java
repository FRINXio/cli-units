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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.RpdState.AdminState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.RpdState.OperState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.ptp.state.top.Ptp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.ptp.state.top.PtpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.RpdChannelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.RpdChannelStatusBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.top.rpd.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.top.rpd.StateBuilder;

class InterfaceRpdStateReaderTest {

    private static final String RPD_STATE = """
            Core Clock State: Test_state_alligned
                                             Cable         Type   Admin Oper                                 \s
            RPD Name     MAC Address    IDX Mac UCAM DCAM (DSxUS) State State IP Address                     \s
            ------------ -------------- --- --- ---- ---- ------- ----- ----- --------------------------------
            Test-RPD-84  129c.5aa1.17cc  24 752  875  87    8x12    Down  IS   2871:a12:8twf5:8:2:10:sf512:8754
            """;

    private static final String PTP_STATE = """
            RPD PTP DSCP:                      50
            Core Clock State:                Test_state
            RPD Name:  Test-RPD-84
            RPD Index: 24
              PTP Clock
              -----------------------------------------
              Domain:                          18
              Profile Name:                    C.1234.5
              Profile Version:                 9.8
              State:                           Test state
              Last State Change:               12 days 19:22:48
              Last Computed Phase Offset:      -123
              Estimated Frequency Offset:      845
            """;

    @SuppressWarnings("checkstyle:linelength")
    private static final String RPD_CHANNEL_STATE = """
            Core Clock State: Phase_aligned
            RPD (Name::DS/US)    Dir Interfaces                                                                                   \s
            -------------------- --- ----------------------------------------------------------------------------------------------
            MND-GT0002-RPD1::0   DS  11/scq/600-633 11/ofd/24                                                                     \s
            MND-GT0002-RPD1::0   US   2/scq/240-243 2/ofd/48                                                                      \s
            MND-GT0002-RPD1::1   US   2/scq/250-253 2/ofd/50""";

    private static final String RPD_CHANNEL_STATUS_STATE = """
            Core Clock State: Phase_aligned
                                                   RPD's                   DEPI   \s
            RPD Name          Dir Interface        Chan State  Chan Sel    ChanID Core PW ID
            ----------------- --- ---------------- ----------- ----------- ------ -----------------------------------
             MND-GT0002-RPD1::0 DS  11/scq/600       NotPresent  0           0      00000000
            +MND-GT0002-RPD1::0 DS  11/scq/601       NotPresent  0           0      00000000
            +MND-GT0002-RPD1::1 US  2/ofd/50         NotPresent  0           -      -""";

    private static final State EXPECTED_RPD_STATE = new StateBuilder()
            .setClockState("Test_state_alligned")
            .setMacAddress("129c.5aa1.17cc")
            .setRpdIndex(24)
            .setCableMac(752)
            .setUcam(875)
            .setDcam(87)
            .setDsUsType("8x12")
            .setAdminState(AdminState.DOWN)
            .setOperState(OperState.IS)
            .setIpv6Address("2871:a12:8twf5:8:2:10:sf512:8754")
            .build();

    private static final Ptp EXPECTED_PTP_STATE = new PtpBuilder()
            .setClockState("Test_state")
            .setDscp(50)
            .setDomain(18)
            .setProfile("C.1234.5")
            .setVersion("9.8")
            .setState("Test state")
            .setLastStateChange("12 days 19:22:48")
            .setPhaseOffset("-123")
            .setFrequencyOffset("845")
            .build();

    private static final State EXPECTED_RPD_CHANNEL_STATE = new StateBuilder()
            .setRpdChannel(List.of(
                    new RpdChannelBuilder()
                            .setId("MND-GT0002-RPD1::0 DS")
                            .setRpdName("MND-GT0002-RPD1::0")
                            .setRpdDir("DS")
                            .setRpdInterfaces("11/scq/600-633 11/ofd/24")
                            .build(),
                    new RpdChannelBuilder()
                            .setId("MND-GT0002-RPD1::0 US")
                            .setRpdName("MND-GT0002-RPD1::0")
                            .setRpdDir("US")
                            .setRpdInterfaces("2/scq/240-243 2/ofd/48")
                            .build(),
                    new RpdChannelBuilder()
                            .setId("MND-GT0002-RPD1::1 US")
                            .setRpdName("MND-GT0002-RPD1::1")
                            .setRpdDir("US")
                            .setRpdInterfaces("2/scq/250-253 2/ofd/50")
                            .build()))
            .build();

    private static final State EXPECTED_RPD_CHANNEL_STATUS_STATE = new StateBuilder()
            .setRpdChannelStatus(List.of(
                    new RpdChannelStatusBuilder()
                            .setId("MND-GT0002-RPD1::0 DS 11/scq/600")
                            .setRpdName("MND-GT0002-RPD1::0")
                            .setRpdDir("DS")
                            .setRpdInterfaces("11/scq/600")
                            .setChannelState("NotPresent")
                            .setChannelSelection("0")
                            .setDepiChannelId("0")
                            .setCorePwId("00000000")
                            .build(),
                    new RpdChannelStatusBuilder()
                            .setId("+MND-GT0002-RPD1::0 DS 11/scq/601")
                            .setRpdName("+MND-GT0002-RPD1::0")
                            .setRpdDir("DS")
                            .setRpdInterfaces("11/scq/601")
                            .setChannelState("NotPresent")
                            .setChannelSelection("0")
                            .setDepiChannelId("0")
                            .setCorePwId("00000000")
                            .build(),
                    new RpdChannelStatusBuilder()
                            .setId("+MND-GT0002-RPD1::1 US 2/ofd/50")
                            .setRpdName("+MND-GT0002-RPD1::1")
                            .setRpdDir("US")
                            .setRpdInterfaces("2/ofd/50")
                            .setChannelState("NotPresent")
                            .setChannelSelection("0")
                            .setDepiChannelId("-")
                            .setCorePwId("-")
                            .build()))
            .build();

    @Test
    void testRpdState() {
        final var rpdStateBuilder = new StateBuilder();
        InterfaceRpdStateReader.parseRpdStateData(RPD_STATE, rpdStateBuilder);
        assertEquals(EXPECTED_RPD_STATE, rpdStateBuilder.build());
    }

    @Test
    void testPtpState() {
        final var stateBuilder = new StateBuilder();
        InterfaceRpdStateReader.parsePtpStateDate(PTP_STATE, stateBuilder);
        assertEquals(EXPECTED_PTP_STATE, stateBuilder.getPtp());
    }

    @Test
    void testRpdChannel() {
        final var rpdStateBuilder = new StateBuilder();
        InterfaceRpdStateReader.parseRpdChannel(RPD_CHANNEL_STATE, rpdStateBuilder);
        assertEquals(EXPECTED_RPD_CHANNEL_STATE, rpdStateBuilder.build());
    }

    @Test
    void testRpdChannelStatus() {
        final var rpdStateBuilder = new StateBuilder();
        InterfaceRpdStateReader.parseRpdChannelStatus(RPD_CHANNEL_STATUS_STATE, rpdStateBuilder);
        assertEquals(EXPECTED_RPD_CHANNEL_STATUS_STATE, rpdStateBuilder.build());
    }
}