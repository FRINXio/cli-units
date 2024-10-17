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

package io.frinx.cli.unit.saos.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;

class SaosComponentStateReaderTest {

    @Test
    void parsePowerTest() {
        final var output = """
                +------------- POWER SUPPLY STATUS -------------+
                | Module  | Part Number  | Type       | State   |
                +---------+--------------+------------+---------+
                | PSA     | 170-0014-900 | AC         | Online  |
                | PSB     | N/A          | Unequipped | Offline |
                +---------+--------------+------------+---------+


                +---------------- POWER SUPPLY PSA -----------------+
                | Parameter                 | Value                 |
                +---------------------------+-----------------------+
                | Part Number               | 170-0014-900          |
                | Serial Number             | M7828843              |
                | Revision                  | ;;C                   |
                | CLEI Code                 | CMUPAABAAA            |
                | Manufactured Date         | 20141101              |
                | Input                     | AC                    |
                | Input Voltage             | 85-265                |
                | Output Voltage            | 12                    |
                | Manufacturing Location    | CHINA                 |
                | Checksum                  | 98                    |
                +---------------------------+-----------------------+

                Power Supply PSB info not available""";

        var stateBuilder = new StateBuilder();
        var builder = new CienaPlatformAugBuilder();
        SaosComponentStateReader.parsePower(stateBuilder, builder, output, "PSA");

        var expectedState = new StateBuilder()
                .setName("PSA")
                .setId("Power_Supply");
        var expectedAug = new CienaPlatformAugBuilder()
                .setPowerPartNumber("170-0014-900")
                .setPowerRevision(";;C")
                .setPowerSn("M7828843");
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parsePortTest() {
        var output = """
                +----+-----+-----+---------Transceiver-Status------------+-----+----------------+----+
                |    |Admin| Oper|                                       |Ciena|Ether Medium &  |Diag|
                |Port|State|State|      Vendor Name & Part Number        | Rev |Connector Type  |Data|
                +----+-----+-----+---------------------------------------+-----+----------------+----+
                |1   |Empty|     |                                       |     |                |    |
                |2   |Empty|     |                                       |     |                |    |
                |3   |Ena  |UCTF |CISCO-OEM CWDM-SFP-1350 Rev8.0         |     |1000BASE-LX/LC  |Yes |
                |4   |Empty|     |                                       |     |                |    |
                |5   |Empty|     |                                       |     |                |    |
                |6   |Empty|     |                                       |     |                |    |
                |7   |Empty|     |                                       |     |                |    |
                |8   |Empty|     |                                       |     |                |    |
                |9   |Ena  |     |CIENA-FIN XCVR-B00CRJ RevA             |A    |1000BASE-T/RJ45 |    |
                |10  |Ena  |UCTF |OEM CIS SFP-10G-LR Rev2.0              |     |10GBASE-LR/LC   |Yes |
                +----+-----+-----+---------------------------------------+-----+----------------+----+""";

        var stateBuilder = new StateBuilder();
        var builder = new CienaPlatformAugBuilder();
        SaosComponentStateReader.parsePort(stateBuilder, builder, output, "3");

        var expectedState = new StateBuilder()
                .setName("3")
                .setId("Port");
        var expectedAug = new CienaPlatformAugBuilder()
                .setVendorPidPartNumber("CISCO-OEM CWDM-SFP-1350 Rev8.0");
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());

        output = """
                +-------------------------- XCVR VENDOR DATA - Port 3    ---------------------------+
                | Parameter                | Value              | Decoded String Equivalent         |
                +--------------------------+--------------------+-----------------------------------+
                | Identifier               | 0x3                | SFP transceiver                   |
                | Ext. Identifier          | 0x4                | SFP/GBIC                          |
                | Connector                | 0x7                | LC                                |
                +--------------------------+--------------------+-----------------------------------+
                | Transceiver Codes        | 0x0000000002000000 |                                   |
                |  - 10 GbE Compliance     | 0x00               |                                   |
                |  - SONET Compliance      | 0x0000             |                                   |
                |  - Ethernet Compliance   | 0x02               | 1000BASE-LX                       |
                |  - Link Length           | 0x00               | unknown                           |
                |  - Transmitter Technology| 0x0000             | unknown                           |
                |  - Transmission Media    | 0x00               | unknown                           |
                |  - Channel speed         | 0x00               | unknown                           |
                +--------------------------+--------------------+-----------------------------------+
                | Encoding                 | 0x01               | 8B10B                             |
                | BR, Nominal              | 13                 | Gigabit                           |
                |--------------------------+--------------------+-----------------------------------|
                | Length(9um fiber) 1km    | 80                 | 80km                              |
                | Length(9um fiber) 100m   | 255                | 25500m                            |
                | Length(50um) 10m         | 0                  | 0m                                |
                | Length(62.5um) 10m       | 0                  | 0m                                |
                | Length(copper) 1m        | 0                  | 0m                                |
                |--------------------------+--------------------+-----------------------------------|
                | Vendor Name              | CISCO-OEM          |                                   |
                | Vendor OUI               | 0x009065           |                                   |
                | Vendor PN                | CWDM-SFP-1350      |                                   |
                | Vendor Revision          | 8.0                |                                   |
                | Vendor Serial Number     | B091113295-C8035   |                                   |
                | Vendor CLEI Code         | ABC                |                                   |
                | Ciena PN                 | ABC                |                                   |
                | Ciena Revision           | 1                  |                                   |
                | Wavelength               | 1350.00            |                                   |
                |--------------------------+--------------------+-----------------------------------|
                | Options                  | 0x1a               |                                   |
                |  - Tunable               | Bit 6              | No                                |
                |  - RATE_SELECT           | Bit 5              | No                                |
                |  - TX_DISABLE            | Bit 4              | Yes                               |
                |  - TX_FAULT              | Bit 3              | Yes                               |
                |  - Loss of Signal Invert | Bit 2              | No                                |
                |  - Loss of Signal        | Bit 1              | Yes                               |
                |--------------------------+--------------------+-----------------------------------|
                | BR, max                  | 0                  |                                   |
                | BR, min                  | 0                  |                                   |
                | Date (mm/dd/yy)          | 11/13/09           |                                   |
                |--------------------------+--------------------+-----------------------------------|
                | Diag Monitor Type        | 0x58               |                                   |
                |  - Legacy diagnostics    | Bit 7              | No                                |
                |  - Diagnostics monitoring| Bit 6              | Yes                               |
                |  - Internally calibrated | Bit 5              | No                                |
                |  - Externally calibrated | Bit 4              | Yes                               |
                |  - Rx power measurement  | Bit 3              | Avg                               |
                |--------------------------+--------------------+-----------------------------------|
                | Enhanced Options         | 0x90               |                                   |
                |  - Alarm/Warning Flags   | Bit 7              | Yes                               |
                |  - Soft TX_DISABLE       | Bit 6              | No                                |
                |  - Soft TX_FAULT         | Bit 5              | No                                |
                |  - Soft RX_LOS           | Bit 4              | Yes                               |
                |  - Soft RATE_SELECT      | Bit 3              | No                                |
                |--------------------------+--------------------+-----------------------------------|
                | SFF-8472 Compliance      | 0x1                | Rev 9.3                           |
                +--------------------------+--------------------+-----------------------------------+""";

        stateBuilder = new StateBuilder();
        builder = new CienaPlatformAugBuilder();
        SaosComponentStateReader.parsePort(stateBuilder, builder, output, "3");

        expectedState = new StateBuilder()
                .setName("3")
                .setId("Port");
        expectedAug = new CienaPlatformAugBuilder()
                .setIdentifier("SFP transceiver")
                .setExtIdentifier("SFP/GBIC")
                .setConnector("LC")
                .setTransceiverCodes("")
                .setTransceiverCodes10GbeCompliance("")
                .setTransceiverCodesSonetCompliance("")
                .setTransceiverCodesEthernetCompliance("1000BASE-LX")
                .setTransceiverCodesLinkLength("unknown")
                .setTransceiverCodesTransmitterTechnology("unknown")
                .setTransceiverCodesTransmissionMedia("unknown")
                .setTransceiverCodesChannelSpeed("unknown")
                .setEncoding("8B10B")
                .setBrNominal("Gigabit")
                .setLengthFiber1Km("80km")
                .setLengthFiber100M("25500m")
                .setLength50um10M("0m")
                .setLength625um10M("0m")
                .setLengthCopper1M("0m")
                .setVendorName("CISCO-OEM")
                .setVendorOui("0x009065")
                .setVendorPortNumber("CWDM-SFP-1350")
                .setVendorRevision("8.0")
                .setVendorSn("B091113295-C8035")
                .setVendorCleiCode("ABC")
                .setCienaPortNumber("ABC")
                .setCienaRevision("1")
                .setWavelength("1350.00")
                .setOptions("")
                .setOptionsTunable("No")
                .setOptionsRateSelect("No")
                .setOptionsTxDisable("Yes")
                .setOptionsTxFault("Yes")
                .setOptionsLossOfSignalInvert("No")
                .setOptionsLossOfSignal("Yes")
                .setBrMax("")
                .setBrMin("")
                .setDate("11/13/09")
                .setDiagMonitorType("")
                .setDiagMonitorTypeLegacyDiagnostics("No")
                .setDiagMonitorTypeDiagnosticsMonitoring("Yes")
                .setDiagMonitorTypeInternallyCalibrated("No")
                .setDiagMonitorTypeExternallyCalibrated("Yes")
                .setDiagMonitorTypeRwPowerMeasurement("Avg")
                .setEnhancedOptions("")
                .setEnhancedOptionsAlarmWarningFlags("Yes")
                .setEnhancedOptionsSoftTxDisable("No")
                .setEnhancedOptionsSoftTxFault("No")
                .setEnhancedOptionsSoftRxLos("Yes")
                .setEnhancedOptionsSoftRateSelect("No")
                .setSff8472Compliance("Rev 9.3")
                .setCienaRevision("1");
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parseOsTest() {
        var output = """
                +---------------- CHASSIS DEVICE ID ----------------+
                | Parameter                 |                       |
                +---------------------------+-----------------------+
                | Device Type               | 011                   |
                | Part Number/Revision      | 1231231231/A          |
                | Serial Number             | M1111111              |
                | Manufactured Date         | 20221025              |
                | CLEI Code                 | ABCD123ABC            |
                | Location of Manufacture   | 1                     |
                | Chassis MAC Address       | 00:00:00:00:00:00     |
                | Param Version             | 001                   |
                +---------------------------+-----------------------+""";

        var stateBuilder = new StateBuilder();
        var builder = new CienaPlatformAugBuilder();
        SaosComponentStateReader.parseOs(stateBuilder, builder, output);

        var expectedState = new StateBuilder()
                .setName(OsComponent.OS_NAME)
                .setId(OsComponent.OS_NAME);
        var expectedAug = new CienaPlatformAugBuilder()
                .setDeviceType("011")
                .setDevicePartNumber("1231231231")
                .setDeviceRevision("A")
                .setDeviceSn("M1111111")
                .setDeviceManufacturedDate("20221025")
                .setDeviceCleiCode("ABCD123ABC")
                .setLocation("1")
                .setMacAddress("00:00:00:00:00:00")
                .setDeviceParamVersion("001");
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());

        output = """
                +------------------------------------------------------------------------------+
                | Installed Package   : saos-06-17-01-0156                                     |
                | Running Package     : saos-06-17-01-0156                                     |
                | Application Build   : 17443                                                  |
                | Package Build Info  : Fri Dec 14 05:27:33 2018 autouser onxvpnjk02           |
                | Running Kernel      : 3.10.54-grsec                                          |
                | Running MIB Version : 04-17-01-0022                                          |
                | Release Status      : GA                                                     |
                +------------------------------------------------------------------------------+
                | Running bank        : A                                                      |
                | Bank package version: saos-06-17-01-0156                                     |
                | Bootloader version  : 17443                                                  |
                | Bootloader status   : valid                                                  |
                | Bank status         : valid (validated  5287hr  3min 33sec ago)              |
                | Standby bank        : B                                                      |
                | Bank package version: saos-06-17-01-0156                                     |
                | Bootloader version  : 17443                                                  |
                | Bootloader status   : valid                                                  |
                | Bank status         : valid (validated  5287hr  3min  9sec ago)              |
                +------------------------------------------------------------------------------+
                | Last command file: /tftpboot/ciena/packages/saos-06-17-01-0156/le-lnx.xml    |
                | Last configuration file: unknown                                             |
                +------------------------------------------------------------------------------+""";

        stateBuilder = new StateBuilder();
        builder = new CienaPlatformAugBuilder();
        SaosComponentStateReader.parseOs(stateBuilder, builder, output);

        expectedState = new StateBuilder()
                .setName(OsComponent.OS_NAME)
                .setId(OsComponent.OS_NAME);
        expectedAug = new CienaPlatformAugBuilder()
                .setSoftwareRunningPackage("saos-06-17-01-0156");
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedState.build(), stateBuilder.build());

        output = """
                +----------------------------------- PLATFORM CAPABILITIES --------------------------+
                | Parameter                        | Value                                           |
                +----------------------------------+-------------------------------------------------+
                | Capabilities Class               | 0                                               |
                | Platform Type                    | 087                                             |
                | Platform Name                    | 3930                                            |
                | Platform Description             | 3930 Service Delivery Switch                    |
                | No. Slots                        | 1                                               |
                | Primary Ctrl Blade               | 1                                               |
                | No. Fan Trays                    | 1                                               |
                | No. Fans Per Tray                | 3                                               |
                | Fixed DC Power                   | No                                              |
                | Redundant Power                  | Yes                                             |
                | Max Physical Ports               | 10                                              |
                | Max LAG Ports                    | 10                                              |
                | Max Ports Per LAG                | 8                                               |
                | Max IP Interfaces                | 64                                              |
                | Max VLANs                        | 4094                                            |
                | VLAN Translation                 | Supported                                       |
                | Max VLAN Cross Connections       | 4096                                            |
                | Global Inner TPID Configuration  | Supported                                       |
                | Max IGMP Snoop Service Instances | 500                                             |
                | Max Mcast Groups                 | 1022                                            |
                | Max Mcast Source Addrs           | 2048                                            |
                | Max Mcast Source Addrs Per Grp   | 64                                              |
                | Max Mcast Logical Interfaces     | 3000                                            |
                | Max Mcast Member Interfaces      | 20440                                           |
                | Max MSTIs                        | 16                                              |
                | Max RSTP Domains                 | 32                                              |
                | Max PM Instances                 | 1059 (29 auto, 1030 user)                       |
                | Max PM Bins (Class 0)            | 4026 (660 auto, 3366 user)                      |
                | Max PM Bins (Class 1)            | 29897 (297 auto, 29600 user)                    |
                | Max PM Interval Profiles         | 0                                               |
                | Protocol Filters                 | Not Supported                                   |
                | Max MPLS Ingress Tunnels         | 500                                             |
                | Max MPLS Egress Tunnels          | 500                                             |
                | Max MPLS Transit Tunnels         | 1000                                            |
                | Max MPLS VCs                     | 1000                                            |
                | Max MPLS L2VPNs                  | 1000                                            |
                | Max MPLS VSs                     | 1000                                            |
                |   Max MPLS VPWS VSs              | 1000                                            |
                |   Max MPLS VPLS VSs              | 500                                             |
                | Max Ethernet VCs                 | 1024                                            |
                | Max Default VSs                  | 1024                                            |
                | Max VSs                          | 1024                                            |
                | Max VS Subs                      | 4096                                            |
                | VS Subs Statistics Collection    | Supported                                       |
                | Max VS Subs with Stats           | 256                                             |
                | Max L2-CFT Profiles              | 64                                              |
                | Max Tagged PVST L2PT Instances   | 10                                              |
                | VC Transforms                    | Supported                                       |
                |   Max VC L2 Transforms           | 1                                               |
                |   Max VCs with L2 Transform      | 512                                             |
                | Egress Bandwidth Shaping         | Not Supported                                   |
                | Multi Subs Per Port              | True                                            |
                | Max Redundancy Groups            | 10                                              |
                | Max Links per Redundancy Group   | 1                                               |
                | VPLS FPGA Support                | False                                           |
                | PBT FPGA Support                 | False                                           |
                | L2 MAC Table Expansion           | Not Supported                                   |
                | Max DYNA-SA Entries              | 32000                                           |
                | Max FSMT Entries                 | 0                                               |
                | Max SRVC-LVL Entries             | 0                                               |
                | Max L2-SAC Entries               | 29                                              |
                | Max SMT Entries                  | 1024                                            |
                | Max EPR Snids                    | 0                                               |
                | Ring Protection                  | Supported                                       |
                | Max Logical Rings                | 9                                               |
                | Max Virtual Rings                | 18                                              |
                | Network Sync                     | Supported                                       |
                | BITS Timing                      | Supported                                       |
                | GPS Timing                       | Supported                                       |
                | PTP Timing                       | Supported                                       |
                | Dying Gasp                       | Supported                                       |
                | Benchmark                        | Supported                                       |
                | Max Benchmark Bandwidth (Mbps)   | 1000                                            |
                | Benchmark Reflector              | Supported                                       |
                | Benchmark Generator              | Supported                                       |
                | Max Benchmark Entities           | 1                                               |
                | Max Benchmark Concurrent Tests   | 32                                              |
                | Max Benchmark Test Inst Config   | 128                                             |
                | Max Benchmark Test Inst Enabled  | 32                                              |
                | Max Benchmark Profiles Config    | 128                                             |
                | Max Benchmark BW Profiles Config | 32                                              |
                | Max Benchmark KPI Profiles Config| 32                                              |
                | Max Benchmark EMIX Sequences     | 32                                              |
                | Max Benchmark Unique Active CVIDs| 16                                              |
                | Max ACL Profile Definitions      | 512                                             |
                | Max ACL Rule Definitions         | 1024                                            |
                | Max DHCP Relay Agents            | 512                                             |
                | Auxiliary Ping                   | Supported                                       |
                | MPLS VPLS IRB                    | Supported                                       |
                |   Max VPLS IRB Ports             | 2                                               |
                | Global RCOS->FCOS Maps           | 4                                               |
                | L2-Only RCOS->FCOS Maps          | 12                                              |
                | MEF Envelope Ingress Metering    | Not Supported                                   |
                | IP ECMP                          | Supported                                       |
                |   Max IP ECMP Groups             | 256                                             |
                | Linear APS                       | Supported                                       |
                |   Max Linear APS Groups          | 10                                              |
                | Max IP-ACL Rule Definitions      | 64                                              |
                +----------------------------------+-------------------------------------------------+""";

        stateBuilder = new StateBuilder();
        builder = new CienaPlatformAugBuilder();
        SaosComponentStateReader.parseOs(stateBuilder, builder, output);

        expectedState = new StateBuilder()
                .setName(OsComponent.OS_NAME)
                .setId(OsComponent.OS_NAME);
        expectedAug = new CienaPlatformAugBuilder()
                .setPlatformName("3930")
                .setPlatformDescription("3930 Service Delivery Switch");
        assertEquals(expectedAug.build(), builder.build());
        assertEquals(expectedState.build(), stateBuilder.build());
    }
}
