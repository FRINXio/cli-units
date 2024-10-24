/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.cablemac;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.CableDsItem;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.CableDsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.CableUsItem;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.CableUsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.OfdmDsItem;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.OfdmDsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.OfdmUsItem;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.cable.mac.state.top.cable.mac.state.state.OfdmUsItemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceCableMacStateReader implements CliOperReader<State, StateBuilder> {

    static final String SH_INT_CABLE_MAC = "show interface cable-mac %s";

    private static final Pattern OFDM_DS = Pattern.compile("(?<scch>\\S+/\\S+/\\S+)\\s+(?<cableMac>\\d+)\\s+"
            + "(?<chanId>\\d+)\\s+(?<primCap>(True|False))\\s+(?<operState>\\S+)\\s+(?<freqLowHigh>\\S+)\\s+"
            + "(?<PLCband>\\d+)\\s+(?<licBW>\\d+)\\s+(?<numOfProf>\\d+)\\s+(?<subcarrierSpacing>\\d+)\\s+"
            + "(?<rolloffPeriod>\\d+)\\s+(?<cyclicPrefix>\\d+)\\s+(?<interlvDepth>\\d+).*");

    private static final Pattern OFDM_US = Pattern.compile("(?<scgch>\\S+/\\S+/\\S+)\\s+(?<cableMac>\\d+)\\s+"
            + "(?<conn>\\S+)\\s+(?<operState>\\S+)\\s+(?<freqLowHigh>\\S+)\\s+(?<licBW>\\d+)\\s+"
            + "(?<minislotsPerFrame>\\d+)\\s+(?<modProf>\\d+)\\s+(?<subcarrierSpacing>\\d+)\\s+(?<rolloffPeriod>\\d+)"
            + "\\s+(?<cyclicPrefix>\\d+)\\s+(?<symFrame>\\d+)\\s+(?<power>\\S+).*");

    private static final Pattern CABLE_DS = Pattern.compile("(?<scch>\\S+/\\S+/\\S+)\\s+(?<cableMac>\\d+)\\s+"
            + "(?<chanId>\\d+)\\s+(?<primCap>(True|False))\\s+(?<operState>\\S+)\\s+(?<annex>\\S+)\\s+(?<freq>\\d+)\\s+"
            + "(?<interlvDepth>\\d+)\\s+(?<modType>\\S+)\\s+(?<power>\\S+)\\s+(?<lbalGroup>((\\d|,\\s)+|-)).*");

    private static final Pattern CABLE_US = Pattern.compile("(?<scgch>\\S+/\\S+/\\S+)\\s+(?<cableMac>\\d+)\\s+"
            + "(?<conn>\\S+)\\s+(?<operState>\\S+)\\s+(?<chanType>\\S+)\\s+(?<freqLowHigh>\\S+)\\s+(?<centerFreq>\\S+)"
            + "\\s+(?<channelWidth>\\S+)\\s+(?<miniSlot>\\d+)\\s+(?<modProf>\\d+)\\s+(?<power>\\S+)\\s+"
            + "(?<lbalGroup>((\\d|,\\s)+|-)).*");

    private static final Pattern OFDM_BOOL = Pattern.compile(".*\\|OFDM: (?<ofdmBool>(true|false))\\|.*");

    private static final Pattern OFDM_DS_TABLE = Pattern.compile("\\S*Cable.*Depth\\(time\\)(?<table>.*)");
    private static final Pattern OFDM_US_TABLE = Pattern.compile("\\S*Cable.*MHz\\)(?<table>.*)");
    private static final Pattern CABLE_DS_TABLE = Pattern.compile("\\S*Cable.*Group(?<table>.*)");
    private static final Pattern CABLE_US_TABLE = Pattern.compile("\\S*Cable.*Group(?<table>.*)");
    private static final Pattern OFDM_TABLE = Pattern.compile(".*\\|OFDM:\\s+(true|false)(?<ofdm>.*)\\|CABLE:.*");
    private static final Pattern CABLE_TABLE = Pattern.compile(".*\\|CABLE:(?<cable>.*)");
    private static final Pattern DS_TABLE = Pattern.compile(".*\\|DS(?<ds>.*)\\|US.*");
    private static final Pattern US_TABLE = Pattern.compile(".*\\|US(?<us>.*)");
    private static final Pattern DS_TABLE_NO_US = Pattern.compile(".*\\|DS(?<ds>.*)");

    private final Cli cli;

    public InterfaceCableMacStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final var cableMacName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (cableMacName.contains("cable-mac")) {
            var cableMacNameShorten = cableMacName.replaceAll("cable-mac ", "");
            var output = blockingRead(f(SH_INT_CABLE_MAC, cableMacNameShorten), cli, instanceIdentifier, readContext);
            parseCableMac(output, stateBuilder);
        }
    }

    @VisibleForTesting
    static void parseCableMac(String output, StateBuilder stateBuilder) {
        var parseOutput = replaceNewLinesWithSeparator(output);
        parseCableMacOfdmDsTable(replaceSeparatorsWithNewLine(getTable(getTable(getTable(parseOutput, OFDM_TABLE,
                        "ofdm", null), DS_TABLE, "ds", DS_TABLE_NO_US).trim(),
                OFDM_DS_TABLE, "table", null)), stateBuilder);
        parseCableMacOfdmUsTable(replaceSeparatorsWithNewLine(getTable(getTable(getTable(parseOutput, OFDM_TABLE,
                        "ofdm", null), US_TABLE, "us", null).trim(),
                OFDM_US_TABLE, "table", null)), stateBuilder);
        parseCableMacCableDsTable(replaceSeparatorsWithNewLine(getTable(getTable(getTable(parseOutput, CABLE_TABLE,
                        "cable", null), DS_TABLE, "ds", DS_TABLE_NO_US).trim(),
                CABLE_DS_TABLE, "table", null)), stateBuilder);
        parseCableMacCableUsTable(replaceSeparatorsWithNewLine(getTable(getTable(getTable(parseOutput, CABLE_TABLE,
                "cable", null), US_TABLE, "us", null).trim(),
                CABLE_US_TABLE, "table", null)), stateBuilder);

        ParsingUtils.parseField(parseOutput, 0,
            OFDM_BOOL::matcher,
            matcher -> matcher.group("ofdmBool"),
            val -> stateBuilder.setOfdm(val.trim().equals("true")));
    }

    private static void parseCableMacOfdmDsTable(String output, StateBuilder stateBuilder) {
        if (!output.equals("")) {
            stateBuilder.setOfdmDsItem(ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(l -> OFDM_DS.matcher(l.trim()).matches())
                    .map(line -> parseOfdmDsItems(line))
                    .collect(Collectors.toList()));
        }
    }

    private static void parseCableMacOfdmUsTable(String output, StateBuilder stateBuilder) {
        if (!output.equals("")) {
            stateBuilder.setOfdmUsItem(ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(l -> OFDM_US.matcher(l.trim()).matches())
                    .map(line -> parseOfdmUsItems(line))
                    .collect(Collectors.toList()));
        }
    }

    private static void parseCableMacCableDsTable(String output, StateBuilder stateBuilder) {
        if (!output.equals("")) {
            stateBuilder.setCableDsItem(ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(l -> CABLE_DS.matcher(l.trim()).matches())
                    .map(line -> parseCableDsItems(line))
                    .collect(Collectors.toList()));
        }
    }

    private static void parseCableMacCableUsTable(String output, StateBuilder stateBuilder) {
        if (!output.equals("")) {
            stateBuilder.setCableUsItem(ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(l -> CABLE_US.matcher(l.trim()).matches())
                    .map(line -> parseCableUsItems(line))
                    .collect(Collectors.toList()));
        }
    }

    private static OfdmDsItem parseOfdmDsItems(String line) {
        var builder = new OfdmDsItemBuilder();
        var matcher = OFDM_DS.matcher(line.trim());

        if (matcher.matches()) {
            builder.setId(matcher.group("scch"))
                    .setCableMac(matcher.group("cableMac"))
                    .setChanId(matcher.group("chanId"))
                    .setPrimCap(matcher.group("primCap"))
                    .setOperState(matcher.group("operState"))
                    .setFreqLowHigh(matcher.group("freqLowHigh"))
                    .setPlcBand(matcher.group("PLCband"))
                    .setLicBw(matcher.group("licBW"))
                    .setNumOfProf(matcher.group("numOfProf"))
                    .setSubcarrierSpacing(matcher.group("subcarrierSpacing"))
                    .setRolloffPeriod(matcher.group("rolloffPeriod"))
                    .setCyclicPrefix(matcher.group("cyclicPrefix"))
                    .setIntrlvDepth(matcher.group("interlvDepth"));
        }

        return builder.build();
    }

    private static OfdmUsItem parseOfdmUsItems(String line) {
        var builder = new OfdmUsItemBuilder();
        var matcher = OFDM_US.matcher(line.trim());

        if (matcher.matches()) {
            builder.setId(matcher.group("scgch"))
                    .setCableMac(matcher.group("cableMac"))
                    .setConn(matcher.group("conn"))
                    .setOperState(matcher.group("operState"))
                    .setFreqLowHigh(matcher.group("freqLowHigh"))
                    .setLicBw(matcher.group("licBW"))
                    .setMinislotsPerFrame(matcher.group("minislotsPerFrame"))
                    .setModProf(matcher.group("modProf"))
                    .setSubcarrierSpacing(matcher.group("subcarrierSpacing"))
                    .setRolloffPeriod(matcher.group("rolloffPeriod"))
                    .setCyclicPrefix(matcher.group("cyclicPrefix"))
                    .setSymFrame(matcher.group("symFrame"))
                    .setPower(matcher.group("power"));
        }

        return builder.build();
    }

    private static CableDsItem parseCableDsItems(String line) {
        var builder = new CableDsItemBuilder();
        var matcher = CABLE_DS.matcher(line.trim());

        if (matcher.matches()) {
            builder.setId(matcher.group("scch"))
                    .setCableMac(matcher.group("cableMac"))
                    .setChanId(matcher.group("chanId"))
                    .setPrimCap(matcher.group("primCap"))
                    .setOperState(matcher.group("operState"))
                    .setAnnex(matcher.group("annex"))
                    .setFrequency(matcher.group("freq"))
                    .setIntrlvDepth(matcher.group("interlvDepth"))
                    .setModType(matcher.group("modType"))
                    .setPower(matcher.group("power"))
                    .setLbalGroup(matcher.group("lbalGroup"));
        }

        return builder.build();
    }

    private static CableUsItem parseCableUsItems(String line) {
        var builder = new CableUsItemBuilder();
        var matcher = CABLE_US.matcher(line.trim());

        if (matcher.matches()) {
            builder.setId(matcher.group("scgch"))
                    .setCableMac(matcher.group("cableMac"))
                    .setConn(matcher.group("conn"))
                    .setOperState(matcher.group("operState"))
                    .setChanType(matcher.group("chanType"))
                    .setFreqLowHigh(matcher.group("freqLowHigh"))
                    .setCenterFreq(matcher.group("centerFreq"))
                    .setChannelWidth(matcher.group("channelWidth"))
                    .setMiniSlot(matcher.group("miniSlot"))
                    .setModProf(matcher.group("modProf"))
                    .setPower(matcher.group("power"))
                    .setLbalGroup(matcher.group("lbalGroup"));
        }

        return builder.build();
    }

    private static String replaceNewLinesWithSeparator(String output) {
        return output.replaceAll("\\n", "|")
                .replaceAll("\\r", "")
                .replaceAll("\\|+", "|");
    }

    private static String replaceSeparatorsWithNewLine(String output) {
        return output.replaceAll("\\|", "\n").trim();
    }

    private static String getTable(String output, Pattern pattern, String group, Pattern noUsPattern) {
        if (noUsPattern == null) {
            var matcher = pattern.matcher(output);
            if (matcher.matches()) {
                return matcher.group(group);
            }
        } else {
            var matcher = pattern.matcher(output);
            if (matcher.matches()) {
                return matcher.group(group);
            }
            if (getTable(output, US_TABLE, "us", null).equals("")) {
                matcher = noUsPattern.matcher(output);
                if (matcher.matches()) {
                    return matcher.group(group);
                }
            }
        }
        return "";
    }
}