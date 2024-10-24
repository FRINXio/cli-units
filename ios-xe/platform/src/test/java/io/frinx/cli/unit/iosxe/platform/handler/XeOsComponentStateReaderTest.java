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

package io.frinx.cli.unit.iosxe.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoTransceiverAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoTransceiverAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.TRANSCEIVER;

class XeOsComponentStateReaderTest {

    @Test
    void parseFieldsTest() {
        final var output = """
            NAME: "Chassis", DESCR: "Cisco ASR920 Series - 2GE and 4-10GE - AC model"
            PID: ASR-920-4SZ-A     , VID: V02  , SN: CAT2323U04C

            NAME: " FIXED IM subslot 0/0", DESCR: "FIXED : 2-port Gig & 4-port Ten Gig Dual Ethernet Interface Module"
            PID:                   , VID: V00  , SN: N/A       \s

            NAME: "subslot 0/0 transceiver 3", DESCR: "SFP+ 10GBASE-LR"
            PID: SFP-10G-LR          , VID: V02  , SN: OPM2317209Q""";

        var builder = new StateBuilder();
        XeOsComponentStateReader.parseFields(builder, "Chassis", output);
        var expected = new StateBuilder()
                .setName("Chassis")
                .setId("Chassis")
                .setDescription("Cisco ASR920 Series - 2GE and 4-10GE - AC model")
                .setType(new Type(LINECARD.class))
                .addAugmentation(CiscoPlatformAug.class, new CiscoPlatformAugBuilder()
                        .setPid("ASR-920-4SZ-A")
                        .setVid("V02")
                        .setSn("CAT2323U04C")
                        .build());
        assertEquals(expected.build(), builder.build());
        builder = new StateBuilder();
        XeOsComponentStateReader.parseFields(builder, " FIXED IM subslot 0/0", output);
        expected = new StateBuilder()
                .setName(" FIXED IM subslot 0/0")
                .setId(" FIXED IM subslot 0/0")
                .setDescription("FIXED : 2-port Gig & 4-port Ten Gig Dual Ethernet Interface Module")
                .setType(new Type(LINECARD.class))
                .addAugmentation(CiscoPlatformAug.class, new CiscoPlatformAugBuilder()
                        .setPid("")
                        .setVid("V00")
                        .setSn("N/A")
                        .build());
        assertEquals(expected.build(), builder.build());
        builder = new StateBuilder();
        XeOsComponentStateReader.parseFields(builder, "subslot 0/0 transceiver 3", output);
        expected = new StateBuilder()
                .setName("subslot 0/0 transceiver 3")
                .setId("subslot 0/0 transceiver 3")
                .setDescription("SFP+ 10GBASE-LR")
                .setType(new Type(LINECARD.class))
                .addAugmentation(CiscoPlatformAug.class, new CiscoPlatformAugBuilder()
                        .setPid("SFP-10G-LR")
                        .setVid("V02")
                        .setSn("OPM2317209Q")
                        .build());
        assertEquals(expected.build(), builder.build());
    }

    @Test
    void parseFieldsTestV15() {
        final var output = """
                NAME: "Chassis", DESCR: "Cisco CSR1000V Chassis"
                PID: CSR1000V          , VID: V00, SN: 90MD4IFB0OJ

                NAME: "module R0", DESCR: "Cisco CSR1000V Route Processor"
                PID: CSR1000V          , VID: V00, SN: JAB1303001C

                NAME: "module F0", DESCR: "Cisco CSR1000V Embedded Services Processor"
                PID: CSR1000V          , VID:    , SN:""";

        var builder = new StateBuilder();
        XeOsComponentStateReader.parseFields(builder, "Chassis", output);
        var expected = new StateBuilder()
                .setName("Chassis")
                .setId("Chassis")
                .setDescription("Cisco CSR1000V Chassis")
                .setType(new Type(LINECARD.class))
                .addAugmentation(CiscoPlatformAug.class, new CiscoPlatformAugBuilder()
                        .setPid("CSR1000V")
                        .setVid("V00")
                        .setSn("90MD4IFB0OJ")
                        .build());
        assertEquals(expected.build(), builder.build());
        builder = new StateBuilder();
        XeOsComponentStateReader.parseFields(builder, "module R0", output);
        expected = new StateBuilder()
                .setName("module R0")
                .setId("module R0")
                .setDescription("Cisco CSR1000V Route Processor")
                .setType(new Type(LINECARD.class))
                .addAugmentation(CiscoPlatformAug.class, new CiscoPlatformAugBuilder()
                        .setPid("CSR1000V")
                        .setVid("V00")
                        .setSn("JAB1303001C")
                        .build());
        assertEquals(expected.build(), builder.build());
        builder = new StateBuilder();
        XeOsComponentStateReader.parseFields(builder, "module F0", output);
        expected = new StateBuilder()
                .setName("module F0")
                .setId("module F0")
                .setDescription("Cisco CSR1000V Embedded Services Processor")
                .setType(new Type(LINECARD.class))
                .addAugmentation(CiscoPlatformAug.class, new CiscoPlatformAugBuilder()
                        .setPid("CSR1000V")
                        .setVid("")
                        .setSn("")
                        .build());
        assertEquals(expected.build(), builder.build());
    }

    @Test
    void parseTransceiverTest() {
        final var output = """
                IDPROM for transceiver TenGigabitEthernet0/0/13:
                  Description                               = ABC or ABC+ optics (type 3)
                  Transceiver Type:                         = ABC+ 10GBASE-LR (274)
                  Product Identifier (PID)                  = ABC-10G-LR         \s
                  Vendor Revision                           = 4.0\s
                  Serial Number (SN)                        = ABC123     \s
                  Vendor Name                               = ABC    \s
                  Vendor OUI (IEEE company ID)              = 00.00.00 (0)
                  CLEI code                                 = 123ABC
                  Cisco part number                         = 11-1111-11
                  Device State                              = Enabled.
                  Date code (yy/mm/dd)                      = 17/08/30
                  Connector type                            = LC.
                  Encoding                                  = 64B/66B (6)
                  Nominal bitrate                           =  (10300 Mbits/s)
                  Minimum bit rate as % of nominal bit rate = not specified
                  Maximum bit rate as % of nominal bit rate = not specified""";

        var builder = new StateBuilder();
        XeOsComponentStateReader.parseTransceiver(builder, "subslot 0/0 transceiver 13", output);
        var expected = new StateBuilder()
                .setName("subslot 0/0 transceiver 13")
                .setId("subslot 0/0 transceiver 13")
                .setType(new Type(TRANSCEIVER.class))
                .addAugmentation(CiscoTransceiverAug.class, new CiscoTransceiverAugBuilder()
                        .setDescription("ABC or ABC+ optics (type 3)")
                        .setTransceiverType("ABC+ 10GBASE-LR (274)")
                        .setProductIdentifier("ABC-10G-LR")
                        .setVendorRevision("4.0")
                        .setSerialNumber("ABC123")
                        .setVendorName("ABC")
                        .setVendorOui("00.00.00 (0)")
                        .setCleiCode("123ABC")
                        .setCiscoPartNumber("11-1111-11")
                        .setDeviceState("Enabled.")
                        .setDateCode("17/08/30")
                        .setConnectorType("LC.")
                        .setEncoding("64B/66B (6)")
                        .setNominalBitrate("(10300 Mbits/s)")
                        .setMinimumBitrate("not specified")
                        .setMaximumBitrate("not specified")
                        .build());
        assertEquals(expected.build(), builder.build());
    }
}
