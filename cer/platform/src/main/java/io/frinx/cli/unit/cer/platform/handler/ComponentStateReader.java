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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.ArrisPlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.ArrisPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.ChassisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.LicenseBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.LldpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.TransceiverBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.arris.rev221124.arris.platform.inventory.extension.VersionDetailBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ComponentStateReader implements CliOperReader<State, StateBuilder> {

    private final Cli cli;

    public ComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_LLDP = "show LLDP";
    private static final String SH_LICENSE = "show license";

    private static final Pattern LLDP_STATUS = Pattern.compile("Status: (?<status>.*)");
    private static final Pattern LLDP_AD_INTERVAL = Pattern
            .compile("LLDP advertisements are sent every (?<interval>\\d+ \\S+).*");
    private static final Pattern LLDP_AD_HOLD_TIME = Pattern
            .compile("LLDP hold time advertised is (?<holdTime>\\d+ \\S+).*");
    private static final Pattern LIC_LEG_INTERCEPT = Pattern
            .compile("system-legal-intercept\\s+:\\s+(?<intercept>(Enabled|Disabled)).*");
    private static final Pattern LIC_PRINCIPAL_CORE = Pattern
            .compile("system-principal-core\\s+:\\s+(?<principalCore>(Enabled|Disabled)).*");
    private static final Pattern LIC_AUX_CORE = Pattern
            .compile("system-auxiliary-core\\s+:\\s+(?<auxCore>(Enabled|Disabled)).*");
    private static final Pattern LIC_LAES = Pattern.compile("system-laes\\s+:\\s+(?<laes>(Enabled|Disabled)).*");
    private static final Pattern LIC_CALEA = Pattern.compile("system-calea\\s+:\\s+(?<cal>(Enabled|Disabled)).*");
    private static final Pattern LIC_LLD = Pattern.compile("system-lld-us-asf\\s+:\\s+(?<lld>(Enabled|Disabled)).*");
    private static final Pattern LIC_PGS = Pattern.compile("system-us-pgs\\s+:\\s+(?<pgs>(Enabled|Disabled)).*");
    private static final Pattern LIC_CHASSIS_SN = Pattern.compile("Chassis Serial Number:\\s+(?<sn>\\S+).*");

    private static final String TRANSCEIVER_SLOT = ".*\\\\(?<output>slot/port: %s\\|[^\\\\]+)\\\\.*";
    private static final Pattern TR_T_TYPE = Pattern.compile("\\s*tType\\s+: (?<ttype>(\\s|\\S)+).*");
    private static final Pattern TR_SPEED = Pattern.compile("\\s*speed\\s+: (?<speed>(\\s|\\S)+).*");
    private static final Pattern TR_TYPE = Pattern.compile("\\s*type\\s+: (?<type>(\\s|\\S)+).*");
    private static final Pattern TR_VENDOR = Pattern.compile("\\s*vendor\\s+: (?<vendor>(\\s|\\S)+).*");
    private static final Pattern TR_PART_NUMBER = Pattern.compile("\\s*partNumber\\s+: (?<partNumber>(\\s|\\S)+).*");
    private static final Pattern TR_REVISION = Pattern.compile("\\s*revision\\s+: (?<revision>(\\s|\\S)+).*");
    private static final Pattern TR_S_NUMBER = Pattern.compile("\\s*serialNumber\\s+: (?<sn>(\\s|\\S)+).*");
    private static final Pattern TR_DATE_CODE = Pattern.compile("\\s*dateCode\\s+: (?<dateCode>(\\s|\\S)+).*");
    private static final Pattern TR_TEMP = Pattern.compile("\\s*temperature\\s+: (?<temperature>(\\s|\\S)+).*");
    private static final Pattern TR_VOLTAGE = Pattern.compile("\\s*voltage\\s+: (?<voltage>(\\s|\\S)+).*");
    private static final Pattern TR_TX_BIAS = Pattern.compile("\\s*Ch 0 txBias\\s+: (?<txBias>(\\s|\\S)+).*");
    private static final Pattern TR_TX_POWER = Pattern.compile("\\s*Ch 0 txPower\\s+: (?<txPower>(\\s|\\S)+)-*");
    private static final Pattern TR_RX_POWER = Pattern.compile("\\s*Ch 0 rxPower\\s+: (?<rxPower>(\\s|\\S)+).*");

    private static final String CHASSIS_SLOT = ".*\\\\(?<output>Module:\\s+%s\\|[^\\\\]+)\\\\.*";
    private static final Pattern CH_MODEL_NAME = Pattern.compile("\\s*Model Name:\\s+(?<name>(\\s|\\S)+).*");
    private static final Pattern CH_MODEL_VER = Pattern.compile("\\s*Model Version:\\s+(?<version>(\\s|\\S)+).*");
    private static final Pattern CH_MODEL_SN = Pattern.compile("\\s*Serial Number:\\s+(?<sn>(\\s|\\S)+).*");

    private static final Pattern CHASSIS_TYPE = Pattern
            .compile(".*\\\\(?<output>Chassis Type:\\s+\\S+[^\\\\]"
                    + "+Model Name:[^\\\\]+Model Version:[^\\\\]+Serial Number:[^\\\\]+)\\\\.*");
    private static final Pattern CH_TYPE = Pattern.compile("\\s*Chassis Type:\\s+(?<type>\\S+).*");

    private static final String VERSION_SLOT = ".*\\\\(?<output>Slot:\\s+%s\\|[^\\\\]+)\\\\.*";
    private static final Pattern VER_TYPE = Pattern.compile("\\s*Type:\\s+(?<type>(\\s|\\S)+).*");
    private static final Pattern VER_MODE_NAME = Pattern.compile("\\s*Model Name:\\s+(?<name>(\\s|\\S)+).*");
    private static final Pattern VER_MODEL_VERSION = Pattern.compile("\\s*Model Version:\\s+(?<ver>(\\s|\\S)+).*");
    private static final Pattern VER_SN = Pattern.compile("\\s*Serial Number:\\s+(?<sn>(\\s|\\S)+).*");
    private static final Pattern VER_CPU_SPEED = Pattern.compile("\\s*CPU Speed:\\s+(?<cpu>(\\s|\\S)+).*");
    private static final Pattern VER_BUS_SPEED = Pattern.compile("\\s*Bus Speed:\\s+(?<bus>(\\s|\\S)+).*");
    private static final Pattern VER_RAM_SIZE = Pattern.compile("\\s*RAM Size:\\s+(?<ram>(\\s|\\S)+).*");
    private static final Pattern VER_NOR_FL_SIZE = Pattern.compile("\\s*NOR  Flash Size:\\s+(?<nor>(\\s|\\S)+).*");
    private static final Pattern VER_NAND_FL_SIZE = Pattern.compile("\\s*NAND Flash Size:\\s+(?<nand>(\\s|\\S)+).*");
    private static final Pattern VER_PIC_MN = Pattern.compile("\\s*PIC Model Name:\\s+(?<picName>(\\s|\\S)+).*");
    private static final Pattern VER_PIC_MV = Pattern.compile("\\s*PIC Model Version:\\s+(?<picVersion>(\\s|\\S)+).*");
    private static final Pattern VER_PIC_SN = Pattern.compile("\\s*PIC Serial Number:\\s+(?<picSN>(\\s|\\S)+).*");
    private static final Pattern VER_FW_VERSION = Pattern.compile("\\s*Firmware Version:\\s+(?<FW>(\\s|\\S)+).*");
    private static final Pattern VER_ACTIVE_SW = Pattern.compile("\\s*Active SW:\\s+(?<SW>(\\s|\\S)+).*");
    private static final Pattern VER_ACT_PATCH = Pattern.compile("\\s*Active Patch:\\s+(?<patch>(\\s|\\S)+).*");
    private static final Pattern VER_BOOT_REASON = Pattern.compile("\\s*Reason Last Booted:\\s+(?<boot>(\\s|\\S)+).*");
    private static final Pattern VER_UPTIME = Pattern.compile("\\s*Uptime:\\s+(?<uptime>(\\s|\\S)+).*");

    private static final Pattern VER_TIME_LAST_BOOT = Pattern
            .compile("Time since the CMTS was last booted:\\s+(?<boottime>(\\s|\\S)+).*");
    private static final Pattern VER_EXEC_TIME = Pattern.compile("EXEC-TIME:\\s+(?<exectime>(\\s|\\S)+).*");

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var name = id.firstKeyOf(Component.class).getName();
        var builder = new ArrisPlatformAugBuilder();
        if (name.equals(OsComponent.OS_KEY.getName())) {
            parseOsVersions(stateBuilder, builder, blockingRead(f(SH_LLDP), cli, id, readContext) + "\n"
                    + blockingRead(f(SH_LICENSE), cli, id, readContext) + "\n"
                    + blockingRead(f(ComponentReader.SH_CH_VERSION), cli, id, readContext) + "\n"
                    + blockingRead(f(ComponentReader.SH_VERSION), cli, id, readContext));
        } else if (name.startsWith(ComponentReader.TRANSCEIVER_PREFIX)) {
            var transceiver = name.replaceFirst(ComponentReader.TRANSCEIVER_PREFIX, "");
            parsePortTransceiver(stateBuilder, builder, blockingRead(f(ComponentReader.SH_PORT_TR),
                    cli, id, readContext), transceiver);
        } else if (name.startsWith(ComponentReader.CHASSIS_PREFIX)) {
            var chassis = name.replaceFirst(ComponentReader.CHASSIS_PREFIX, "");
            parseVersionChassis(stateBuilder, builder, blockingRead(f(ComponentReader.SH_CH_VERSION),
                    cli, id, readContext), chassis);
        } else if (name.startsWith(ComponentReader.VERSION_PREFIX)) {
            var version = name.replaceFirst(ComponentReader.VERSION_PREFIX, "");
            parseVersion(stateBuilder, builder, blockingRead(f(ComponentReader.SH_VERSION),
                    cli, id, readContext), version);
        }
        stateBuilder.addAugmentation(ArrisPlatformAug.class, builder.build());
    }

    static void parseOsVersions(@NotNull StateBuilder stateBuilder,
                                @NotNull ArrisPlatformAugBuilder builder,
                                String output) {
        final var lldpBuilder = new LldpBuilder();
        final var licenseBuilder = new LicenseBuilder();
        final var chassisBuilder = new ChassisBuilder();
        final var versionBuilder = new VersionDetailBuilder();
        stateBuilder.setName(OsComponent.OS_NAME);

        ParsingUtils.parseFields(output, 0,
            LLDP_STATUS::matcher,
            matcher -> matcher.group("status"),
            lldpBuilder::setLldpStatus);

        ParsingUtils.parseField(output, 0,
            LLDP_AD_INTERVAL::matcher,
            matcher -> matcher.group("interval"),
            lldpBuilder::setLldpAdInterval);

        ParsingUtils.parseField(output, 0,
            LLDP_AD_HOLD_TIME::matcher,
            matcher -> matcher.group("holdTime"),
            lldpBuilder::setLldpAdHoldTime);

        if (lldpBuilder.getLldpStatus() != null || lldpBuilder.getLldpAdInterval() != null
                || lldpBuilder.getLldpAdHoldTime() != null) {
            builder.setLldp(lldpBuilder.build());
        }

        ParsingUtils.parseField(output, 0,
            LIC_LEG_INTERCEPT::matcher,
            matcher -> matcher.group("intercept"),
            val -> licenseBuilder.setSystemLegalIntercept(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_PRINCIPAL_CORE::matcher,
            matcher -> matcher.group("principalCore"),
            val -> licenseBuilder.setSystemPrincipalCore(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_AUX_CORE::matcher,
            matcher -> matcher.group("auxCore"),
            val -> licenseBuilder.setSystemAuxiliaryCore(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_LAES::matcher,
            matcher -> matcher.group("laes"),
            val -> licenseBuilder.setSystemLaes(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_CALEA::matcher,
            matcher -> matcher.group("cal"),
            val -> licenseBuilder.setSystemCalea(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_LLD::matcher,
            matcher -> matcher.group("lld"),
            val -> licenseBuilder.setSystemLldUsAsf(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_PGS::matcher,
            matcher -> matcher.group("pgs"),
            val -> licenseBuilder.setSystemUsPgs(isEnabled(val)));

        ParsingUtils.parseField(output, 0,
            LIC_CHASSIS_SN::matcher,
            matcher -> matcher.group("sn"),
            val -> licenseBuilder.setChassisSerialNumber(val.trim()));

        if (isLicenseBuilderNotEmpty(licenseBuilder)) {
            builder.setLicense(licenseBuilder.build());
        }

        var out = convertNewLinesInTables(output);
        var match = CHASSIS_TYPE.matcher(out);
        var str = match.matches() ? match.group("output") : "";
        str = convertToNewLines(str);

        ParsingUtils.parseField(str, 0,
            CH_TYPE::matcher,
            matcher -> matcher.group("type"),
            chassisBuilder::setChassisType);

        ParsingUtils.parseField(str, 0,
            CH_MODEL_NAME::matcher,
            matcher -> matcher.group("name"),
            chassisBuilder::setModelName);

        ParsingUtils.parseField(str, 0,
            CH_MODEL_VER::matcher,
            matcher -> matcher.group("version"),
            chassisBuilder::setModelVersion);

        ParsingUtils.parseField(str, 0,
            CH_MODEL_SN::matcher,
            matcher -> matcher.group("sn"),
            chassisBuilder::setSerialNumber);

        if (chassisBuilder.getChassisType() != null || chassisBuilder.getModelName() != null
                || chassisBuilder.getModelVersion() != null || chassisBuilder.getSerialNumber() != null) {
            builder.setChassis(chassisBuilder.build());
        }

        ParsingUtils.parseField(output, 0,
            VER_TIME_LAST_BOOT::matcher,
            matcher -> matcher.group("boottime"),
            versionBuilder::setLastBootedTime);

        ParsingUtils.parseField(output, 0,
            VER_EXEC_TIME::matcher,
            matcher -> matcher.group("exectime"),
            versionBuilder::setExecTime);

        if (versionBuilder.getLastBootedTime() != null || versionBuilder.getExecTime() != null) {
            builder.setVersionDetail(versionBuilder.build());
        }
    }

    static void parsePortTransceiver(@NotNull StateBuilder stateBuilder,
                                     @NotNull ArrisPlatformAugBuilder builder,
                                     @NotNull String output,
                                     @NotNull String name) {
        stateBuilder.setName(name);
        stateBuilder.setId("Transceiver");
        var out = convertNewLinesInTables(output);
        out = removeUnapproved(out);
        var match = Pattern.compile(String.format(TRANSCEIVER_SLOT, name)).matcher(out);
        var str = match.matches() ? match.group("output") : "";
        str = convertToNewLines(str);
        var transceiverBuilder = new TransceiverBuilder();

        ParsingUtils.parseField(str, 0,
            TR_T_TYPE::matcher,
            matcher -> matcher.group("ttype"),
            transceiverBuilder::setTType);

        ParsingUtils.parseField(str, 0,
            TR_SPEED::matcher,
            matcher -> matcher.group("speed"),
            transceiverBuilder::setSpeed);

        ParsingUtils.parseField(str, 0,
            TR_TYPE::matcher,
            matcher -> matcher.group("type"),
            transceiverBuilder::setType);

        ParsingUtils.parseField(str, 0,
            TR_VENDOR::matcher,
            matcher -> matcher.group("vendor"),
            transceiverBuilder::setVendor);

        ParsingUtils.parseField(str, 0,
            TR_PART_NUMBER::matcher,
            matcher -> matcher.group("partNumber"),
            transceiverBuilder::setPartNumber);

        ParsingUtils.parseField(str, 0,
            TR_REVISION::matcher,
            matcher -> matcher.group("revision"),
            transceiverBuilder::setRevision);

        ParsingUtils.parseField(str, 0,
            TR_S_NUMBER::matcher,
            matcher -> matcher.group("sn"),
            transceiverBuilder::setSerialNumber);

        ParsingUtils.parseField(str, 0,
            TR_DATE_CODE::matcher,
            matcher -> matcher.group("dateCode"),
            transceiverBuilder::setDateCode);

        ParsingUtils.parseField(str, 0,
            TR_TEMP::matcher,
            matcher -> matcher.group("temperature"),
            transceiverBuilder::setTemperature);

        ParsingUtils.parseField(str, 0,
            TR_VOLTAGE::matcher,
            matcher -> matcher.group("voltage"),
            transceiverBuilder::setVoltage);

        ParsingUtils.parseField(str, 0,
            TR_TX_BIAS::matcher,
            matcher -> matcher.group("txBias"),
            transceiverBuilder::setCh0TxBias);

        ParsingUtils.parseField(str, 0,
            TR_TX_POWER::matcher,
            matcher -> matcher.group("txPower"),
            transceiverBuilder::setCh0TxPower);

        ParsingUtils.parseField(str, 0,
            TR_RX_POWER::matcher,
            matcher -> matcher.group("rxPower"),
            transceiverBuilder::setCh0RxPower);

        if (isTransceiverBuilderNotEmpty(transceiverBuilder)) {
            builder.setTransceiver(transceiverBuilder.build());
        }
    }

    static void parseVersionChassis(@NotNull StateBuilder stateBuilder,
                                    @NotNull ArrisPlatformAugBuilder builder,
                                    @NotNull String output,
                                    @NotNull String name) {
        stateBuilder.setName(name);
        stateBuilder.setId("Chassis");
        var out = convertNewLinesInTables(output);
        var match = Pattern.compile(String.format(CHASSIS_SLOT, name)).matcher(out);
        var str = match.matches() ? match.group("output") : "";
        str = convertToNewLines(str);
        var chassisBuilder = new ChassisBuilder();

        ParsingUtils.parseField(str, 0,
            CH_MODEL_NAME::matcher,
            matcher -> matcher.group("name"),
            chassisBuilder::setModelName);

        ParsingUtils.parseField(str, 0,
            CH_MODEL_VER::matcher,
            matcher -> matcher.group("version"),
            chassisBuilder::setModelVersion);

        ParsingUtils.parseField(str, 0,
            CH_MODEL_SN::matcher,
            matcher -> matcher.group("sn"),
            chassisBuilder::setSerialNumber);

        if (chassisBuilder.getModelName() != null || chassisBuilder.getModelVersion() != null
                || chassisBuilder.getSerialNumber() != null) {
            builder.setChassis(chassisBuilder.build());
        }
    }

    static void parseVersion(@NotNull StateBuilder stateBuilder,
                             @NotNull ArrisPlatformAugBuilder builder,
                             @NotNull String output,
                             @NotNull String name) {
        stateBuilder.setName(name);
        stateBuilder.setId("Version");
        var out = convertNewLinesInTables(output);
        var match = Pattern.compile(String.format(VERSION_SLOT, name)).matcher(out);
        var str = match.matches() ? match.group("output") : "";
        str = convertToNewLines(str);
        var versionBuilder = new VersionDetailBuilder();

        ParsingUtils.parseField(str, 0,
            VER_TYPE::matcher,
            matcher -> matcher.group("type"),
            versionBuilder::setType);

        ParsingUtils.parseField(str, 0,
            VER_MODE_NAME::matcher,
            matcher -> matcher.group("name"),
            versionBuilder::setModelName);

        ParsingUtils.parseField(str, 0,
            VER_MODEL_VERSION::matcher,
            matcher -> matcher.group("ver"),
            versionBuilder::setModelVersion);

        ParsingUtils.parseField(str, 0,
            VER_SN::matcher,
            matcher -> matcher.group("sn"),
            versionBuilder::setSerialNumber);

        ParsingUtils.parseField(str, 0,
            VER_CPU_SPEED::matcher,
            matcher -> matcher.group("cpu"),
            versionBuilder::setCpuSpeed);

        ParsingUtils.parseField(str, 0,
            VER_BUS_SPEED::matcher,
            matcher -> matcher.group("bus"),
            versionBuilder::setBusSpeed);

        ParsingUtils.parseField(str, 0,
            VER_RAM_SIZE::matcher,
            matcher -> matcher.group("ram"),
            versionBuilder::setRamSize);

        ParsingUtils.parseField(str, 0,
            VER_NOR_FL_SIZE::matcher,
            matcher -> matcher.group("nor"),
            versionBuilder::setNorFlashSize);

        ParsingUtils.parseField(str, 0,
            VER_NAND_FL_SIZE::matcher,
            matcher -> matcher.group("nand"),
            versionBuilder::setNandFlashSize);

        ParsingUtils.parseField(str, 0,
            VER_PIC_MN::matcher,
            matcher -> matcher.group("picName"),
            versionBuilder::setPicModelName);

        ParsingUtils.parseField(str, 0,
            VER_PIC_MV::matcher,
            matcher -> matcher.group("picVersion"),
            versionBuilder::setPicModelVersion);

        ParsingUtils.parseField(str, 0,
            VER_PIC_SN::matcher,
            matcher -> matcher.group("picSN"),
            versionBuilder::setPicSerialNumber);

        ParsingUtils.parseField(str, 0,
            VER_FW_VERSION::matcher,
            matcher -> matcher.group("FW"),
            versionBuilder::setFirmwareVersion);

        ParsingUtils.parseField(str, 0,
            VER_ACTIVE_SW::matcher,
            matcher -> matcher.group("SW"),
            versionBuilder::setActiveSw);

        ParsingUtils.parseField(str, 0,
            VER_ACT_PATCH::matcher,
            matcher -> matcher.group("patch"),
            versionBuilder::setActivePatch);

        ParsingUtils.parseField(str, 0,
            VER_BOOT_REASON::matcher,
            matcher -> matcher.group("boot"),
            versionBuilder::setReasonLastBooted);

        ParsingUtils.parseField(str, 0,
            VER_UPTIME::matcher,
            matcher -> matcher.group("uptime"),
            versionBuilder::setUptime);

        if (isVersionBuilderNotEmpty(versionBuilder)) {
            builder.setVersionDetail(versionBuilder.build());
        }
    }

    private static Boolean isLicenseBuilderNotEmpty(LicenseBuilder licenseBuilder) {
        return licenseBuilder.getChassisSerialNumber() != null || licenseBuilder.isSystemLegalIntercept() != null
                || licenseBuilder.isSystemPrincipalCore() != null || licenseBuilder.isSystemAuxiliaryCore() != null
                || licenseBuilder.isSystemLaes() != null || licenseBuilder.isSystemCalea() != null
                || licenseBuilder.isSystemLldUsAsf() != null || licenseBuilder.isSystemUsPgs() != null;
    }

    private static Boolean isTransceiverBuilderNotEmpty(TransceiverBuilder transceiverBuilder) {
        return transceiverBuilder.getTType() != null || transceiverBuilder.getSpeed() != null
                || transceiverBuilder.getType() != null || transceiverBuilder.getVendor() != null
                || transceiverBuilder.getPartNumber() != null || transceiverBuilder.getRevision() != null
                || transceiverBuilder.getSerialNumber() != null || transceiverBuilder.getDateCode() != null
                || transceiverBuilder.getTemperature() != null || transceiverBuilder.getVoltage() != null
                || transceiverBuilder.getCh0TxBias() != null || transceiverBuilder.getCh0TxPower() != null
                || transceiverBuilder.getCh0RxPower() != null;
    }

    private static Boolean isVersionBuilderNotEmpty(VersionDetailBuilder versionBuilder) {
        return versionBuilder.getType() != null || versionBuilder.getModelName() != null
                || versionBuilder.getModelVersion() != null || versionBuilder.getSerialNumber() != null
                || versionBuilder.getCpuSpeed() != null || versionBuilder.getBusSpeed() != null
                || versionBuilder.getRamSize() != null || versionBuilder.getNorFlashSize() != null
                || versionBuilder.getNandFlashSize() != null || versionBuilder.getPicModelName() != null
                || versionBuilder.getPicModelVersion() != null || versionBuilder.getPicSerialNumber() != null
                || versionBuilder.getFirmwareVersion() != null || versionBuilder.getActiveSw() != null
                || versionBuilder.getActivePatch() != null || versionBuilder.getReasonLastBooted() != null
                || versionBuilder.getUptime() != null;
    }

    private static String convertNewLinesInTables(String output) {
        return "\\" + output.replaceAll("\\r", "")
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\n\\r", "\n")
                .replaceAll("\\nslot/port", "\\\\slot/port")
                .replaceAll("\\nModule", "\\\\Module")
                .replaceAll("\\nChassis", "\\\\Chassis")
                .replaceAll("\\nSlot", "\\\\Slot")
                .replaceAll("\\n\\n", "\\\\")
                .replaceAll("\\n", "|") + "\\";
    }

    private static boolean isEnabled(String testString) {
        return testString.trim().equals("Enabled");
    }

    private static String convertToNewLines(String output) {
        return output.replaceAll("\\|", "\r\n");
    }

    private static String removeUnapproved(String output) {
        return output
                .replaceAll("\\|\\|[^|]+Please contact ARRIS CMTS technical support for assistance.[^|]*\\|\\|", "||")
                .replaceAll("\\|[^|]+\\(Unapproved\\)\\*[^|]*\\|", "|");
    }
}