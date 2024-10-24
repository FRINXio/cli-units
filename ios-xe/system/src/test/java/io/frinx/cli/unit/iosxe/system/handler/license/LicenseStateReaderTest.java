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
package io.frinx.cli.unit.iosxe.system.handler.license;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.license.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoLicenseExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoLicenseExtensionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoProductExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoProductExtensionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoReservationExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoReservationExtensionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoSmartLicensingExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.CiscoSmartLicensingExtensionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.product.extension.ActiveUdiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.product.extension.StandbyUdiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.product.extension.UdiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.smart.licensing.extension.DataPrivacyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.smart.licensing.extension.LicenseAuthorizationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.smart.licensing.extension.MiscellaneousBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.smart.licensing.extension.RegistrationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.license.extension.rev221202.cisco.smart.licensing.extension.TransportBuilder;

class LicenseStateReaderTest {

    @Test
    void parseLicenseFieldsTest() {
        final String output = """
                CBR8 DOCSIS 3.0 Downstream Channel License Feature (DS_License):
                  Description: CBR8 DOCSIS 3.0 Downstream Channel License Feature
                  Count: 436
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED

                CBR8 DOCSIS 3.0 Upstream Channel License Feature (US_License):
                  Description: CBR8 DOCSIS 3.0 Upstream Channel License Feature
                  Count: 81
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED

                cBR DOCSIS 3.0 Line-card N+1 HA License Feature (LCHA_License):
                  Description: cBR DOCSIS 3.0 Line-card HA License Feature
                  Count: 1
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED

                CBR8 VOD/SDV Downstream Video QAM License Feature (NC_License):
                  Description: CBR8 VOD/SDV Downstream Video QAM License Feature
                  Count: 16
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED

                cBR8 D3.1 Downstream License (DS_D31_License):
                  Description: cBR DOCSIS 3.1 6MHz Downstream License
                  Count: 180
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED

                cBR8 D3.1 Upstream License (US_D31_License):
                  Description: cBR DOCSIS 3.1 1MHz Upstream Exclusive License
                  Count: 62
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED

                CBR8 - Supervisor 10G Port License (WAN_License):
                  Description: CBR8 - Supervisor 10G Port License
                  Count: 5
                  Version: 1.0
                  Status: AUTHORIZED
                  Export status: NOT RESTRICTED""";

        var builder = new StateBuilder();
        LicenseStateReader.parseLicenseFields(output, "US_D31_License", builder);
        CiscoLicenseExtensionAugBuilder augBuilder = new CiscoLicenseExtensionAugBuilder();
        var expected = new StateBuilder()
                .addAugmentation(CiscoLicenseExtensionAug.class, new CiscoLicenseExtensionAugBuilder()
                        .setDescription("cBR DOCSIS 3.1 1MHz Upstream Exclusive License")
                        .setCount(Short.valueOf("62"))
                        .setVersion("1.0")
                        .setStatus("AUTHORIZED")
                        .setExportStatus("NOT RESTRICTED")
                        .build());
        assertEquals(expected.build(), builder.build());

    }

    @Test
    void parseSmartLicensingTest() {
        final String output = """
                Smart Licensing is ENABLED

                Utility:
                  Status: DISABLED

                Data Privacy:
                  Sending Hostname: yes
                    Callhome hostname privacy: DISABLED
                    Smart Licensing hostname privacy: DISABLED
                  Version privacy: DISABLED

                Transport:
                  Type: Callhome

                Registration:
                  Status: REGISTERED
                  Smart Account: Ziggo
                  Virtual Account: VFZ Access Engineering
                  Export-Controlled Functionality: ALLOWED
                  Initial Registration: SUCCEEDED on Nov 04 09:01:27 2022 CET
                  Last Renewal Attempt: SUCCEEDED on May 16 10:01:19 2022 CET
                  Next Renewal Attempt: May 03 09:01:26 2023 CET
                  Registration Expires: Nov 04 08:55:22 2023 CET

                License Authorization:\s
                  Status: AUTHORIZED on Dec 06 23:14:52 2022 CET
                  Last Communication Attempt: SUCCEEDED on Dec 06 23:14:52 2022 CET
                  Next Communication Attempt: Jan 05 23:14:52 2023 CET
                  Communication Deadline: Mar 06 23:09:51 2023 CET

                Export Authorization Key:
                  Features Authorized:
                    <none>

                Miscellaneous:
                  Custom Id: <empty>""";

        var builder = new StateBuilder();
        LicenseStateReader.parseSmartLicensing(output, builder);
        var expected = new StateBuilder()
                .addAugmentation(CiscoSmartLicensingExtensionAug.class, new CiscoSmartLicensingExtensionAugBuilder()
                        .setSmartLicensingStatus("ENABLED")
                        .setDataPrivacy(new DataPrivacyBuilder()
                                .setSendingHostname("yes")
                                .setCallhomeHostnamePrivacy("DISABLED")
                                .setVersionPrivacy("DISABLED")
                                .setSmartLicensingHostnamePrivacy("DISABLED")
                                .build())
                        .setRegistration(new RegistrationBuilder()
                                .setSmartAccount("Ziggo")
                                .setVirtualAccount("VFZ Access Engineering")
                                .setInitialRegistration("SUCCEEDED on Nov 04 09:01:27 2022 CET")
                                .setExportControlledFunctionality("ALLOWED")
                                .setLastRenewalAttempt("SUCCEEDED on May 16 10:01:19 2022 CET")
                                .setNextRenewalAttempt("May 03 09:01:26 2023 CET")
                                .setRegistrationExpires("Nov 04 08:55:22 2023 CET")
                                .build())
                        .setLicenseAuthorization(new LicenseAuthorizationBuilder()
                                .setLicenseAuthorizationStatus("AUTHORIZED on Dec 06 23:14:52 2022 CET")
                                .setCommunicationDeadline("Mar 06 23:09:51 2023 CET")
                                .setLastCommunicationAttempt("SUCCEEDED on Dec 06 23:14:52 2022 CET")
                                .setNextCommunicationAttempt("Jan 05 23:14:52 2023 CET")
                                .build())
                        .setTransport(new TransportBuilder().setType("Callhome").build())
                        .setMiscellaneous(new MiscellaneousBuilder().setCustomId("<empty>").build())
                        .build());
        assertEquals(expected.build(), builder.build());
    }

    @Test
    void parseUdiTest() {
        final String output = """
                UDI: PID:CBR-8-CCAP-CHASS,SN:FXS2220Q20A

                HA UDI List:
                    Active:PID:CBR-8-CCAP-CHASS,SN:FXS2220Q20A
                    Standby:PID:CBR-8-CCAP-CHASS,SN:FXS2220Q20A""";

        var builder = new StateBuilder();
        LicenseStateReader.parseUdi(output, builder);
        var expected = new StateBuilder()
                .addAugmentation(CiscoProductExtensionAug.class, new CiscoProductExtensionAugBuilder()
                        .setUdi(new UdiBuilder()
                                .setPid("CBR-8-CCAP-CHASS")
                                .setSn("FXS2220Q20A").build())
                        .setActiveUdi(new ActiveUdiBuilder()
                                .setPid("CBR-8-CCAP-CHASS")
                                .setSn("FXS2220Q20A").build())
                        .setStandbyUdi(new StandbyUdiBuilder()
                                .setPid("CBR-8-CCAP-CHASS")
                                .setSn("FXS2220Q20A").build())
                        .build());
        assertEquals(expected.build(), builder.build());
    }

    @Test
    void parseReservationTest() {
        final String output = "License reservation: DISABLED";

        var builder = new StateBuilder();
        LicenseStateReader.parseReservation(output, builder);
        var expected = new StateBuilder()
                .addAugmentation(CiscoReservationExtensionAug.class, new CiscoReservationExtensionAugBuilder()
                        .setReservationStatus("DISABLED")
                        .build());
        assertEquals(expected.build(), builder.build());
    }
}
