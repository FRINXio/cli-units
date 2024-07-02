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

package io.frinx.cli.unit.cer.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

public class ComponentReaderTest {

    public static final String OUTPUT_PORT_TRANSCEIVER = """
            slot/port: 6/0
              tType        : SFP
              speed        : 1G
              type         : SFP COPPER
              vendor       : FINISAR CORP.  \s
              partNumber   : FCLF-8520-3     \s
              revision     : A  \s
              serialNumber : PRD1KUV        \s
              dateCode     : 140325 \s
              temperature  : Not Available
              voltage      : Not Available
              Ch 0 txBias  : Not Available
              Ch 0 txPower : Not Available
              Ch 0 rxPower : Not Available
            slot/port: 6/3
              tType        : QSFP28
              speed        : 100G
              type         : QSFP28-UNKNOWN
              vendor       : SOURCE PHOTONICS
              partNumber   : SPQCEERCDFM      (Unapproved)*
              revision     : 01
              serialNumber : M7U2001482     \s
              dateCode     : 220629 \s
              temperature  : 24.3 C
              voltage      : 3.3 V                                    \s
              Ch 0 txBias  : Not Available
              Ch 0 txPower : -40.0 dBmW
              Ch 0 rxPower : -40.0 dBmW
              Ch 1 txBias  : Not Available
              Ch 1 txPower : -40.0 dBmW
              Ch 1 rxPower : -40.0 dBmW
              Ch 2 txBias  : Not Available
              Ch 2 txPower : -40.0 dBmW
              Ch 2 rxPower : -40.0 dBmW
              Ch 3 txBias  : Not Available
              Ch 3 txPower : -40.0 dBmW
              Ch 3 rxPower : -40.0 dBmW
            slot/port: 6/5
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : INNOLIGHT      \s
              partNumber   : TR-PX13L-N00     (Unapproved)*
              revision     : 1B \s
              serialNumber : INGBV0021149   \s
              dateCode     : 161207 \s
              temperature  : 34.9 C
              voltage      : 3.3 V
              Ch 0 txBias  : 30.1 mA
              Ch 0 txPower : -2.8 dBmW                                \s
              Ch 0 rxPower : -2.0 dBmW
            slot/port: 6/6
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : CISCO-SUMITOMO \s
              partNumber   : SPP5200LR-C6     (Unapproved)*
              revision     : B  \s
              serialNumber : SPC1831027Y    \s
              dateCode     : 140729KD
              temperature  : 35.3 C
              voltage      : 3.3 V
              Ch 0 txBias  : 49.8 mA
              Ch 0 txPower : -2.7 dBmW
              Ch 0 rxPower : -2.0 dBmW
            slot/port: 6/7
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : FINISAR CORP.  \s
              partNumber   : FTLX1475D3BNL   \s
              revision     : A  \s
              serialNumber : N57BACD        \s
              dateCode     : 210305 \s
              temperature  : 36.6 C                                   \s
              voltage      : 3.3 V
              Ch 0 txBias  : 39.0 mA
              Ch 0 txPower : -2.4 dBmW
              Ch 0 rxPower : -22.4 dBmW
            slot/port: 6/8
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : FINISAR CORP.  \s
              partNumber   : FTLX1475D3BNL   \s
              revision     : A  \s
              serialNumber : N55CCJP        \s
              dateCode     : 210305 \s
              temperature  : 34.6 C
              voltage      : 3.3 V
              Ch 0 txBias  : 35.3 mA
              Ch 0 txPower : -2.2 dBmW
              Ch 0 rxPower : -26.6 dBmW
            slot/port: 7/0
              tType        : SFP
              speed        : 1G
              type         : SFP COPPER
              vendor       : FINISAR CORP.  \s
              partNumber   : FCLF-8520-3     \s
              revision     : A                                        \s
              serialNumber : PRE1BFX        \s
              dateCode     : 140401 \s
              temperature  : Not Available
              voltage      : Not Available
              Ch 0 txBias  : Not Available
              Ch 0 txPower : Not Available
              Ch 0 rxPower : Not Available
            slot/port: 7/5
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : WTD            \s
              partNumber   : RTXM228-401      (Unapproved)*
              revision     : 1.0\s
              serialNumber : BP173901531248 \s
              dateCode     : 170924 \s
              temperature  : 35.4 C
              voltage      : 3.3 V
              Ch 0 txBias  : 35.3 mA
              Ch 0 txPower : -3.0 dBmW
              Ch 0 rxPower : -40.0 dBmW
            slot/port: 7/6
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR                                  \s
              vendor       : CISCO-FINISAR  \s
              partNumber   : FTLX1474D3BCL-C1 (Unapproved)*
              revision     : A  \s
              serialNumber : FNS191501WP    \s
              dateCode     : 150406 \s
              temperature  : 34.1 C
              voltage      : 3.3 V
              Ch 0 txBias  : 35.7 mA
              Ch 0 txPower : -2.0 dBmW
              Ch 0 rxPower : -25.5 dBmW
            slot/port: 7/7
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : FINISAR CORP.  \s
              partNumber   : FTLX1475D3BNL   \s
              revision     : A  \s
              serialNumber : N57BAC7        \s
              dateCode     : 210305 \s
              temperature  : 34.6 C
              voltage      : 3.3 V
              Ch 0 txBias  : 33.0 mA
              Ch 0 txPower : -2.1 dBmW
              Ch 0 rxPower : -22.8 dBmW
            slot/port: 7/8                                            \s
              tType        : SFP
              speed        : 10G
              type         : SFP+ LR
              vendor       : FINISAR CORP.  \s
              partNumber   : FTLX1475D3BNL   \s
              revision     : A  \s
              serialNumber : N57BAGY        \s
              dateCode     : 210305 \s
              temperature  : 30.7 C
              voltage      : 3.3 V
              Ch 0 txBias  : 34.3 mA
              Ch 0 txPower : -2.2 dBmW
              Ch 0 rxPower : -23.9 dBmW

            * Please contact ARRIS CMTS technical support for assistance.""";

    public static final String OUTPUT_CHASSIS = """
            Chassis Type: E6000
              Model Name:          CHAS-01014W
              Model Version:       A06
              Serial Number:       18363CHS0066
            Module:   CCM 0
              Model Name:          CCM-01014W
              Model Version:       A02
              Serial Number:       18266CCM0064
            Module:   CCM 1
              Model Name:          CCM-01014W
              Model Version:       A02
              Serial Number:       18266CCM0065
            Module:   PEM A
              Model Name:          PEM-01248W
              Model Version:       B02
              Serial Number:       18196PEM0074
            Module:   PEM B
              Model Name:          PEM-01248W
              Model Version:       B02
              Serial Number:       18206PEM0009
            Module:   Fan Tray 0
              Model Name:          FAN-01614W
              Model Version:       C01
              Serial Number:       18306FSB0172
            Module:   Fan Tray 1                                      \s
              Model Name:          FAN-01614W
              Model Version:       C01
              Serial Number:       18316FSB0062
            Module:   Fan Tray 2
              Model Name:          FAN-01614W
              Model Version:       C01
              Serial Number:       18306FSB0174""";

    public static final String OUTPUT_VERSION = """
            Chassis Type: E6000
            Time since the CMTS was last booted: 6 days, 20:39:49 (hr:min:sec)
            Slot:   0
              Type:                UCAM2
              Model Name:          UCAM-22431W
              Model Version:       C06
              Serial Number:       17043CUB0206
              CPU Speed:           1500 MHz
              Bus Speed:           600.0 MHz
              RAM Size:            4096 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     14752 MB
              PIC Model Name:      UPIC-0S024W
              PIC Model Version:   C02
              PIC Serial Number:   18217RSU0024
              Firmware Version:    FW_UCAM2_V01.13
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:05
              Card HW Deviation:   000
              PIC HW Deviation:    000
            Slot:   1
              Type:                UCAM2
              Model Name:          UCAM-22431W                        \s
              Model Version:       C06
              Serial Number:       17043CUB0181
              CPU Speed:           1500 MHz
              Bus Speed:           600.0 MHz
              RAM Size:            4096 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     14752 MB
              PIC Model Name:      UPIC-0A024W
              PIC Model Version:   D03
              PIC Serial Number:   18337RUP0089
              Firmware Version:    FW_UCAM2_V01.13
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:06
              Card HW Deviation:   000
              PIC HW Deviation:    000
            Slot:   2
              Type:                UCAM2
              Model Name:          UCAM-22431W
              Model Version:       C08
              Serial Number:       18107CUB0147
              CPU Speed:           1500 MHz
              Bus Speed:           600.0 MHz
              RAM Size:            4096 MB                            \s
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      UPIC
              PIC Model Version:   D02
              PIC Serial Number:   14345RUP0155
              Firmware Version:    FW_UCAM2_V03.00
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:07
              Card HW Deviation:   000
              PIC HW Deviation:    00
            Slot:   3
              Type:                UCAM2
              Model Name:          UCAM-22431W
              Model Version:       C07
              Serial Number:       18033CUB0016
              CPU Speed:           1500 MHz
              Bus Speed:           600.0 MHz
              RAM Size:            4096 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      UPIC
              PIC Model Version:   D02
              PIC Serial Number:   14335RUP0152                       \s
              Firmware Version:    FW_UCAM2_V03.00
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:07
              Card HW Deviation:   000
              PIC HW Deviation:    00
            Slot:   6
              Type:                RSM2
              Model Name:          RSM-22480W
              Model Version:       C07
              Serial Number:       18393RMB0098
              CPU Speed:           1500 MHz
              Bus Speed:           600.0 MHz
              RAM Size:            4096 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      RPIC-21Q8SW
              PIC Model Version:   B03
              PIC Serial Number:   18397RQB0152
              Firmware Version:    FW_RSM2_V02.05
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:39:51                    \s
              Card HW Deviation:   000
              PIC HW Deviation:    000
            Slot:   7
              Type:                RSM2
              Model Name:          RSM-22480W
              Model Version:       C07
              Serial Number:       18403RMB0030
              CPU Speed:           1500 MHz
              Bus Speed:           600.0 MHz
              RAM Size:            4096 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      RPIC-21Q8SW
              PIC Model Version:   B03
              PIC Serial Number:   18397RQB0113
              Firmware Version:    FW_RSM2_V02.05
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:07
              Card HW Deviation:   000
              PIC HW Deviation:    000
            Slot:  11
              Type:                DCAM2
              Model Name:          DCAM-21631W                        \s
              Model Version:       D08
              Serial Number:       18333CDB0039
              CPU Speed:           1000 MHz
              Bus Speed:           1600.0 MHz
              RAM Size:            1024 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      DPIC-2L016W\s
              PIC Model Version:   C02
              PIC Serial Number:   18117RDB0178
              Firmware Version:    FW_DCAM2_V04.02
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:07
              Card HW Deviation:   000
              PIC HW Deviation:    000
            Slot:  12
              Type:                DCAM2
              Model Name:          DCAM-21631W
              Model Version:       D07
              Serial Number:       18093CDB0195
              CPU Speed:           1000 MHz
              Bus Speed:           1600.0 MHz
              RAM Size:            1024 MB                            \s
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      DPIC-2L016W\s
              PIC Model Version:   C02
              PIC Serial Number:   18137RDB0050
              Firmware Version:    FW_DCAM2_V04.02
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:08
              Card HW Deviation:   000
              PIC HW Deviation:    000
            Slot:  13
              Type:                DCAM2
              Model Name:          DCAM-21631W
              Model Version:       D0B
              Serial Number:       20117CDB0120
              CPU Speed:           1000 MHz
              Bus Speed:           1600.0 MHz
              RAM Size:            1024 MB
              NOR  Flash Size:     128 MB
              NAND Flash Size:     15104 MB
              PIC Model Name:      DPIC-2S016W
              PIC Model Version:   B01
              PIC Serial Number:   18127RSB0028                       \s
              Firmware Version:    FW_DCAM2_V04.05
              Active SW:           CER_V10.01.00.0013
              Active Patch:      \s
              Reason Last Booted:  Reload
              Uptime:              6 days 20:36:08
              Card HW Deviation:   000
              PIC HW Deviation:    000""";

    @Test
    void parseAllTransceiverTest() {
        final var componentKeys = ComponentReader.parseAllTransceiver(OUTPUT_PORT_TRANSCEIVER);
        final var expectedKeys = List.of(
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "6/0"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "6/3"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "6/5"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "6/6"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "6/7"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "6/8"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "7/0"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "7/5"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "7/6"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "7/7"),
                new ComponentKey(ComponentReader.TRANSCEIVER_PREFIX + "7/8")
        );
        assertEquals(expectedKeys, componentKeys);
    }

    @Test
    void parseAllChassisTest() {
        final var componentKeys = ComponentReader.parseAllChassis(OUTPUT_CHASSIS);
        final var expectedKeys = List.of(
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "CCM 0"),
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "CCM 1"),
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "PEM A"),
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "PEM B"),
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "Fan Tray 0"),
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "Fan Tray 1"),
                new ComponentKey(ComponentReader.CHASSIS_PREFIX + "Fan Tray 2")
        );
        assertEquals(expectedKeys, componentKeys);
    }

    @Test
    void parseAllVersionTest() {
        final var componentKeys = ComponentReader.parseAllVersion(OUTPUT_VERSION);
        final var expectedKeys = List.of(
                new ComponentKey(ComponentReader.VERSION_PREFIX + "0"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "1"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "2"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "3"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "6"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "7"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "11"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "12"),
                new ComponentKey(ComponentReader.VERSION_PREFIX + "13")
        );
        assertEquals(expectedKeys, componentKeys);
    }
}