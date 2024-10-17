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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.ArrisPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.ChassisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.LicenseBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.LldpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.TransceiverBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.VersionDetailBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;

class ComponentStateReaderTest {

    private static final String OUTPUT_LLDP = """
            Global LLDP Information:
                Status: ACTIVE (All Ethernet interfaces)
                LLDP advertisements are sent every 30 seconds
                LLDP hold time advertised is 121 seconds""";

    private static final String OUTPUT_LICENSE = """
            system-legal-intercept        : Enabled
            system-principal-core         : Disabled
            system-auxiliary-core         : Disabled
            system-laes                   : Enabled
            system-calea                  : Disabled
            system-lld-us-asf             : Disabled

            Chassis based licensing:               --------------------------- Licenses ------------------------
                                                   Total     ---------- Primary -----------   ----- Spare ------
            Licensing Type                         License   License   Admin Up   Remaining   License  Remaining
            video-ncast-A             (VN-A)           192        96         46          50        96         66
            video-bcast-A             (VB-A)            52        51          1          50         1          0
            docsis-upstream-30        (D30-US)         352       256        256           0        96          0
            docsis-downstream-30-A    (D30A-DS)       1536      1024       1024           0       512          0
            docsis-downstream-ofdm    (OFDM-DS)       6144      3072       1936        1136      3072       1235
            docsis-upstream-ofdma     (OFDM-US)       5000      2500       1498        1002      2500       1002

            Note: Negative numbers in "Remaining" means there is not enough licensing to fully support provisioning

            Licensing Type                           ---- Chassis based Licenses Assigned per Slot ----
            Upstream                                  0:SPARE  1:UCAM2  2:UCAM2  3:UCAM2\s
            docsis-upstream-30           (D30-US)           0       80       96       64\s
            docsis-upstream-ofdma        (OFDM-US)          0     1498        0        0\s
            mac-docsis-us-30             (MD30-US)          0        0        0        0\s
            mac-docsis-us-ofdma          (MOFDMA-US)        0        0        0        0\s

            Downstream                               11:DCAM2 12:DCAM2 13:SPARE\s
            video-ncast-B                (VN-B/C)           0        0        0\s
            video-replica-ncast-B        (VRN-B/C)          0        0        0\s
            video-bcast-B                (VB-B)             0        0        0\s
            video-ncast-A                (VN-A)            30       16        0\s
            video-replica-ncast-A        (VRN-A)            0        0        0\s
            video-bcast-A                (VB-A)             0        1        0\s
            docsis-downstream-30-B       (D30B-DS)          0        0        0\s
            docsis-downstream-30-A       (D30A-DS)        512      448        0\s
            docsis-downstream-ofdm       (OFDM-DS)         99     1617        0\s
            mac-docsis-ds-30-B           (MD30B-DS)         0        0        0\s
            mac-docsis-ds-30-A           (MD30A-DS)         0        0        0\s
            mac-video-ncast-B            (MVN-B)            0        0        0\s
            mac-video-bcast-B            (MVB-B)            0        0        0\s
            mac-video-ncast-A            (MVN-A)            0        0        0\s
            mac-video-bcast-A            (MVB-A)            0        0        0\s
            mac-docsis-ds-ofdm           (MOFDM-DS)         0        0        0\s

            Chassis Serial Number: 18363CHS0066""";

    @Test
    void parseOsVersionsTest() {
        var stateBuilder = new StateBuilder();
        var builder = new ArrisPlatformAugBuilder();
        ComponentStateReader.parseOsVersions(stateBuilder, builder, OUTPUT_LLDP + "\n"
                + OUTPUT_LICENSE + "\n" + ComponentReaderTest.OUTPUT_CHASSIS + "\n"
                + ComponentReaderTest.OUTPUT_VERSION);

        var expectedState = new StateBuilder()
                .setName(OsComponent.OS_NAME);
        var expectedAug = new ArrisPlatformAugBuilder()
                .setLldp(new LldpBuilder()
                        .setLldpStatus("ACTIVE (All Ethernet interfaces)")
                        .setLldpAdInterval("30 seconds")
                        .setLldpAdHoldTime("121 seconds")
                        .build())
                .setLicense(new LicenseBuilder()
                        .setSystemLegalIntercept(true)
                        .setSystemPrincipalCore(false)
                        .setSystemAuxiliaryCore(false)
                        .setSystemLaes(true)
                        .setSystemCalea(false)
                        .setSystemLldUsAsf(false)
                        .setChassisSerialNumber("18363CHS0066")
                        .build())
                .setChassis(new ChassisBuilder()
                        .setChassisType("E6000")
                        .setModelName("CHAS-01014W")
                        .setModelVersion("A06")
                        .setSerialNumber("18363CHS0066")
                        .build())
                .setVersionDetail(new VersionDetailBuilder()
                        .setLastBootedTime("6 days, 20:39:49 (hr:min:sec)")
                        .build())
                .build();
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.getChassis(), builder.getChassis());
    }

    @Test
    void parsePortTransceiverTest() {
        var stateBuilder = new StateBuilder();
        var builder = new ArrisPlatformAugBuilder();
        ComponentStateReader.parsePortTransceiver(stateBuilder, builder, ComponentReaderTest.OUTPUT_PORT_TRANSCEIVER,
                "6/0");

        var expectedState = new StateBuilder()
                .setName("6/0")
                .setId("Transceiver");
        var expectedAug = new ArrisPlatformAugBuilder()
                .setTransceiver(new TransceiverBuilder()
                        .setTType("SFP")
                        .setSpeed("1G")
                        .setType("SFP COPPER")
                        .setVendor("FINISAR CORP.")
                        .setPartNumber("FCLF-8520-3")
                        .setRevision("A")
                        .setSerialNumber("PRD1KUV")
                        .setDateCode("140325")
                        .setTemperature("Not Available")
                        .setVoltage("Not Available")
                        .setCh0TxBias("Not Available")
                        .setCh0TxPower("Not Available")
                        .setCh0RxPower("Not Available")
                        .build());
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.getChassis(), builder.getChassis());
    }

    @Test
    void parseVersionChassisTest() {
        var stateBuilder = new StateBuilder();
        var builder = new ArrisPlatformAugBuilder();
        ComponentStateReader.parseVersionChassis(stateBuilder, builder, ComponentReaderTest.OUTPUT_CHASSIS, "CCM 0");
        var expectedState = new StateBuilder()
                .setName("CCM 0")
                .setId("Chassis");
        var expectedAug = new ArrisPlatformAugBuilder()
                .setChassis(new ChassisBuilder()
                        .setModelName("CCM-01014W")
                        .setModelVersion("A02")
                        .setSerialNumber("18266CCM0064")
                        .build());
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }

    @Test
    void parseVersionTest() {
        var stateBuilder = new StateBuilder();
        var builder = new ArrisPlatformAugBuilder();
        ComponentStateReader.parseVersion(stateBuilder, builder, ComponentReaderTest.OUTPUT_VERSION, "0");
        var expectedState = new StateBuilder()
                .setName("0")
                .setId("Version");
        var expectedAug = new ArrisPlatformAugBuilder()
                .setVersionDetail(new VersionDetailBuilder()
                        .setType("UCAM2")
                        .setModelName("UCAM-22431W")
                        .setModelVersion("C06")
                        .setSerialNumber("17043CUB0206")
                        .setCpuSpeed("1500 MHz")
                        .setBusSpeed("600.0 MHz")
                        .setRamSize("4096 MB")
                        .setNorFlashSize("128 MB")
                        .setNandFlashSize("14752 MB")
                        .setPicModelName("UPIC-0S024W")
                        .setPicModelVersion("C02")
                        .setPicSerialNumber("18217RSU0024")
                        .setFirmwareVersion("FW_UCAM2_V01.13")
                        .setActiveSw("CER_V10.01.00.0013")
                        .setReasonLastBooted("Reload")
                        .setUptime("6 days 20:36:05")
                        .build());
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }
}
