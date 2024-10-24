/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

public class XrOsComponentReaderTest {

    public static final String OUTPUT_INVENTORY = """
            Fri Aug  3 09:23:03.519 MET_DST
            NAME: "module 0/RP0/CPU0", DESCR: "ASR 99 Route Processor for Packet Transport"
            PID: A99-RP2-TR, VID: V03, SN: FOC2044N5HC

            NAME: "module 0/RP1/CPU0", DESCR: "ASR 99 Route Processor for Packet Transport"
            PID: A99-RP2-TR, VID: V03, SN: FOC2036NSFF

            NAME: "fantray 0/FT0/SP", DESCR: "ASR-9922 Fan Tray V2"
            PID: ASR-9922-FAN-V2, VID: V01, SN: FOC2130NZRD

            NAME: "fan0 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan1 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan2 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan3 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan4 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan5 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan6 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan7 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan8 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan9 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan10 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan11 0/FT0/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fantray 0/FT1/SP", DESCR: "ASR-9922 Fan Tray V2"
            PID: ASR-9922-FAN-V2, VID: V01, SN: FOC2130NZQU

            NAME: "fan0 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan1 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan2 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan3 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan4 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan5 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan6 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan7 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan8 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan9 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan10 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan11 0/FT1/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fantray 0/FT2/SP", DESCR: "ASR-9922 Fan Tray V2"
            PID: ASR-9922-FAN-V2, VID: V01, SN: FOC2130NZRF

            NAME: "fan0 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan1 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan2 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan3 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan4 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan5 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan6 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan7 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan8 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan9 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan10 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan11 0/FT2/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fantray 0/FT3/SP", DESCR: "ASR-9922 Fan Tray V2"
            PID: ASR-9922-FAN-V2, VID: V01, SN: FOC2130NZRL

            NAME: "fan0 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan1 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan2 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan3 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan4 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan5 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan6 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan7 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan8 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan9 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan10 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "fan11 0/FT3/SP", DESCR: "ASR9K Generic Fan"
            PID:    , VID: N/A, SN:\s

            NAME: "module 0/0/CPU0", DESCR: "400G Modular Linecard, Service Edge Optimized"
            PID: A9K-MOD400-SE, VID: V06, SN: FOC2124NJ8B

            NAME: "module 0/0/0", DESCR: "ASR 9000 20-port 10GE Modular Port Adapter"
            PID: A9K-MPA-20X10GE, VID: V03, SN: FOC2130P305

            NAME: "module mau 0/0/0/0", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270NF5    \s

            NAME: "module mau 0/0/0/1", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270NFL    \s

            NAME: "module mau 0/0/0/2", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MYF    \s

            NAME: "module mau 0/0/0/3", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MYM    \s

            NAME: "module mau 0/0/0/4", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MY5    \s

            NAME: "module mau 0/0/0/5", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MZ2    \s

            NAME: "module mau 0/0/0/6", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MY6    \s

            NAME: "module mau 0/0/0/7", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MY9    \s

            NAME: "module mau 0/0/0/8", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MYE    \s

            NAME: "module mau 0/0/0/9", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MY4    \s

            NAME: "module mau 0/0/0/10", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270N12    \s

            NAME: "module mau 0/0/0/11", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270NFN    \s

            NAME: "module mau 0/0/0/12", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270NFR    \s

            NAME: "module mau 0/0/0/13", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MYA    \s

            NAME: "module mau 0/0/0/14", DESCR: "10GBASE-ER 1550nm SMF 40KM"
            PID: SFP-10G-ER          , VID: V02 , SN: ONT2106004V    \s

            NAME: "module mau 0/0/0/15", DESCR: "10GBASE-ER 1550nm SMF 40KM"
            PID: SFP-10G-ER          , VID: V02 , SN: ONT210700CW    \s

            NAME: "module mau 0/0/0/16", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270N13    \s

            NAME: "module mau 0/0/0/17", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MY8    \s

            NAME: "module mau 0/0/0/18", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270NG2    \s

            NAME: "module mau 0/0/0/19", DESCR: "10GBASE-LR SFP+ Module for SMF"
            PID: SFP-10G-LR          , VID: V02 , SN: OPM21270MY7    \s

            NAME: "module 0/0/1", DESCR: "ASR 9000 2-port 100GE Modular Port Adapter"
            PID: A9K-MPA-2X100GE, VID: V03, SN: FOC2132N1AG

            NAME: "module mau 0/0/1/0", DESCR: "100GBASE-ER4 CFP2 Module for SMF (<40 km)"
            PID: CFP2-100G-ER4   , VID: V01 , SN: FLJ2052H00E    \s

            NAME: "module mau 0/0/1/1", DESCR: "CPAK 100G LR4"
            PID: CPAK-100G-LR4   , VID: V06 , SN: FBN21311718    \s

            NAME: "module 0/1/CPU0", DESCR: "400G Modular Linecard, Service Edge Optimized"
            PID: A9K-MOD400-SE, VID: V06, SN: FOC2124NJ5X

            NAME: "power-module 0/PS0/M0/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM212900YT

            NAME: "power-module 0/PS0/M1/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM2129010L

            NAME: "power-module 0/PS0/M2/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM2131020D

            NAME: "power-module 0/PS1/M0/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM2131022G

            NAME: "power-module 0/PS1/M1/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM21310228

            NAME: "power-module 0/PS1/M2/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM2131021B

            NAME: "power-module 0/PS2/M0/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM212900X7

            NAME: "power-module 0/PS2/M1/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM212900YK

            NAME: "power-module 0/PS2/M2/SP", DESCR: "6kW AC V3 Power Module"
            PID: PWR-6KW-AC-V3, VID: V03, SN: DTM2131020F

            NAME: "module 0/FC0/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2118NETW

            NAME: "module 0/FC1/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2131NMQ4

            NAME: "module 0/FC2/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2131NMSH

            NAME: "module 0/FC3/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2123NTEV

            NAME: "module 0/FC4/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2128NGDC

            NAME: "module 0/FC5/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2131NMVJ

            NAME: "module 0/FC6/SP", DESCR: "ASR 9900 Series Switch Fabric Card 2"
            PID: A99-SFC2, VID: V02 , SN: FOC2131NMTA

            NAME: "chassis ASR-9922", DESCR: "ASR 9922 20 Line Card Slot Chassis with V3 AC PEM"
            PID: ASR-9922, VID: V01, SN: FOX2131PJDT
            """;


    @Test
    void testParseComponents() throws Exception {
        List<ComponentKey> componentKeys = XrOsComponentReader.parseComponents(OUTPUT_INVENTORY);

        assertEquals(97, componentKeys.size());
    }
}