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

package io.frinx.cli.platform.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.OPENCONFIGHARDWARECOMPONENT;

public class ComponentStateReaderTest {

    public static final String OUTPUT2 = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  2   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL093481KB\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  2  0015.2b68.6268 to 0015.2b68.6297  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Sub-Module                  Model              Serial       Hw     Status \n" +
            "---- --------------------------- ------------------ ----------- ------- -------\n" +
            "  2  Cisco Voice Daughter Card   WS-F6K-VPWR-GE     SAL09327A70  1.1    Ok\n" +
            "\n" +
            "Mod  Online Diag Status \n" +
            "---- -------------------\n" +
            "  2  Pass\n";

    public static final String OUTPUT1 = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  1   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL0918ACD8\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  1  0013.c424.a638 to 0013.c424.a667  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Online Diag Status \n" +
            "---- -------------------\n" +
            "  1  Pass\n";

    public static final String OUTPUT5 = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  5    2  Supervisor Engine 720 (Active)         WS-SUP720-3B       SAL092961PK\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  5  0014.a97d.9eac to 0014.a97d.9eaf   4.4   8.1(3)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Sub-Module                  Model              Serial       Hw     Status \n" +
            "---- --------------------------- ------------------ ----------- ------- -------\n" +
            "  5  Policy Feature Card 3       WS-F6K-PFC3B       SAL09306ANA  2.1    Ok\n" +
            "  5  MSFC3 Daughterboard         WS-SUP720          SAL09295MYH  2.3    Ok\n" +
            "\n" +
            "Mod  Online Diag Status \n" +
            "---- -------------------\n" +
            "  5  Pass\n";

    public static final String OUTPUT_VERSION = "Cisco IOS Software, 7200 Software (C7200-ADVIPSERVICESK9-M), Version 15.2(4)S5, RELEASE SOFTWARE (fc1)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Thu 20-Feb-14 06:51 by prod_rel_team\n" +
            "\n" +
            "ROM: ROMMON Emulation Microcode\n" +
            "BOOTLDR: 7200 Software (C7200-ADVIPSERVICESK9-M), Version 15.2(4)S5, RELEASE SOFTWARE (fc1)\n" +
            "\n" +
            "R2 uptime is 6 days, 21 hours, 58 minutes\n" +
            "System returned to ROM by unknown reload cause - suspect boot_data[BOOT_COUNT] 0x0, BOOT_COUNT 0, BOOTDATA 19\n" +
            "System image file is \"tftp://255.255.255.255/unknown\"\n" +
            "Last reload reason: unknown reload cause - suspect boot_data[BOOT_COUNT] 0x0, BOOT_COUNT 0, BOOTDATA 19\n" +
            "\n" +
            "\n" +
            "\n" +
            "This product contains cryptographic features and is subject to United\n" +
            "States and local country laws governing import, export, transfer and\n" +
            "use. Delivery of Cisco cryptographic products does not imply\n" +
            "third-party authority to import, export, distribute or use encryption.\n" +
            "Importers, exporters, distributors and users are responsible for\n" +
            "compliance with U.S. and local country laws. By using this product you\n" +
            "agree to comply with applicable laws and regulations. If you are unable\n" +
            "to comply with U.S. and local laws, return this product immediately.\n" +
            "\n" +
            "A summary of U.S. laws governing Cisco cryptographic products may be found at:\n" +
            "http://www.cisco.com/wwl/export/crypto/tool/stqrg.html\n" +
            "\n" +
            "If you require further assistance please contact us by sending email to\n" +
            "export@cisco.com.\n" +
            "\n" +
            "Cisco 7206VXR (NPE400) processor (revision A) with 491520K/32768K bytes of memory.\n" +
            "Processor board ID 4279256517\n" +
            "R7000 CPU at 150MHz, Implementation 39, Rev 2.1, 256KB L2 Cache\n" +
            "6 slot VXR midplane, Version 2.1\n" +
            "\n" +
            "Last reset from power-on\n" +
            "\n" +
            "PCI bus mb0_mb1 (Slots 0, 1, 3 and 5) has a capacity of 600 bandwidth points.\n" +
            "Current configuration on bus mb0_mb1 has a total of 1000 bandwidth points. \n" +
            "The set of PA-2FE, PA-POS-2OC3, and I/O-2FE qualify for \"half \n" +
            "bandwidth points\" consideration, when full bandwidth point counting \n" +
            "results in oversubscription, under the condition that only one of the \n" +
            "two ports is used. With this adjustment, current configuration on bus \n" +
            "mb0_mb1 has a total of 1000 bandwidth points. \n" +
            "This configuration has oversubscripted the PCI bus and is not a \n" +
            "supported configuration. \n" +
            "\n" +
            "PCI bus mb2 (Slots 2, 4, 6) has a capacity of 600 bandwidth points.\n" +
            "Current configuration on bus mb2 has a total of 400 bandwidth points \n" +
            "This configuration is within the PCI bus capacity and is supported. \n" +
            "\n" +
            "Please refer to the following document \"Cisco 7200 Series Port Adaptor\n" +
            "Hardware Configuration Guidelines\" on Cisco.com <http://www.cisco.com>\n" +
            "for c7200 bandwidth points oversubscription and usage guidelines.\n" +
            "\n" +
            "WARNING: PCI bus mb0_mb1 Exceeds 600 bandwidth points\n" +
            "\n" +
            "1 FastEthernet interface\n" +
            "3 Gigabit Ethernet interfaces\n" +
            "509K bytes of NVRAM.\n" +
            "          \n" +
            "8192K bytes of Flash internal SIMM (Sector size 256K).\n" +
            "Configuration register is 0x2102\n";


    @Test
    public void testParse() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "2", OUTPUT2);
        assertEquals(new StateBuilder()
                        .setId("2")
                        .setDescription("SFM-capable 48 port 10/100/1000mb RJ45")
                        .setName("2")
                        .setPartNo("WS-X6548-GE-TX")
                        .setVersion("10.1")
                        .setSerialNo("SAL093481KB")
                        .setType(new PlatformComponentState.Type(LINECARD.class))
                        .build(),

                stateBuilder.build());

        stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "1", OUTPUT1);
        assertEquals(new StateBuilder()
                        .setId("1")
                        .setDescription("SFM-capable 48 port 10/100/1000mb RJ45")
                        .setName("1")
                        .setPartNo("WS-X6548-GE-TX")
                        .setVersion("10.1")
                        .setSerialNo("SAL0918ACD8")
                        .setType(new PlatformComponentState.Type(LINECARD.class))
                        .build(),

                stateBuilder.build());

        stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "5", OUTPUT5);
        assertEquals(new StateBuilder()
                        .setId("5")
                        .setDescription("Supervisor Engine 720 (Active)")
                        .setName("5")
                        .setPartNo("WS-SUP720-3B")
                        .setVersion("4.4")
                        .setSerialNo("SAL092961PK")
                        .setType(new PlatformComponentState.Type(LINECARD.class))
                        .build(),

                stateBuilder.build());

        stateBuilder = new StateBuilder();
        ComponentStateReader.parseOSVersions(stateBuilder, OUTPUT_VERSION);
        assertEquals(new StateBuilder()
                        .setId("IOS")
                        .setName("OS")
                        .setSoftwareVersion("15.2(4)S5")
                        .build(),

                stateBuilder.build());
    }
}