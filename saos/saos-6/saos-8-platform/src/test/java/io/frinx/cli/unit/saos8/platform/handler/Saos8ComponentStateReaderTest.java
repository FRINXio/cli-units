/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos8.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaSaos8PlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.CpuBoardBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.DeviceTechnologyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.DiagMonitorCapsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.DiagnosticMonitorTypeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.EnhancedOptBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.ExIdentifierBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.MainBoardBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.MediaPropertiesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.NumLanesSupportedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.RatesSupportedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.RxMonitorClockOptionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.SignalCodeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.TransceiverCodesPropsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.TxMonitorClockOptionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;

class Saos8ComponentStateReaderTest {

    private static final String OUTPUT_XCVR_PORT = """
            +------------------------ XCVR VENDOR DATA - Port 1/1      --------------------+
            | Parameter                | Value              | Decoded String Equivalent    |
            +--------------------------+--------------------+------------------------------+
            | Identifier               | 0xe                | CFP transceiver              |
            | Ext. Identifier          | 0x61               |                              |
            |  - Module Power Level    | Bit 6-7            | Level 2 (<=16W max.)         |
            |  - Lane Ratio Type       | Bit 4-5            | n:n Parallel                 |
            |  - WDM Type              | Bit 1-3            | Non-WDM                      |
            |  - CLEI Code Present     | Bit 0              | Yes                          |
            | Connector                | 0x9                | MU                           |
            +--------------------------+--------------------+------------------------------+
            | Transceiver Codes        | 0x4aaa180000000003 |                              |
            |  - Ethernet Compliance   | 0x03               | 100GE-MM-SR10                |
            |  - Fiber Compliance      | 0x00               |                              |
            |  - Copper Compliance     | 0x00               |                              |
            |  - SONET Compliance      | 0x00               |                              |
            |  - OTN Compliance        | 0x00               |                              |
            |--------------------------+--------------------+------------------------------|
            | Add'l Rates Supported    | 0x18               |                              |
            |  - 111.8 Gb/s            | Bit 4              | Yes                          |
            |  - 103.125 Gb/s          | Bit 3              | Yes                          |
            |  - 41.25 Gb/s            | Bit 2              | No                           |
            |  - 43 Gb/s               | Bit 1              | No                           |
            |  - 39.8 Gb/s             | Bit 0              | No                           |
            | Num Lanes Supported      | 0xaa               |                              |
            |  - Num Network Lanes     | Bit 4-7            | 10 lanes                     |
            |  - Num Host Lanes        | Bit 0-3            | 10 lanes                     |
            | Media Properties         | 0x4a               |                              |
            |  - Media Type            | Bit 6-7            | MMF (OM3)                    |
            |  - Directionality        | Bit 5              | Normal                       |
            |  - Optical Mux/Demux     | Bit 4              | No                           |
            |  - Active Fiber per Con  | Bit 0-3            | 10 tx / 10 rx lanes          |
            |--------------------------+--------------------+------------------------------|
            | Max Network Lane BR      | 0x38               | 11.20 Gb/s                   |
            | Max Host Lane BR         | 0x38               | 11.20 Gb/s                   |
            | Max SM Fiber Length      | 0x0                | 0km                          |
            | Max MM Fiber Length      | 0xa                | 100m                         |
            | Max Cu Cable Length      | 0x0                | 0m                           |
            | Min Wavelength per Fiber | 0x8340             | 840.000 nm                   |
            | Max Wavelength per Fiber | 0x8660             | 860.000 nm                   |
            | Max per Lane Opt Width   | 0x28a              | 650 pm                       |
            |--------------------------+--------------------+------------------------------|
            | Device Technology 1      | 0x0                |                              |
            |  - Laser Source Tech     | Bit 4-7            | VCSEL                        |
            |  - Tx Modulation Tech    | Bit 0-3            | DML                          |
            | Device Technology 2      | 0x4                |                              |
            |  - Wavelength Control    | Bit 7              | No                           |
            |  - Cooled Transmitter    | Bit 6              | No                           |
            |  - Tunability            | Bit 5              | No                           |
            |  - VOA Implemented       | Bit 4              | No                           |
            |  - Detector Type         | Bit 2-3            | PIN detector                 |
            |  - CDR with EDC          | Bit 1              | No                           |
            | Signal Code              | 0x40               |                              |
            |  - Modulation            | Bit 6-7            | NRZ                          |
            |  - Signal Coding         | Bit 2-5            | Non-PSK                      |
            |--------------------------+--------------------+------------------------------|
            | Max Output Pwr per Con   | 0xad               | 17300 uW                     |
            | Max Input Pwr per Lane   | 0x11               | 1700 uW                      |
            | Max Pwr Consumption      | 0x3c               | 12000 mW                     |
            | Max Pwr in Low Pwr Mode  | 0x32               | 1000 mW                      |
            | Max Oper Case Temp       | 0x46               | 70 C                         |
            | Min Oper Case Temp       | 0x0                | 0 C                          |
            | Max High-Power-up Time   | 0x4                | 4 s                          |
            | Max High-Power-down Time | 0x1                | 1 s                          |
            | Max Tx-Turn-on Time      | 0x1                | 1 s                          |
            | Max Tx-Turn-off Time     | 0x64               | 100 ms                       |
            | Heat Sink Type           | 0x0                | Flat top                     |
            | Host Ln Signal Spec      | 0x1                | CAUI                         |
            |--------------------------+--------------------+------------------------------|
            | Ciena Module Identifier  | CIENA              |                              |
            | Ciena Module Item Number | NTTA03AA           |                              |
            | Ciena Module Rev Number  | 001                |                              |
            | Ciena Vendor Serial Num  | FNSRMYUS91KEF      |                              |
            | Date Code                | 20140902           |                              |
            | Lot Code                 | 00                 |                              |
            | CLEI Code                | WOTRCWWFAA         |                              |
            | CFP MSA HW Spec Rev      | 0xe                | 1.4                          |
            | CFP MSA Mgmt IF Spec Rev | 0xe                | 1.4                          |
            | Module HW Version        | 0x200              | 2.0                          |
            | Module FW Version        | 0x106              | 1.6                          |
            |--------------------------+--------------------+------------------------------|
            | Diag Monitor Type        | 0xc                |                              |
            |  - Rx Power Meas. Type   | Bit 3              | Avg                          |
            |  - Tx Power Meas. Type   | Bit 2              | Avg                          |
            | Diag Monitor Caps 1      | 0x3                |                              |
            |  - Tx aux monitor 2      | Bit 6-7            | No                           |
            |  - Tx aux monitor 1      | Bit 4-5            | No                           |
            |  - Tx SOA bias current   | Bit 2              | No                           |
            |  - Tx pwr supply voltage | Bit 1              | Yes                          |
            |  - Tx temperature        | Bit 0              | Yes                          |
            | Diag Monitor Caps 2      | 0x0                |                              |
            |  - Netwk ln rx pwr       | Bit 3              | No                           |
            |  - Netwk ln output pwr   | Bit 2              | No                           |
            |  - Netwk ln bias current | Bit 1              | No                           |
            |  - Netwk ln temperature  | Bit 0              | No                           |
            |--------------------------+--------------------+------------------------------|
            | Enhanced Options 1       | 0xa0               |                              |
            |  - Host ln loopback      | Bit 7              | Yes                          |
            |  - Host ln PRBS          | Bit 6              | No                           |
            |  - Host ln emphasis ctrl | Bit 5              | Yes                          |
            |  - Netwk ln loopback     | Bit 4              | No                           |
            |  - Netwk ln PRBS         | Bit 3              | No                           |
            |  - Amplitude adjustment  | Bit 2              | No                           |
            |  - Phase adjustment      | Bit 1              | No                           |
            |  - Unidirectional tx/rx  | Bit 0              | No                           |
            | Enhanced Options 2       | 0x0                |                              |
            |  - Active volt/phase func| Bit 4              | No                           |
            |  - Rx FIFO reset         | Bit 3              | No                           |
            |  - Rx FIFO auto reset    | Bit 2              | No                           |
            |  - Tx FIFO reset         | Bit 1              | No                           |
            |  - Tx FIFO auto reset    | Bit 0              | No                           |
            | Tx Monitor Clock Options | 0x0                |                              |
            |  - 1/16 of Host ln rate  | Bit 7              | No                           |
            |  - 1/16 of Netwk ln rate | Bit 6              | No                           |
            |  - 1/64 of Host ln rate  | Bit 5              | No                           |
            |  - 1/64 of Netwk ln rate | Bit 4              | No                           |
            |  - 1/8 of Netwk ln rate  | Bit 2              | No                           |
            |  - Monitor clock option  | Bit 0              | No                           |
            | Rx Monitor Clock Options | 0x0                |                              |
            |  - 1/16 of Host ln rate  | Bit 7              | No                           |
            |  - 1/16 of Netwk ln rate | Bit 6              | No                           |
            |  - 1/64 of Host ln rate  | Bit 5              | No                           |
            |  - 1/64 of Netwk ln rate | Bit 4              | No                           |
            |  - 1/8 of Netwk ln rate  | Bit 2              | No                           |
            |  - Monitor clock option  | Bit 0              | No                           |
            |--------------------------+--------------------+------------------------------|""";

    @Test
    void parseDevTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseDev(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT);

        var expectedState = new StateBuilder()
                .setName(Saos8ComponentReader.DEVICE_ID)
                .setId(Saos8ComponentReader.DEVICE_ID);
        var expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEthernetBaseAddress("2c:39:c1:47:2c:00")
                .setEthernetAddressBlockSize("1024")
                .setModuleSn("M7655762")
                .setModelPartNumber("154-8700-930")
                .setModelRevision("003")
                .setProductId("CHASS-8700-4")
                .setManufacturedDate("09192014")
                .setCleiCode("IPMWV00DRA")
                .setBarCode("466385")
                .setBackplaneAssySn("C5307742")
                .setBackplaneAssyPn("154-0037-810")
                .setBackplaneAssyRev("005")
                .setBackplaneSn("B7428748")
                .setBackplanePn("154-0037-410")
                .setBackplaneRev("002")
                .setSoftwareCompatibility("1")
                .setFunctionalTestCount("1");

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parseModuleSlotTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseModuleSlot(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT, "CTX1.ctm");

        var expectedState = new StateBuilder()
                .setName("CTX1.ctm")
                .setId("MODULE");
        var expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEthernetBaseAddress("00:23:8a:ea:bf:10")
                .setEthernetAddressBlockSize("8")
                .setModuleSn("M7626011")
                .setModelPartNumber("154-0005-900")
                .setModelRevision(";;X")
                .setProductId("CTX-8700")
                .setManufacturedDate("12292020")
                .setCleiCode("IPUCBGREAA")
                .setBarCode("189524")
                .setBoardSn("C5265618")
                .setBoardPn("154-0005-870")
                .setBoardRev("013")
                .setSoftwareCompatibility("1")
                .setFunctionalTestCount("1")
                .setFaultCard("0");

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseModuleSlot(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT, "CTX1.sm");

        expectedState = new StateBuilder()
                .setName("CTX1.sm")
                .setId("MODULE");
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setModuleSn("M7626011")
                .setModelPartNumber("154-0005-900")
                .setModelRevision("';;X'")
                .setProductId("CTX-8700")
                .setManufacturedDate("12292020")
                .setCleiCode("IPUCBGREAA")
                .setBarCode("189524")
                .setBoardSn("C5265618")
                .setBoardPn("154-0005-870")
                .setBoardRev("013")
                .setSoftwareCompatibility("1")
                .setFunctionalTestCount("1")
                .setFaultCard("0");

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parseModuleSlotLMtest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseModuleSlotLM(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT, "LM1");

        var expectedState = new StateBuilder()
                .setName("LM1")
                .setId("MODULE");
        var expectedAug = new CienaSaos8PlatformAugBuilder()
                .setCpuBoard(new CpuBoardBuilder()
                        .setEthernetBaseAddress("00:23:8a:e0:0a:c8")
                        .setEthernetAddressBlockSize("8")
                        .setModuleSn("M7589108")
                        .setModelPartNumber("154-0402-900")
                        .setModelRevision("004")
                        .setProductId("PSLM-200-2")
                        .setManufacturedDate("10162014")
                        .setCleiCode("IPUCBG2EAA")
                        .setBarCode("189448")
                        .setBoardSn("B7392652")
                        .setBoardPn("134-0252-420")
                        .setBoardRev("008")
                        .setSoftwareCompatibility("1")
                        .setFunctionalTestCount("1")
                        .setFaultCard("0")
                        .build())
                .setMainBoard(new MainBoardBuilder()
                        .setModuleSn("M7589108")
                        .setModelPartNumber("154-0402-900")
                        .setModelRevision("004")
                        .setManufacturedDate("09082014")
                        .setBoardSn("B7445460")
                        .setBoardPn("154-0402-420")
                        .setBoardRev("005")
                        .build());

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parseDeviceSlotTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseDeviceSlot(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT, "CFU");

        var expectedState = new StateBuilder()
                .setName("CFU")
                .setId("DEV-SLOT");
        var expectedAug = new CienaSaos8PlatformAugBuilder()
                .setModuleSn("M7619209")
                .setModelPartNumber("154-0008-900")
                .setModelRevision("004")
                .setProductId("FAN-8700-4")
                .setManufacturedDate("08202014")
                .setCleiCode("IPUCBGUEAA")
                .setBarCode("189529")
                .setBoardSn("")
                .setBoardPn("")
                .setBoardRev("001")
                .setSoftwareCompatibility("1")
                .setFunctionalTestCount("1");

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseDeviceSlot(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT, "PWR-A");

        expectedState = new StateBuilder()
                .setName("PWR-A")
                .setId("DEV-SLOT");
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setModuleSn("M7732920")
                .setModelPartNumber("154-0001-900")
                .setModelRevision("002")
                .setProductId("PSU-8700")
                .setManufacturedDate("09052014")
                .setCleiCode("IPUPAL4PAA")
                .setBarCode("189537")
                .setBoardSn("M7732920")
                .setBoardPn("PS2553-Y")
                .setBoardRev("00A")
                .setSoftwareCompatibility("1")
                .setFunctionalTestCount("1");

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parseIomTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaSaos8PlatformAugBuilder();
        Saos8ComponentStateReader.parseIom(stateBuilder, builder, Saos8ComponentReaderTest.OUTPUT, "IOM");

        var expectedState = new StateBuilder()
                .setName("IOM")
                .setId("IOM");
        var expectedAug = new CienaSaos8PlatformAugBuilder()
                .setModuleSn("M7750640")
                .setModelPartNumber("154-0004-900")
                .setModelRevision("005")
                .setProductId("IOM-8700")
                .setManufacturedDate("09112014")
                .setCleiCode("IPU3A7ALAA")
                .setBarCode("189527")
                .setBoardSn("C5318431")
                .setBoardPn("154-0004-830")
                .setBoardRev("006")
                .setSoftwareCompatibility("1")
                .setFunctionalTestCount("1");

        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parsePortTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaSaos8PlatformAugBuilder();
        var cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder, OUTPUT_XCVR_PORT
                + Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "1/1");
        var expectedState = new StateBuilder().setName("1/1").setId("Port");
        var expectedCienaAug = new CienaPlatformAugBuilder()
                .setIdentifier("CFP transceiver")
                .setConnector("MU");
        var expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CIENA NTTA03AA Rev001")
                .setEthernetType("100GE-MM-SR10:MU")
                .setDiagnosticData(true)
                .setCienaModuleIdentifier("CIENA")
                .setCienaModuleItemNumber("NTTA03AA")
                .setCienaModuleRevNumber("001")
                .setCienaVendorSerialNumber("FNSRMYUS91KEF")
                .setDateCode("20140902")
                .setLotCode("00")
                .setCleiCode("WOTRCWWFAA")
                .setExIdentifier(new ExIdentifierBuilder()
                        .setExtIdentifier("0x61")
                        .setModulePowerLevel("Level 2 (<=16W max.)")
                        .setLaneRatioType("n:n Parallel")
                        .setWdmType("Non-WDM")
                        .setCleiCodePresent(true)
                        .build())
                .setTransceiverCodesProps(new TransceiverCodesPropsBuilder()
                        .setTransceiverCodes("0x4aaa180000000003")
                        .setEthernetCompliance("100GE-MM-SR10")
                        .setFiberCompliance("")
                        .setCopperCompliance("")
                        .setSonetCompliance("")
                        .setOtnCompliance("")
                        .build())
                .setRatesSupported(new RatesSupportedBuilder()
                        .setRatesSupported("0x18")
                        .setRate1118(true)
                        .setRate103125(true)
                        .setRate4125(false)
                        .setRate43(false)
                        .setRate398(false)
                        .build())
                .setNumLanesSupported(new NumLanesSupportedBuilder()
                        .setNumLanesSupported("0xaa")
                        .setNumNetworkLanes(10)
                        .setNumHostLanes(10)
                        .build())
                .setMediaProperties(new MediaPropertiesBuilder()
                        .setMediaProperties("0x4a")
                        .setMediaType("MMF (OM3)")
                        .setDirectionality("Normal")
                        .setOpticalMuxDemux(false)
                        .setActiveFiberPerCon("10 tx / 10 rx lanes")
                        .build())
                .setMaxNetworkLaneBr("11.20 Gb/s")
                .setMaxHostLaneBr("11.20 Gb/s")
                .setMaxSmFiberLength("0km")
                .setMaxMmFiberLength("100m")
                .setMaxCuCableLength("0m")
                .setMinWavelengthPerFiber("840.000 nm")
                .setMaxWavelengthPerFiber("860.000 nm")
                .setMaxPerLaneOptWidth("650 pm")
                .setDeviceTechnology(new DeviceTechnologyBuilder()
                        .setDeviceTechnology1("0x0")
                        .setDeviceTechnology2("0x4")
                        .setLaserSourceTech("VCSEL")
                        .setTxModulationTech("DML")
                        .setWavelengthControl(false)
                        .setCooledTransmitter(false)
                        .setTunability(false)
                        .setVoaImplemented(false)
                        .setDetectorType("PIN detector")
                        .setCdrWithEdc(false)
                        .build())
                .setSignalCode(new SignalCodeBuilder()
                        .setSignalCode("0x40")
                        .setModulation("NRZ")
                        .setSignalCoding("Non-PSK")
                        .build())
                .setMaxOutputPwrPerCon("17300 uW")
                .setMaxInputPwrPerLane("1700 uW")
                .setMaxPwrConsumption("12000 mW")
                .setMaxPwrInLowPwrMode("1000 mW")
                .setMaxOperCaseTemp("70 C")
                .setMinOperCaseTemp("0 C")
                .setMaxHighPowerUpTime("4 s")
                .setMaxHighPowerDownTime("1 s")
                .setMaxTxTurnOnTime("1 s")
                .setMaxTxTurnOffTime("100 ms")
                .setHeatSinkType("Flat top")
                .setHostLnSignalSpec("CAUI")
                .setCfpMsaHwSpecRev("1.4")
                .setCfpMsaMgmtIfSpecRev("1.4")
                .setModuleHwVersion("2.0")
                .setModuleFwVersion("1.6")
                .setDiagnosticMonitorType(new DiagnosticMonitorTypeBuilder()
                        .setDiagMonitorType("0xc")
                        .setRxPowerMeasType("Avg")
                        .setTxPowerMeasType("Avg")
                        .build())
                .setDiagMonitorCaps(new DiagMonitorCapsBuilder()
                        .setDiagMonitorCaps1("0x3")
                        .setDiagMonitorCaps2("0x0")
                        .setTxAuxMonitor1(false)
                        .setTxAuxMonitor2(false)
                        .setTxSoaBiasCurrent(false)
                        .setTxPwrSupplyVoltage(true)
                        .setTxTemperature(true)
                        .setNetworkLnRxPwr(false)
                        .setNetworkLnOutputPwr(false)
                        .setNetworkLnBiasCurrent(false)
                        .setNetworkLnTemperature(false)
                        .build())
                .setEnhancedOpt(new EnhancedOptBuilder()
                        .setEnhancedOptions1("0xa0")
                        .setEnhancedOptions2("0x0")
                        .setHostLnLoopback(true)
                        .setHostLnPrbs(false)
                        .setHostLnEmphasisCtrl(true)
                        .setNetworkLnLoopback(false)
                        .setNetworkLnPrbs(false)
                        .setAmplitudeAdjustment(false)
                        .setPhaseAdjustment(false)
                        .setUnidirectionalTxRx(false)
                        .setActiveVoltPhaseFunc(false)
                        .setRxFifoReset(false)
                        .setRxFifoAutoReset(false)
                        .setTxFifoReset(false)
                        .setTxFifoAutoReset(false)
                        .build())
                .setTxMonitorClockOptions(new TxMonitorClockOptionsBuilder()
                        .setTxMonitorClockOptions("0x0")
                        .setHostLnRate116(false)
                        .setNetworkLnRate116(false)
                        .setHostLnRate164(false)
                        .setNetworkLnRate164(false)
                        .setNetworkLnRate18(false)
                        .setMonitorClockOption(false)
                        .build())
                .setRxMonitorClockOptions(new RxMonitorClockOptionsBuilder()
                        .setRxMonitorClockOptions("0x0")
                        .setHostLnRate116(false)
                        .setNetworkLnRate116(false)
                        .setHostLnRate164(false)
                        .setNetworkLnRate164(false)
                        .setNetworkLnRate18(false)
                        .setMonitorClockOption(false)
                        .build());
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "2/1");
        expectedState = new StateBuilder().setName("2/1").setId("Port");
        expectedAug = new CienaSaos8PlatformAugBuilder().setEmpty(true);
        expectedCienaAug = new CienaPlatformAugBuilder();
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "2/2");
        expectedState = new StateBuilder().setName("2/2").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CIENA-JDS XCVR-S10V31 Rev000B")
                .setEthernetType("10G BASE-LR:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "2/3");
        expectedState = new StateBuilder().setName("2/3").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-AVAGO SFCT-739SMZ RevG3.1")
                .setEthernetType("10G BASE-LR:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "2/7");
        expectedState = new StateBuilder().setName("2/7").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-OPLINK TPP5XGFLRCCISE2G Rev01")
                .setEthernetType("10G BASE-LR:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "2/9");
        expectedState = new StateBuilder().setName("2/9").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-FINISAR FTLX1474D3BCL-C2 RevA")
                .setEthernetType("10G BASE-LR:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "2/10");
        expectedState = new StateBuilder().setName("2/10").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-AVAGO SFCT-5798PZ-CS3 Rev0000")
                .setEthernetType("1000BASE-LX:LC")
                .setDiagnosticData(false);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "3/1");
        expectedState = new StateBuilder().setName("3/1").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CIENA-JDS XCVR-S10V31 Rev000B")
                .setEthernetType("10G BASE-LR:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "3/9");
        expectedState = new StateBuilder().setName("3/9").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-FINISAR FTRJ1319P1BTL-C7 RevA")
                .setEthernetType("1000BASE-LX:LC")
                .setDiagnosticData(false);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "3/13");
        expectedState = new StateBuilder().setName("3/13").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("JDSU JMEP-01LX10A00 Rev1")
                .setEthernetType("1000BASE-LX:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "3/17");
        expectedState = new StateBuilder().setName("3/17").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-AVAGO ABCU-5710RZ-CS2")
                .setEthernetType("1000BASE-T:RJ45")
                .setDiagnosticData(false);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "3/19");
        expectedState = new StateBuilder().setName("3/19").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CIENA-FIN XCVR-B00CRJ RevA")
                .setEthernetType("1000BASE-T:RJ45")
                .setDiagnosticData(false);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "4/1");
        expectedState = new StateBuilder().setName("4/1").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CISCO-AVAGO SFCT-739SMZ RevG3.1")
                .setEthernetType("10G BASE-LR:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());

        stateBuilder = new StateBuilder();
        builder = new CienaSaos8PlatformAugBuilder();
        cienaBuilder = new CienaPlatformAugBuilder();
        Saos8ComponentStateReader.parsePort(stateBuilder, builder, cienaBuilder,
                Saos8ComponentReaderTest.OUTPUT_XCVR_PORT, "4/11");
        expectedState = new StateBuilder().setName("4/11").setId("Port");
        expectedCienaAug = new CienaPlatformAugBuilder();
        expectedAug = new CienaSaos8PlatformAugBuilder()
                .setEmpty(false)
                .setVendorPartNumber("CIENA-INN XCVR-Q10V31 RevB")
                .setEthernetType("100GE-QSFP28:LC")
                .setDiagnosticData(true);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedCienaAug.build(), cienaBuilder.build());
    }
}