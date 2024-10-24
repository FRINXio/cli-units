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

package io.frinx.cli.unit.cer.ifc.handler.cablemac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.CableDsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.CableUsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.OfdmDsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.OfdmUsItemBuilder;

class InterfaceCableMacStateReaderTest {
    @SuppressWarnings("checkstyle:linelength")
    private static final String CABLE_MAC_STATE = """
            Cable-mac 127
            =============
            Inbound  access list is not set
            Outbound access list is not set
            Inband   access list is not set

            OFDM: true
            DS         Cable Chan Prim  Oper   Freq Low-High   PLC Band   LicBW    Num of  Subcarrier    Rolloff Cyclic  Intrlv
            S/C/CH     Mac   ID   Cap   State   (MHz.KHz)        (MHz)    (MHz)     Prof   Spacing(KHz)  Period  Prefix  Depth(time)
            11/ofd/0    127   33   True   IS  878.000-1070.000    917     189        5          50         192    256     1
            11/ofd/1    127   34   True  OOS 1071.000-1218.000   1087     144        5          50         192    256     1

            US          Cable     Oper    Freq Low-High    LicBW   Minislots  Mod  Subcarrier   Rolloff Cyclic Sym/  Power(dBmV/ \s
            S/CG/CH     Mac  Conn State    (MHz.KHz)      (100KHz) per frame  Prof Spacing(KHz) Period  Prefix Frame 1.6 MHz) \s
            2/ofd/0     127     -    IS  19.025-34.625     156        37        4       50         64    128      16        -6.0
            2/ofd/1     127     -   OOS  18.000-113.000    950         0        2       50         64    128      16           -

            CABLE:
            DS          Cable Chan  Prim  Oper                  Intrlv  Mod  Power     LBal
            S/C/CH      Mac   ID    Cap   State Annex Freq(Hz)  Depth   Type (.1dBmV)  Group
            11/scq/0    127      1  True     IS   A  602000000    12    q256      350  1073743872, 1073747968, 1073750016
            11/scq/1    127      2 False     IS   A  610000000    12    q256      350  1073743872, 1073747968, 1073750016

            US          Cable     Oper  Chan   Freq Low-High   Center  Channel   Mini Mod  Pwr(dBmV/  LBal\s
            S/CG/CH     Mac  Conn State Type    (MHz.KHz)      Freq(MHz) Width   Slot Prof  1.6 MHz)  Group
            2/scq/0     127     -    IS atdma  55.600-62.000    58.800   6.400     2  364    -6.0     1073747968
            2/scq/13    127     -    IS atdma  35.200-41.600    38.400   6.400     2  364    -6.0     1073743872, 1073750016""";

    private static final State EXPECTED_CABLE_MAC_STATE = new StateBuilder()
            .setOfdm(true)
            .setOfdmDsItem(List.of(
                    new OfdmDsItemBuilder()
                            .setId("11/ofd/0")
                            .setCableMac("127")
                            .setChanId("33")
                            .setPrimCap("True")
                            .setOperState("IS")
                            .setFreqLowHigh("878.000-1070.000")
                            .setPlcBand("917")
                            .setLicBw("189")
                            .setNumOfProf("5")
                            .setSubcarrierSpacing("50")
                            .setRolloffPeriod("192")
                            .setCyclicPrefix("256")
                            .setIntrlvDepth("1")
                            .build(),
                    new OfdmDsItemBuilder()
                            .setId("11/ofd/1")
                            .setCableMac("127")
                            .setChanId("34")
                            .setPrimCap("True")
                            .setOperState("OOS")
                            .setFreqLowHigh("1071.000-1218.000")
                            .setPlcBand("1087")
                            .setLicBw("144")
                            .setNumOfProf("5")
                            .setSubcarrierSpacing("50")
                            .setRolloffPeriod("192")
                            .setCyclicPrefix("256")
                            .setIntrlvDepth("1")
                            .build()))
            .setOfdmUsItem(List.of(
                    new OfdmUsItemBuilder()
                            .setId("2/ofd/0")
                            .setCableMac("127")
                            .setConn("-")
                            .setOperState("IS")
                            .setFreqLowHigh("19.025-34.625")
                            .setLicBw("156")
                            .setMinislotsPerFrame("37")
                            .setModProf("4")
                            .setSubcarrierSpacing("50")
                            .setRolloffPeriod("64")
                            .setCyclicPrefix("128")
                            .setSymFrame("16")
                            .setPower("-6.0")
                            .build(),
                    new OfdmUsItemBuilder()
                            .setId("2/ofd/1")
                            .setCableMac("127")
                            .setConn("-")
                            .setOperState("OOS")
                            .setFreqLowHigh("18.000-113.000")
                            .setLicBw("950")
                            .setMinislotsPerFrame("0")
                            .setModProf("2")
                            .setSubcarrierSpacing("50")
                            .setRolloffPeriod("64")
                            .setCyclicPrefix("128")
                            .setSymFrame("16")
                            .setPower("-")
                            .build()))
            .setCableDsItem(List.of(
                    new CableDsItemBuilder()
                            .setId("11/scq/0")
                            .setCableMac("127")
                            .setChanId("1")
                            .setPrimCap("True")
                            .setOperState("IS")
                            .setAnnex("A")
                            .setFrequency("602000000")
                            .setIntrlvDepth("12")
                            .setModType("q256")
                            .setPower("350")
                            .setLbalGroup("1073743872, 1073747968, 1073750016")
                            .build(),
                    new CableDsItemBuilder()
                            .setId("11/scq/1")
                            .setCableMac("127")
                            .setChanId("2")
                            .setPrimCap("False")
                            .setOperState("IS")
                            .setAnnex("A")
                            .setFrequency("610000000")
                            .setIntrlvDepth("12")
                            .setModType("q256")
                            .setPower("350")
                            .setLbalGroup("1073743872, 1073747968, 1073750016")
                            .build()))
            .setCableUsItem(List.of(
                    new CableUsItemBuilder()
                            .setId("2/scq/0")
                            .setCableMac("127")
                            .setConn("-")
                            .setOperState("IS")
                            .setChanType("atdma")
                            .setFreqLowHigh("55.600-62.000")
                            .setCenterFreq("58.800")
                            .setChannelWidth("6.400")
                            .setMiniSlot("2")
                            .setModProf("364")
                            .setPower("-6.0")
                            .setLbalGroup("1073747968")
                            .build(),
                    new CableUsItemBuilder()
                            .setId("2/scq/13")
                            .setCableMac("127")
                            .setConn("-")
                            .setOperState("IS")
                            .setChanType("atdma")
                            .setFreqLowHigh("35.200-41.600")
                            .setCenterFreq("38.400")
                            .setChannelWidth("6.400")
                            .setMiniSlot("2")
                            .setModProf("364")
                            .setPower("-6.0")
                            .setLbalGroup("1073743872, 1073750016")
                            .build()))
            .build();

    @Test
    void testCableMac() {
        final var cableMacStateBuilder = new StateBuilder();
        InterfaceCableMacStateReader.parseCableMac(CABLE_MAC_STATE, cableMacStateBuilder);
        assertEquals(EXPECTED_CABLE_MAC_STATE, cableMacStateBuilder.build());
    }
}
