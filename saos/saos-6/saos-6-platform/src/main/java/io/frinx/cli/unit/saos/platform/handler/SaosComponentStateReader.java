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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaPlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SaosComponentStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_DEVICE_ID = "chassis show device-id";
    private static final String SH_SOFTWARE = "software show";
    private static final String SH_CAPABILITIES = "chassis show capabilities";
    private static final String SH_POWER = "chassis show power";
    private static final String SH_PORT = "port xcvr show port %s vendor";
    private static final String SH_PORTS = "port xcvr show";
    private static final Pattern DEVICE_TYPE = Pattern.compile("\\| Device Type *\\| (?<deviceType>[^|]+)\\|.*");
    private static final Pattern PART_NUMBER_REVISION = Pattern.compile(
            "\\| Part Number/Revision *\\| (?<partNumber>[^|]+)/(?<revision>[^|]+)\\|.*");
    private static final Pattern SERIAL_NUMBER = Pattern.compile("\\| Serial Number *\\| (?<serialNumber>[^|]+)\\|.*");
    private static final Pattern MANUFACTURED_DATE = Pattern.compile(
            "\\| Manufactured Date *\\| (?<manufacturedDate>[^|]+)\\|.*");
    private static final Pattern CLEI_CODE = Pattern.compile("\\| CLEI Code *\\| (?<clei>[^|]+)\\|.*");
    private static final Pattern LOCATION = Pattern.compile(
            "\\| Location of Manufacture *\\| (?<location>[^|]+)\\|.*");
    private static final Pattern MAC_ADDRESS = Pattern.compile(
            "\\| Chassis MAC Address *\\| (?<macAddress>[^|]+)\\|.*");
    private static final Pattern PARAM_VERSION = Pattern.compile("\\| Param Version *\\| (?<paramVersion>[^|]+)\\|.*");
    private static final Pattern SOFTWARE_PKG = Pattern.compile("\\| Running Package *: (?<pkg>[^\\|]+)\\|.*");
    private static final Pattern CAPABILITIES_NAME = Pattern.compile("\\| Platform Name *\\| (?<name>[^\\|]+)\\|.*");
    private static final Pattern CAPABILITIES_DESCRIPTION = Pattern
            .compile("\\| Platform Description *\\| (?<desc>[^\\|]+)\\|.*");
    private static final Pattern PORT_IDENTIFIER = Pattern.compile(
            "\\| Identifier *\\| [A-Za-z0-9]+ *\\| (?<identifier>[^\\|]+)\\|.*");
    private static final Pattern PORT_EXT_IDENTIFIER = Pattern.compile(
            "\\| Ext\\. Identifier *\\| [A-Za-z0-9]+ *\\| (?<extIdentifier>[^\\|]+)\\|.*");
    private static final Pattern PORT_CONNECTOR = Pattern.compile(
            "\\| Connector *\\| [A-Za-z0-9]+ *\\| (?<connector>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES = Pattern.compile(
            "\\| Transceiver Codes *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodes>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_10_GBE_COMP = Pattern.compile(
            "\\|  - 10 GbE Compliance *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodes10GBe>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_SONET_COMP = Pattern.compile(
            "\\|  - SONET Compliance *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodesSonet>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_ETHERNET_COMP = Pattern.compile(
            "\\|  - Ethernet Compliance *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodesEthernet>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_LINK_LENGTH = Pattern.compile(
            "\\|  - Link Length *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodesLinkLength>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_TRANSMITTER_TECH = Pattern.compile(
            "\\|  - Transmitter Technology *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodesTransmitterTech>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_TRANSMISSION_MEDIA = Pattern.compile(
            "\\|  - Transmission Media *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodesTransmissionMedia>[^\\|]+)\\|.*");
    private static final Pattern PORT_TRANSCEIVER_CODES_CHANNEL_SPEED = Pattern.compile(
            "\\|  - Channel speed *\\| [A-Za-z0-9]+ *\\| (?<transceiverCodesChannelSpeed>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENCODING = Pattern.compile(
            "\\| Encoding *\\| [A-Za-z0-9]+ *\\| (?<encoding>[^\\|]+)\\|.*");
    private static final Pattern PORT_BR_NOMINAL = Pattern.compile(
            "\\| BR, Nominal *\\| [A-Za-z0-9]+ *\\| (?<brNominal>[^\\|]+)\\|.*");
    private static final Pattern PORT_LENGTH_FIBER_1_KM = Pattern.compile(
            "\\| Length\\(9um fiber\\) 1km *\\| [A-Za-z0-9]+ *\\| (?<lengthFiber1Km>[^\\|]+)\\|.*");
    private static final Pattern PORT_LENGTH_FIBER_100_M = Pattern.compile(
            "\\| Length\\(9um fiber\\) 100m *\\| [A-Za-z0-9]+ *\\| (?<lengthFiber100M>[^\\|]+)\\|.*");
    private static final Pattern PORT_LENGTH_50_UM_10_M = Pattern.compile(
            "\\| Length\\(50um\\) 10m *\\| [A-Za-z0-9]+ *\\| (?<length50Um10M>[^\\|]+)\\|.*");
    private static final Pattern PORT_LENGTH_62_5_UM_10_M = Pattern.compile(
            "\\| Length\\(62\\.5um\\) 10m *\\| [A-Za-z0-9]+ *\\| (?<length625Um10M>[^\\|]+)\\|.*");
    private static final Pattern PORT_LENGTH_COPPER_1_M = Pattern.compile(
            "\\| Length\\(copper\\) 1m *\\| [A-Za-z0-9]+ *\\| (?<lengthCopper1M>[^\\|]+)\\|.*");
    private static final Pattern PORT_VENDOR_NAME = Pattern.compile("\\| Vendor Name *\\| (?<name>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_OUI = Pattern.compile("\\| Vendor OUI *\\| (?<oui>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_PN = Pattern.compile("\\| Vendor PN *\\| (?<pn>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_REV = Pattern.compile("\\| Vendor Revision *\\| (?<rev>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_SN = Pattern.compile("\\| Vendor Serial Number *\\| (?<sn>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_CLEI = Pattern.compile("\\| Vendor CLEI Code *\\| (?<clei>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_CIENA_PN = Pattern.compile("\\| Ciena PN *\\| (?<cienaPN>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_CIENA_REV = Pattern.compile(
            "\\| Ciena Revision *\\| (?<cienaRev>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_WAVE_LENGTH = Pattern.compile(
            "\\| Wavelength *\\| (?<wavelength>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_OPTIONS = Pattern.compile(
            "\\| Options *\\| [A-Za-z0-9]+ *\\| (?<options>[^\\|]+)\\|.*");
    private static final Pattern PORT_OPTIONS_TUNABLE = Pattern.compile(
            "\\|  - Tunable *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<optionsTunable>[^\\|]+)\\|.*");
    private static final Pattern PORT_OPTIONS_RATE_SELECT = Pattern.compile(
            "\\|  - RATE_SELECT *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<optionsRateSelect>[^\\|]+)\\|.*");
    private static final Pattern PORT_OPTIONS_TX_DISABLE = Pattern.compile(
            "\\|  - TX_DISABLE *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<optionsTxDisable>[^\\|]+)\\|.*");
    private static final Pattern PORT_OPTIONS_TX_FAULT = Pattern.compile(
            "\\|  - TX_FAULT *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<optionsTxFault>[^\\|]+)\\|.*");
    private static final Pattern PORT_OPTIONS_LOSS_OF_SIGNAL_INVERT = Pattern.compile(
            "\\|  - Loss of Signal Invert *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<optionsLossOfSignalInvert>[^\\|]+)\\|.*");
    private static final Pattern PORT_OPTIONS_LOSS_OF_SIGNAL = Pattern.compile(
            "\\|  - Loss of Signal *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<optionsLossOfSignal>[^\\|]+)\\|.*");
    private static final Pattern PORT_BR_MAX = Pattern.compile(
            "\\| BR, max *\\| [A-Za-z0-9]+ *\\| (?<brMax>[^\\|]+)\\|.*");
    private static final Pattern PORT_BR_MIN = Pattern.compile(
            "\\| BR, min *\\| [A-Za-z0-9]+ *\\| (?<brMin>[^\\|]+)\\|.*");
    private static final Pattern PORT_DATE = Pattern.compile(
            "\\| Date \\(mm/dd/yy\\) *\\| (?<date>[^\\|]+)\\| *\\|.*");
    private static final Pattern PORT_DIAG_MONITOR_TYPE = Pattern.compile(
            "\\| Diag Monitor Type *\\| [A-Za-z0-9]+ *\\| (?<diagMonitorType>[^\\|]+)\\|.*");
    private static final Pattern PORT_DIAG_MONITOR_TYPE_LEGACY_DIAGNOSTICS = Pattern.compile(
            "\\|  - Legacy diagnostics *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<diagMonitorTypeLegalDiag>[^\\|]+)\\|.*");
    private static final Pattern PORT_DIAG_MONITOR_TYPE_DIAGNOSTICS_MON = Pattern.compile(
            "\\|  - Diagnostics monitoring *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<diagMonitorTypeDiagMon>[^\\|]+)\\|.*");
    private static final Pattern PORT_DIAG_MONITOR_TYPE_INTERNALLY_CALIB = Pattern.compile(
            "\\|  - Internally calibrated *\\| [A-Za-z0-9]+ [0-9]* *\\| "
                    + "(?<diagMonitorTypeInternallyCalib>[^\\|]+)\\|.*");
    private static final Pattern PORT_DIAG_MONITOR_TYPE_EXTERNALLY_CALIB = Pattern.compile(
            "\\|  - Externally calibrated *\\| [A-Za-z0-9]+ [0-9]* *\\| "
                    + "(?<diagMonitorTypeExternallyCalib>[^\\|]+)\\|.*");
    private static final Pattern PORT_DIAG_MONITOR_TYPE_RW_POWER_MEASUR = Pattern.compile(
            "\\|  - Rx power measurement *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<diagMonitorTypeRwPowerMeasur>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENHANCED_OPTIONS = Pattern.compile(
            "\\| Enhanced Options *\\| [A-Za-z0-9]+ *\\| (?<enhancedOptions>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENHANCED_OPTIONS_ALARM_WARNING_FLAGS = Pattern.compile(
            "\\|  - Alarm/Warning Flags *\\| [A-Za-z0-9]+ [0-9]* *\\| "
                    + "(?<enhancedOptionsAlarmWarningsFlags>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENHANCED_OPTIONS_SOFT_TX_DISABLE = Pattern.compile(
            "\\|  - Soft TX_DISABLE *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<enhancedOptionsSoftTxDisable>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENHANCED_OPTIONS_SOFT_TX_FAULT = Pattern.compile(
            "\\|  - Soft TX_FAULT *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<enhancedOptionsSoftTxFault>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENHANCED_OPTIONS_SOFT_RX_LOS = Pattern.compile(
            "\\|  - Soft RX_LOS *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<enhancedOptionsSoftRxLos>[^\\|]+)\\|.*");
    private static final Pattern PORT_ENHANCED_OPTIONS_SOFT_RATE_SELECT = Pattern.compile(
            "\\|  - Soft RATE_SELECT *\\| [A-Za-z0-9]+ [0-9]* *\\| (?<enhancedOptionsSoftRateSelect>[^\\|]+)\\|.*");
    private static final Pattern PORT_SFF_8472_COMPLIANCE = Pattern.compile(
            "\\| SFF-8472 Compliance *\\| [A-Za-z0-9]+ *\\| (?<identifierSff8472Compliance>[^\\|]+)\\|.*");
    private static final String PORT_PID = "\\|%s *\\|Ena *\\|(\\S+)? *\\|(?<vpid>[^\\|]+)\\|.*\\|.*\\|.*\\|.*";
    private static final String POWER = "\\+---------------- POWER SUPPLY %s .*"
            + "\\| Part Number *\\| (?<pn>[^\\|]+)\\| "
            + "\\| Serial Number *\\| (?<sn>[^\\|]+)\\| "
            + "\\| Revision *\\| (?<rev>[^\\|]+)\\|.*";

    private final Cli cli;

    public SaosComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var name = id.firstKeyOf(Component.class).getName();
        var builder = new CienaPlatformAugBuilder();
        if (name.equals(OsComponent.OS_KEY.getName())) {
            parseOs(stateBuilder, builder, blockingRead(f(SH_DEVICE_ID), cli, id, readContext)
                    + blockingRead(f(SH_SOFTWARE), cli, id, readContext)
                    + blockingRead(f(SH_CAPABILITIES), cli, id, readContext));
            stateBuilder.addAugmentation(CienaPlatformAug.class, builder.build());
        } else if (name.startsWith(SaosComponentReader.POWER_SUPPLY_PREFIX)) {
            var power = name.replaceFirst(SaosComponentReader.POWER_SUPPLY_PREFIX, "");
            parsePower(stateBuilder, builder, blockingRead(f(SH_POWER), cli, id, readContext), power);
            stateBuilder.addAugmentation(CienaPlatformAug.class, builder.build());
        } else if (name.startsWith(SaosComponentReader.PORT_PREFIX_CONST)) {
            var port = name.replaceFirst(SaosComponentReader.PORT_PREFIX_CONST, "");
            parsePort(stateBuilder, builder, blockingRead(f(SH_PORT, port), cli, id, readContext)
                    + blockingRead(f(SH_PORTS), cli, id, readContext), port);
            stateBuilder.addAugmentation(CienaPlatformAug.class, builder.build());
        }
    }

    static void parseOs(@NotNull StateBuilder stateBuilder,
                        CienaPlatformAugBuilder builder,
                        String output) {
        stateBuilder.setName(OsComponent.OS_NAME);
        stateBuilder.setId(OsComponent.OS_NAME);

        ParsingUtils.parseFields(output, 0,
                DEVICE_TYPE::matcher,
                m -> m.group("deviceType"),
                v -> builder.setDeviceType(v.trim()));

        ParsingUtils.parseFields(output, 0,
                PART_NUMBER_REVISION::matcher,
                m -> m.group("partNumber"),
                v -> builder.setDevicePartNumber(v.trim()));

        ParsingUtils.parseFields(output, 0,
                PART_NUMBER_REVISION::matcher,
                m -> m.group("revision"),
                v -> builder.setDeviceRevision(v.trim()));

        ParsingUtils.parseFields(output, 0,
            SERIAL_NUMBER::matcher,
            m -> m.group("serialNumber"),
            v -> builder.setDeviceSn(v.trim()));

        ParsingUtils.parseFields(output, 0,
                MANUFACTURED_DATE::matcher,
                m -> m.group("manufacturedDate"),
                v -> builder.setDeviceManufacturedDate(v.trim()));

        ParsingUtils.parseFields(output, 0,
                CLEI_CODE::matcher,
                m -> m.group("clei"),
                v -> builder.setDeviceCleiCode(v.trim()));

        ParsingUtils.parseFields(output, 0,
                LOCATION::matcher,
                m -> m.group("location"),
                v -> builder.setLocation(v.trim()));

        ParsingUtils.parseFields(output, 0,
                MAC_ADDRESS::matcher,
                m -> m.group("macAddress"),
                v -> builder.setMacAddress(v.trim()));

        ParsingUtils.parseFields(output, 0,
            PARAM_VERSION::matcher,
            m -> m.group("paramVersion"),
            v -> builder.setDeviceParamVersion(v.trim()));

        ParsingUtils.parseFields(output, 0,
            SOFTWARE_PKG::matcher,
            m -> m.group("pkg"),
            v -> builder.setSoftwareRunningPackage(v.trim()));

        ParsingUtils.parseFields(output, 0,
            CAPABILITIES_NAME::matcher,
            m -> m.group("name"),
            v -> builder.setPlatformName(v.trim()));

        ParsingUtils.parseFields(output, 0,
            CAPABILITIES_DESCRIPTION::matcher,
            m -> m.group("desc"),
            v -> builder.setPlatformDescription(v.trim()));
    }

    static void parsePower(@NotNull StateBuilder stateBuilder,
                           CienaPlatformAugBuilder builder,
                           String output, String name) {
        stateBuilder.setName(name);
        stateBuilder.setId("Power_Supply");
        output = processPowerTable(output);

        ParsingUtils.parseField(output, 0,
            Pattern.compile(String.format(POWER, name))::matcher,
            m -> m.group("pn"),
            v -> builder.setPowerPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            Pattern.compile(String.format(POWER, name))::matcher,
            m -> m.group("sn"),
            v -> builder.setPowerSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            Pattern.compile(String.format(POWER, name))::matcher,
            m -> m.group("rev"),
            v -> builder.setPowerRevision(v.trim()));
    }

    static void parsePort(@NotNull StateBuilder stateBuilder,
                          CienaPlatformAugBuilder builder,
                          String output, String name) {
        stateBuilder.setName(name);
        stateBuilder.setId("Port");

        ParsingUtils.parseField(output, 0,
                PORT_IDENTIFIER::matcher,
                m -> m.group("identifier"),
                v -> builder.setIdentifier(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_EXT_IDENTIFIER::matcher,
                m -> m.group("extIdentifier"),
                v -> builder.setExtIdentifier(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_CONNECTOR::matcher,
                m -> m.group("connector"),
                v -> builder.setConnector(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES::matcher,
                m -> m.group("transceiverCodes"),
                v -> builder.setTransceiverCodes(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_10_GBE_COMP::matcher,
                m -> m.group("transceiverCodes10GBe"),
                v -> builder.setTransceiverCodes10GbeCompliance(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_SONET_COMP::matcher,
                m -> m.group("transceiverCodesSonet"),
                v -> builder.setTransceiverCodesSonetCompliance(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_ETHERNET_COMP::matcher,
                m -> m.group("transceiverCodesEthernet"),
                v -> builder.setTransceiverCodesEthernetCompliance(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_LINK_LENGTH::matcher,
                m -> m.group("transceiverCodesLinkLength"),
                v -> builder.setTransceiverCodesLinkLength(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_TRANSMITTER_TECH::matcher,
                m -> m.group("transceiverCodesTransmitterTech"),
                v -> builder.setTransceiverCodesTransmitterTechnology(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_TRANSMISSION_MEDIA::matcher,
                m -> m.group("transceiverCodesTransmissionMedia"),
                v -> builder.setTransceiverCodesTransmissionMedia(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_TRANSCEIVER_CODES_CHANNEL_SPEED::matcher,
                m -> m.group("transceiverCodesChannelSpeed"),
                v -> builder.setTransceiverCodesChannelSpeed(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENCODING::matcher,
                m -> m.group("encoding"),
                v -> builder.setEncoding(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_BR_NOMINAL::matcher,
                m -> m.group("brNominal"),
                v -> builder.setBrNominal(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_LENGTH_FIBER_1_KM::matcher,
                m -> m.group("lengthFiber1Km"),
                v -> builder.setLengthFiber1Km(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_LENGTH_FIBER_100_M::matcher,
                m -> m.group("lengthFiber100M"),
                v -> builder.setLengthFiber100M(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_LENGTH_50_UM_10_M::matcher,
                m -> m.group("length50Um10M"),
                v -> builder.setLength50um10M(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_LENGTH_62_5_UM_10_M::matcher,
                m -> m.group("length625Um10M"),
                v -> builder.setLength625um10M(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_LENGTH_COPPER_1_M::matcher,
                m -> m.group("lengthCopper1M"),
                v -> builder.setLengthCopper1M(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_VENDOR_NAME::matcher,
                m -> m.group("name"),
                v -> builder.setVendorName(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OUI::matcher,
                m -> m.group("oui"),
                v -> builder.setVendorOui(v.trim()));

        ParsingUtils.parseField(output, 0,
            PORT_PN::matcher,
            m -> m.group("pn"),
            v -> builder.setVendorPortNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            PORT_REV::matcher,
            m -> m.group("rev"),
            v -> builder.setVendorRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            PORT_SN::matcher,
            m -> m.group("sn"),
            v -> builder.setVendorSn(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_CLEI::matcher,
                m -> m.group("clei"),
                v -> builder.setVendorCleiCode(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_CIENA_PN::matcher,
                m -> m.group("cienaPN"),
                v -> builder.setCienaPortNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_CIENA_REV::matcher,
                m -> m.group("cienaRev"),
                v -> builder.setCienaRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_WAVE_LENGTH::matcher,
                m -> m.group("wavelength"),
                v -> builder.setWavelength(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS::matcher,
                m -> m.group("options"),
                v -> builder.setOptions(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS_TUNABLE::matcher,
                m -> m.group("optionsTunable"),
                v -> builder.setOptionsTunable(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS_RATE_SELECT::matcher,
                m -> m.group("optionsRateSelect"),
                v -> builder.setOptionsRateSelect(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS_TX_DISABLE::matcher,
                m -> m.group("optionsTxDisable"),
                v -> builder.setOptionsTxDisable(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS_TX_FAULT::matcher,
                m -> m.group("optionsTxFault"),
                v -> builder.setOptionsTxFault(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS_LOSS_OF_SIGNAL_INVERT::matcher,
                m -> m.group("optionsLossOfSignalInvert"),
                v -> builder.setOptionsLossOfSignalInvert(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_OPTIONS_LOSS_OF_SIGNAL::matcher,
                m -> m.group("optionsLossOfSignal"),
                v -> builder.setOptionsLossOfSignal(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_BR_MAX::matcher,
                m -> m.group("brMax"),
                v -> builder.setBrMax(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_BR_MIN::matcher,
                m -> m.group("brMin"),
                v -> builder.setBrMin(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DATE::matcher,
                m -> m.group("date"),
                v -> builder.setDate(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DIAG_MONITOR_TYPE::matcher,
                m -> m.group("diagMonitorType"),
                v -> builder.setDiagMonitorType(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DIAG_MONITOR_TYPE_LEGACY_DIAGNOSTICS::matcher,
                m -> m.group("diagMonitorTypeLegalDiag"),
                v -> builder.setDiagMonitorTypeLegacyDiagnostics(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DIAG_MONITOR_TYPE_DIAGNOSTICS_MON::matcher,
                m -> m.group("diagMonitorTypeDiagMon"),
                v -> builder.setDiagMonitorTypeDiagnosticsMonitoring(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DIAG_MONITOR_TYPE_INTERNALLY_CALIB::matcher,
                m -> m.group("diagMonitorTypeInternallyCalib"),
                v -> builder.setDiagMonitorTypeInternallyCalibrated(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DIAG_MONITOR_TYPE_EXTERNALLY_CALIB::matcher,
                m -> m.group("diagMonitorTypeExternallyCalib"),
                v -> builder.setDiagMonitorTypeExternallyCalibrated(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_DIAG_MONITOR_TYPE_RW_POWER_MEASUR::matcher,
                m -> m.group("diagMonitorTypeRwPowerMeasur"),
                v -> builder.setDiagMonitorTypeRwPowerMeasurement(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENHANCED_OPTIONS::matcher,
                m -> m.group("enhancedOptions"),
                v -> builder.setEnhancedOptions(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENHANCED_OPTIONS_ALARM_WARNING_FLAGS::matcher,
                m -> m.group("enhancedOptionsAlarmWarningsFlags"),
                v -> builder.setEnhancedOptionsAlarmWarningFlags(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENHANCED_OPTIONS_SOFT_TX_DISABLE::matcher,
                m -> m.group("enhancedOptionsSoftTxDisable"),
                v -> builder.setEnhancedOptionsSoftTxDisable(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENHANCED_OPTIONS_SOFT_TX_FAULT::matcher,
                m -> m.group("enhancedOptionsSoftTxFault"),
                v -> builder.setEnhancedOptionsSoftTxFault(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENHANCED_OPTIONS_SOFT_RX_LOS::matcher,
                m -> m.group("enhancedOptionsSoftRxLos"),
                v -> builder.setEnhancedOptionsSoftRxLos(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_ENHANCED_OPTIONS_SOFT_RATE_SELECT::matcher,
                m -> m.group("enhancedOptionsSoftRateSelect"),
                v -> builder.setEnhancedOptionsSoftRateSelect(v.trim()));

        ParsingUtils.parseField(output, 0,
                PORT_SFF_8472_COMPLIANCE::matcher,
                m -> m.group("identifierSff8472Compliance"),
                v -> builder.setSff8472Compliance(v.trim()));

        ParsingUtils.parseField(output, 0,
            Pattern.compile(String.format(PORT_PID, name))::matcher,
            m -> m.group("vpid"),
            v -> builder.setVendorPidPartNumber(v.trim()));
    }

    private static String processPowerTable(String output) {
        return output.replaceAll("\\n", " ")
                .replaceAll("\\r", "")
                .replaceAll("\\\\n", "")
                .replaceAll("\\+---------------- POWER SUPPLY", "\n+---------------- POWER SUPPLY");
    }
}