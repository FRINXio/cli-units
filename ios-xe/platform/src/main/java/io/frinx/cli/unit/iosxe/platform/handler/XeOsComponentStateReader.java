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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformSlotAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformSlotAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoTransceiverAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoTransceiverAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.TRANSCEIVER;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class XeOsComponentStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_MODULE_INVENTORY = "show inventory";
    private static final String SH_MODULE_VERSION = "show version";
    static final Pattern VERSION = Pattern.compile("Cisco IOS XE Software, Version (?<version>\\S+).*");
    static final Pattern VERSION_IOS = Pattern
            .compile("Cisco IOS Software.*, .* Software .*, Version (?<ver>[[^\\s^,]]+),.*");
    static final String IDS = "NAME: \"%s\".*DESCR: \"(?<description>[^\"]+)\".*PID: (?<pid>[^,]+),.*"
            + "VID: (?<vid>[^,]+),.*SN:( )?(?<sn>[^,]*).*";
    private static final String SH_SUBSLOT_TRANSCEIVER = "show hw-module subslot %s transceiver %s idprom";
    private static final Pattern DESCRIPTION = Pattern.compile("Description *= (?<description>.+)");
    private static final Pattern TRANSCEIVER_TYPE = Pattern.compile("Transceiver Type: *= (?<transceiverType>.+)");
    private static final Pattern PID = Pattern.compile("Product Identifier \\(PID\\) *= (?<pid>.+)");
    private static final Pattern REVISION = Pattern.compile("Vendor Revision *= (?<rev>.+)");
    private static final Pattern SERIAL_NUMBER = Pattern.compile("Serial Number \\(SN\\) *= (?<sn>.+)");
    private static final Pattern NAME = Pattern.compile("Vendor Name *= (?<name>.+)");
    private static final Pattern OUI = Pattern.compile("Vendor OUI \\(IEEE company ID\\) *= (?<oui>.+)");
    private static final Pattern CLEI_CODE = Pattern.compile("CLEI code *= (?<cleiCode>.+)");
    private static final Pattern CISCO_PART_NUMBER = Pattern.compile("Cisco part number *= (?<partNumber>.+)");
    private static final Pattern DEVICE_STATE = Pattern.compile("Device State *= (?<deviceState>.+)");
    private static final Pattern DATE_CODE = Pattern.compile("Date code \\(yy/mm/dd\\) *= (?<dateCode>.+)");
    private static final Pattern CONNECTOR_TYPE = Pattern.compile("Connector type *= (?<connectorType>.+)");
    private static final Pattern ENCODING = Pattern.compile("Encoding *= (?<encoding>.+)");
    private static final Pattern NOMINAL_BITRATE = Pattern.compile("Nominal bitrate *= *(?<nominalBitrate>.+)");
    private static final Pattern MINIMUM_BITRATE = Pattern.compile(
            "Minimum bit rate as % of nominal bit rate *= *(?<minimumBitrate>.+)");
    private static final Pattern MAXIMUM_BITRATE = Pattern.compile(
            "Maximum bit rate as % of nominal bit rate *= *(?<maximumBitrate>.+)");
    private static final Pattern SLOT_LINE2 = Pattern.compile(
            "(?<slot>\\S+) *(?<cpldVersion>\\S+) *(?<rommonVersion>\\S+) *");

    private final Cli cli;

    public XeOsComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Component.class)
                .getName();
        if (name.equals(OsComponent.OS_KEY.getName())) {
            parseOSVersions(stateBuilder, blockingRead(f(SH_MODULE_VERSION), cli, instanceIdentifier, readContext));
        } else if (name.contains("transceiver")) {
            List<String> values = findSubslotAndTransceiverByName(
                    Pattern.compile("subslot (?<subslot>.+) transceiver (?<transceiver>.+)"), name);
            parseTransceiver(stateBuilder, name, blockingRead(f(SH_SUBSLOT_TRANSCEIVER, values.get(0), values.get(1)),
                    cli, instanceIdentifier, readContext));
        } else if (name.contains("SFP")) {
            List<String> values =  findSubslotAndTransceiverByName(
                    Pattern.compile("SFP\\+ module (?<subslot>.+)/(?<transceiver>.+)"), name);
            parseTransceiver(stateBuilder, name, blockingRead(f(SH_SUBSLOT_TRANSCEIVER, values.get(0), values.get(1)),
                    cli, instanceIdentifier, readContext));
        } else {
            parseFields(stateBuilder, name, blockingRead(f(SH_MODULE_INVENTORY, name), cli, instanceIdentifier,
                    readContext));
            parseSlot(stateBuilder, name, blockingRead(f(XeOsComponentReader.SH_PLATFORM),
                    cli, instanceIdentifier, readContext));
        }
    }

    static void parseFields(@NotNull StateBuilder stateBuilder, String name, String output) {
        stateBuilder.setName(name);
        stateBuilder.setId(name);

        var ids = Pattern.compile(String.format(IDS, processName(name)));

        ParsingUtils.parseField(output, 0,
            VERSION::matcher,
            m -> m.group("version"),
            stateBuilder::setVersion);

        output = processOutput(output);

        var builder = new CiscoPlatformAugBuilder();

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("description"),
            stateBuilder::setDescription);

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("pid"),
            v -> builder.setPid(v.trim()));

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("vid"),
            v -> builder.setVid(v.trim()));

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("sn"),
            v -> builder.setSn(v.trim()));

        stateBuilder.addAugmentation(CiscoPlatformAug.class, builder.build());

        // TODO We are reading just line cards now, so it should be fine
        // to always set LINECARD type for now. But in the future we should
        // take into account also other types
        stateBuilder.setType(new PlatformComponentState.Type(LINECARD.class));
    }

    static void parseOSVersions(@NotNull StateBuilder stateBuilder, String output) {
        stateBuilder.setName(OsComponent.OS_NAME);
        ParsingUtils.parseField(output, 0,
            VERSION::matcher,
            m -> m.group("version"),
            stateBuilder::setVersion);

        ParsingUtils.parseField(output, 0,
            VERSION_IOS::matcher,
            m -> m.group("ver"),
            stateBuilder::setSoftwareVersion);

        ParsingUtils.parseField(output, 0,
            XeOsComponentReader.LINE::matcher,
            m -> m.group("name"),
            stateBuilder::setName);
    }

    static void parseTransceiver(@NotNull StateBuilder stateBuilder, String name, String output) {
        stateBuilder.setName(name);
        stateBuilder.setId(name);

        CiscoTransceiverAugBuilder builder = new CiscoTransceiverAugBuilder();
        ParsingUtils.parseField(output, 0,
                NAME::matcher,
                m -> m.group("name"),
                builder::setVendorName);

        ParsingUtils.parseField(output, 0,
                REVISION::matcher,
                m -> m.group("rev"),
                builder::setVendorRevision);

        ParsingUtils.parseField(output, 0,
                SERIAL_NUMBER::matcher,
                m -> m.group("sn"),
                builder::setSerialNumber);

        ParsingUtils.parseField(output, 0,
                DATE_CODE::matcher,
                m -> m.group("dateCode"),
                builder::setDateCode);

        ParsingUtils.parseField(output, 0,
                CONNECTOR_TYPE::matcher,
                m -> m.group("connectorType"),
                builder::setConnectorType);

        ParsingUtils.parseField(output, 0,
                DESCRIPTION::matcher,
                m -> m.group("description"),
                builder::setDescription);

        ParsingUtils.parseField(output, 0,
                TRANSCEIVER_TYPE::matcher,
                m -> m.group("transceiverType"),
                builder::setTransceiverType);

        ParsingUtils.parseField(output, 0,
                PID::matcher,
                m -> m.group("pid"),
                builder::setProductIdentifier);

        ParsingUtils.parseField(output, 0,
                OUI::matcher,
                m -> m.group("oui"),
                builder::setVendorOui);

        ParsingUtils.parseField(output, 0,
                CLEI_CODE::matcher,
                m -> m.group("cleiCode"),
                builder::setCleiCode);

        ParsingUtils.parseField(output, 0,
                CISCO_PART_NUMBER::matcher,
                m -> m.group("partNumber"),
                builder::setCiscoPartNumber);

        ParsingUtils.parseField(output, 0,
                DEVICE_STATE::matcher,
                m -> m.group("deviceState"),
                builder::setDeviceState);

        ParsingUtils.parseField(output, 0,
                ENCODING::matcher,
                m -> m.group("encoding"),
                builder::setEncoding);

        ParsingUtils.parseField(output, 0,
                NOMINAL_BITRATE::matcher,
                m -> m.group("nominalBitrate"),
                builder::setNominalBitrate);

        ParsingUtils.parseField(output, 0,
                MINIMUM_BITRATE::matcher,
                m -> m.group("minimumBitrate"),
                builder::setMinimumBitrate);

        ParsingUtils.parseField(output, 0,
                MAXIMUM_BITRATE::matcher,
                m -> m.group("maximumBitrate"),
                builder::setMaximumBitrate);

        stateBuilder.setType(new PlatformComponentState.Type(TRANSCEIVER.class));
        stateBuilder.addAugmentation(CiscoTransceiverAug.class, builder.build());
    }

    private static List<String> findSubslotAndTransceiverByName(Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(name);
        matcher.matches();
        String subslot = matcher.group("subslot");
        String transceiver = matcher.group("transceiver");
        return List.of(subslot, transceiver);
    }

    private static void parseSlot(@NotNull StateBuilder stateBuilder, String name, String output) {
        stateBuilder.setName(name);
        stateBuilder.setId(name);

        CiscoPlatformSlotAugBuilder builder = new CiscoPlatformSlotAugBuilder();
        ParsingUtils.parseField(output, 0,
                XeOsComponentReader.SLOT_LINE::matcher,
                m -> m.group("type"),
                builder::setType);

        ParsingUtils.parseField(output, 0,
                XeOsComponentReader.SLOT_LINE::matcher,
                m -> m.group("state"),
                builder::setState);

        ParsingUtils.parseField(output, 0,
                XeOsComponentReader.SLOT_LINE::matcher,
                m -> m.group("insertTime"),
                builder::setInsertTime);

        ParsingUtils.parseField(output, 0,
                SLOT_LINE2::matcher,
                m -> m.group("cpldVersion"),
                builder::setCpldVersion);

        ParsingUtils.parseField(output, 0,
                SLOT_LINE2::matcher,
                m -> m.group("rommonVersion"),
                builder::setRommonVersion);

        stateBuilder.addAugmentation(CiscoPlatformSlotAug.class, builder.build());
    }

    private static String processOutput(String output) {
        return output.replaceAll("PID", ", PID")
                .replaceAll("\\\\n", " ")
                .replaceAll("\\n", " ")
                .replaceAll("\\r", "")
                .replaceAll("NAME:", "\nNAME:")
                .replaceFirst("(?m)^\\n", "");
    }

    private static String processName(String name) {
        return name.replaceAll("\\+", "\\\\+");
    }
}