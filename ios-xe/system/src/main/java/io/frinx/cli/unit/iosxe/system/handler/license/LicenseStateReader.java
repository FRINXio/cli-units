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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.License;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.license.State;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LicenseStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_LICENSES = "show license usage";
    private static final Pattern LICENSE_DESCRIPTION = Pattern.compile("Description: (?<description>.+)");
    private static final Pattern LICENSE_COUNT = Pattern.compile("Count: (?<count>.+)");
    private static final Pattern LICENSE_VERSION = Pattern.compile("Version: (?<version>.+)");
    private static final Pattern LICENSE_STATUS = Pattern.compile("Status: (?<status>.+)");
    private static final Pattern LICENSE_EXPORT_STATUS = Pattern.compile("Export status: (?<exportStatus>.+)");

    private static final String SH_UDI = "show license udi";
    private static final Pattern UDI_LINE = Pattern.compile("UDI: PID:(?<pid>.+),SN:(?<sn>.+)");
    private static final Pattern UDI_ACTIVE = Pattern.compile("Active:PID:(?<pid>.+),SN:(?<sn>.+)");
    private static final Pattern UDI_STANDBY = Pattern.compile("Standby:PID:(?<pid>.+),SN:(?<sn>.+)");

    private static final String SH_RESERVATION = "show license reservation";
    private static final Pattern RESERVATION_LINE = Pattern.compile("License reservation: (?<status>.+)");

    private static final String SH_STATUS = "show license status";
    private static final Pattern STATUS_LINE = Pattern.compile("Smart Licensing is (?<status>.+)");
    private static final Pattern SENDING_HOSTNAME = Pattern.compile("Sending Hostname: (?<sendingHostname>.+)");
    private static final Pattern CALLHOME_PRIVACY = Pattern.compile(
            "Callhome hostname privacy: (?<callhomeHostnamePrivacy>.+)");
    private static final Pattern HOSTNAME_PRIVACY = Pattern.compile(
            "Smart Licensing hostname privacy: (?<smartLicensingHostnamePrivacy>.+)");
    private static final Pattern VERSION_PRIVACY = Pattern.compile("Version privacy: (?<versionPrivacy>.+)");
    private static final Pattern TYPE = Pattern.compile("Type: (?<type>.+)");
    private static final Pattern SMART_ACCOUNT = Pattern.compile("Smart Account: (?<smartAccount>.+)");
    private static final Pattern VIRTUAL_ACCOUNT = Pattern.compile("Virtual Account: (?<virtualAccount>.+)");
    private static final Pattern EXPORT_CONTROLLED = Pattern.compile(
            "Export-Controlled Functionality: (?<exportControlledFunctionality>.+)");
    private static final Pattern INITIAL_REGISTRATION = Pattern.compile(
            "Initial Registration: (?<initialRegistration>.+)");
    private static final Pattern LAST_RENEWAL = Pattern.compile("Last Renewal Attempt: (?<lastRenewalAttempt>.+)");
    private static final Pattern NEXT_RENEWAL = Pattern.compile("Next Renewal Attempt: (?<nextRenewalAttempt>.+)");
    private static final Pattern REGISTRATION_EXPIRES = Pattern.compile(
            "Registration Expires: (?<registrationExpires>.+)");
    private static final Pattern LICENSE_AUTHORIZATION_STATUS = Pattern.compile("Status: (?<status>.{20,})");
    private static final Pattern LAST_COMMUNICATION = Pattern.compile(
            "Last Communication Attempt: (?<lastCommunicationAttempt>.+)");
    private static final Pattern NEXT_COMMUNICATION = Pattern.compile(
            "Next Communication Attempt: (?<nextCommunicationAttempt>.+)");
    private static final Pattern COMMUNICATION_DEADLINE = Pattern.compile("Communication Deadline: (?<deadline>.+)");
    private static final Pattern CUSTOM_ID = Pattern.compile("Custom Id: (?<customId>.+)");

    private final Cli cli;

    public LicenseStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(License.class).getLicenseId();
        if (name.contains("Smart Licensing")) {
            parseSmartLicensing(blockingRead(f(SH_STATUS), cli, instanceIdentifier, readContext), stateBuilder);
        } else if (name.contains("Product Information")) {
            parseUdi(blockingRead(f(SH_UDI), cli, instanceIdentifier, readContext), stateBuilder);
        } else if (name.contains("Reservation Info")) {
            parseReservation(blockingRead(f(SH_RESERVATION), cli, instanceIdentifier, readContext), stateBuilder);
        } else {
            parseLicenseFields(blockingRead(f(SH_LICENSES), cli, instanceIdentifier, readContext), name, stateBuilder);
        }
    }

    static void parseLicenseFields(String output, String name, StateBuilder stateBuilder) {
        List<String> splittedOutput = Pattern.compile("\n").splitAsStream(output).collect(Collectors.toList());

        String description = null;
        String count = null;
        String version = null;
        String status = null;
        String exportStatus = null;
        for (int i = 0; i < splittedOutput.size(); i++) {
            if (splittedOutput.get(i).contains(name)) {
                description = splittedOutput.get(i + 1);
                count = splittedOutput.get(i + 2);
                version = splittedOutput.get(i + 3);
                status = splittedOutput.get(i + 4);
                exportStatus = splittedOutput.get(i + 5);
            }
        }

        CiscoLicenseExtensionAugBuilder builder = new CiscoLicenseExtensionAugBuilder();
        ParsingUtils.parseField(description, 0,
                LICENSE_DESCRIPTION::matcher,
                m -> m.group("description"),
                builder::setDescription);

        ParsingUtils.parseField(count, 0,
                LICENSE_COUNT::matcher,
                m -> m.group("count"),
                countt -> builder.setCount(Short.valueOf(countt)));

        ParsingUtils.parseField(version, 0,
                LICENSE_VERSION::matcher,
                m -> m.group("version"),
                builder::setVersion);

        ParsingUtils.parseField(status, 0,
                LICENSE_STATUS::matcher,
                m -> m.group("status"),
                builder::setStatus);

        ParsingUtils.parseField(exportStatus, 0,
                LICENSE_EXPORT_STATUS::matcher,
                m -> m.group("exportStatus"),
                builder::setExportStatus);

        stateBuilder.addAugmentation(CiscoLicenseExtensionAug.class, builder.build());
    }

    static void parseSmartLicensing(String output, StateBuilder stateBuilder) {
        CiscoSmartLicensingExtensionAugBuilder builder = new CiscoSmartLicensingExtensionAugBuilder();

        ParsingUtils.parseField(output, 0,
                STATUS_LINE::matcher,
                m -> m.group("status"),
                builder::setSmartLicensingStatus);

        DataPrivacyBuilder dataPrivacyBuilder = new DataPrivacyBuilder();
        ParsingUtils.parseField(output, 0,
                SENDING_HOSTNAME::matcher,
                m -> m.group("sendingHostname"),
                dataPrivacyBuilder::setSendingHostname);

        ParsingUtils.parseField(output, 0,
                CALLHOME_PRIVACY::matcher,
                m -> m.group("callhomeHostnamePrivacy"),
                dataPrivacyBuilder::setCallhomeHostnamePrivacy);

        ParsingUtils.parseField(output, 0,
                HOSTNAME_PRIVACY::matcher,
                m -> m.group("smartLicensingHostnamePrivacy"),
                dataPrivacyBuilder::setSmartLicensingHostnamePrivacy);

        ParsingUtils.parseField(output, 0,
                VERSION_PRIVACY::matcher,
                m -> m.group("versionPrivacy"),
                dataPrivacyBuilder::setVersionPrivacy);

        TransportBuilder transportBuilder = new TransportBuilder();
        ParsingUtils.parseField(output, 0,
                TYPE::matcher,
                m -> m.group("type"),
                transportBuilder::setType);

        RegistrationBuilder registrationBuilder = new RegistrationBuilder();
        ParsingUtils.parseField(output, 0,
                SMART_ACCOUNT::matcher,
                m -> m.group("smartAccount"),
                registrationBuilder::setSmartAccount);

        ParsingUtils.parseField(output, 0,
                VIRTUAL_ACCOUNT::matcher,
                m -> m.group("virtualAccount"),
                registrationBuilder::setVirtualAccount);

        ParsingUtils.parseField(output, 0,
                EXPORT_CONTROLLED::matcher,
                m -> m.group("exportControlledFunctionality"),
                registrationBuilder::setExportControlledFunctionality);

        ParsingUtils.parseField(output, 0,
                INITIAL_REGISTRATION::matcher,
                m -> m.group("initialRegistration"),
                registrationBuilder::setInitialRegistration);

        ParsingUtils.parseField(output, 0,
                LAST_RENEWAL::matcher,
                m -> m.group("lastRenewalAttempt"),
                registrationBuilder::setLastRenewalAttempt);

        ParsingUtils.parseField(output, 0,
                NEXT_RENEWAL::matcher,
                m -> m.group("nextRenewalAttempt"),
                registrationBuilder::setNextRenewalAttempt);

        ParsingUtils.parseField(output, 0,
                REGISTRATION_EXPIRES::matcher,
                m -> m.group("registrationExpires"),
                registrationBuilder::setRegistrationExpires);

        LicenseAuthorizationBuilder licenseAuthorizationBuilder = new LicenseAuthorizationBuilder();
        ParsingUtils.parseField(output, 0,
                LICENSE_AUTHORIZATION_STATUS::matcher,
                m -> m.group("status"),
                licenseAuthorizationBuilder::setLicenseAuthorizationStatus);

        ParsingUtils.parseField(output, 0,
                LAST_COMMUNICATION::matcher,
                m -> m.group("lastCommunicationAttempt"),
                licenseAuthorizationBuilder::setLastCommunicationAttempt);

        ParsingUtils.parseField(output, 0,
                NEXT_COMMUNICATION::matcher,
                m -> m.group("nextCommunicationAttempt"),
                licenseAuthorizationBuilder::setNextCommunicationAttempt);

        ParsingUtils.parseField(output, 0,
                COMMUNICATION_DEADLINE::matcher,
                m -> m.group("deadline"),
                licenseAuthorizationBuilder::setCommunicationDeadline);

        MiscellaneousBuilder miscellaneousBuilder = new MiscellaneousBuilder();
        ParsingUtils.parseField(output, 0,
                CUSTOM_ID::matcher,
                m -> m.group("customId"),
                miscellaneousBuilder::setCustomId);

        builder.setLicenseAuthorization(licenseAuthorizationBuilder.build());
        builder.setMiscellaneous(miscellaneousBuilder.build());
        builder.setRegistration(registrationBuilder.build());
        builder.setTransport(transportBuilder.build());
        builder.setDataPrivacy(dataPrivacyBuilder.build());

        stateBuilder.addAugmentation(CiscoSmartLicensingExtensionAug.class, builder.build());
    }

    static void parseUdi(String output, StateBuilder stateBuilder) {
        UdiBuilder udiBuilder = new UdiBuilder();

        ParsingUtils.parseField(output, 0,
                UDI_LINE::matcher,
                m -> m.group("pid"),
                udiBuilder::setPid);

        ParsingUtils.parseField(output, 0,
                UDI_LINE::matcher,
                m -> m.group("sn"),
                udiBuilder::setSn);

        ActiveUdiBuilder activeBuilder = new ActiveUdiBuilder();
        ParsingUtils.parseField(output, 0,
                UDI_ACTIVE::matcher,
                m -> m.group("pid"),
                activeBuilder::setPid);

        ParsingUtils.parseField(output, 0,
                UDI_ACTIVE::matcher,
                m -> m.group("sn"),
                activeBuilder::setSn);

        StandbyUdiBuilder standbyBuilder = new StandbyUdiBuilder();
        ParsingUtils.parseField(output, 0,
                UDI_STANDBY::matcher,
                m -> m.group("pid"),
                standbyBuilder::setPid);

        ParsingUtils.parseField(output, 0,
                UDI_STANDBY::matcher,
                m -> m.group("sn"),
                standbyBuilder::setSn);

        CiscoProductExtensionAugBuilder builder = new CiscoProductExtensionAugBuilder();
        builder.setUdi(udiBuilder.build());
        builder.setActiveUdi(activeBuilder.build());
        builder.setStandbyUdi(standbyBuilder.build());
        stateBuilder.addAugmentation(CiscoProductExtensionAug.class, builder.build());
    }

    static void parseReservation(String output, StateBuilder stateBuilder) {
        CiscoReservationExtensionAugBuilder builder = new CiscoReservationExtensionAugBuilder();
        ParsingUtils.parseField(output, 0,
                RESERVATION_LINE::matcher,
                m -> m.group("status"),
                builder::setReservationStatus);

        stateBuilder.addAugmentation(CiscoReservationExtensionAug.class, builder.build());
    }
}