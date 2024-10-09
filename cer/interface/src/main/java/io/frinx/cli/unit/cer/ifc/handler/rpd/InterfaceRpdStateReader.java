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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.RpdState.AdminState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.RpdState.OperState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.ptp.state.top.PtpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.RpdChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.RpdChannelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.RpdChannelStatus;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.RpdChannelStatusBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.top.rpd.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.state.top.rpd.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SHOW_RPD_DATA = "show interface rpd %s";
    private static final String SHOW_PTP_DATA = "show interface rpd %s ptp detail";
    private static final Pattern DSCP = Pattern.compile("^RPD PTP DSCP:\\s+(?<dscp>\\d+)");
    private static final Pattern CLOCK_STATE = Pattern.compile("^Core Clock State:\\s+(?<clock>.+)");
    private static final Pattern DOMAIN = Pattern.compile(".*Domain:\\s+(?<domain>.+)");
    private static final Pattern PROFILE_NAME = Pattern.compile(".*Profile Name:\\s+(?<pname>.+)");
    private static final Pattern PROFILE_VERSION = Pattern.compile(".*Profile Version:\\s+(?<pversion>.+)");
    private static final Pattern STATE = Pattern.compile("State:\\s+(?<state>.+)");
    private static final Pattern LS_CHANGE = Pattern.compile(".*Last State Change:\\s+(?<lschange>.*)");
    private static final Pattern P_OFF = Pattern.compile(".*Last Computed Phase Offset:\\s+(?<poffset>\\S+)");
    private static final Pattern F_OFF = Pattern.compile(".*Estimated Frequency Offset:\\s+(?<foffset>.+)");
    private static final Pattern RPD_STATE = Pattern.compile(
            "(?<rpd>.+)\\s+(?<macadd>\\w+\\.\\w+\\.\\w+)"
                    + "\\s+(?<idx>\\w+)\\s+(?<cmac>[\\w-]+)\\s+(?<ucam>\\d+)"
                    + "\\s+(?<dcam>\\d+)\\s+(?<type>\\w+)\\s+(?<astate>\\w+)"
                    + "\\s+(?<ostate>\\w+)\\s+(?<ip>.+)");

    static final String SH_CHANNEL = "show interface rpd %s channel";
    static final String SH_CHANNEL_STATUS = "show interface rpd %s channel status";

    static final Pattern PARSE_CHANNEL = Pattern
            .compile("(?<name>\\S+)\\s+(?<dir>(DS|US))\\s+(?<int>(\\S+\\s\\S+|\\S+))");

    static final Pattern PARSE_CHANNEL_STATUS = Pattern
            .compile("\\s*(?<name>\\S+) (?<dir>(DS|US))"
                    + "\\s+(?<int>\\S+)\\s+(?<state>\\S+)\\s+(?<ch>\\S+)\\s+(?<depi>\\S+)\\s+(?<core>\\S+)");

    private final Cli cli;

    public InterfaceRpdStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final var rpdName = id.firstKeyOf(Interface.class).getName();
        if (rpdName.contains("rpd")) {
            final var name = rpdName.substring(5, rpdName.length() - 1);
            final var channelOutput = blockingRead(f(SH_CHANNEL, name), cli, id, ctx);
            final var channelStateOutput = blockingRead(f(SH_CHANNEL_STATUS, name), cli, id, ctx);
            parseRpdStateData(blockingRead(f(SHOW_RPD_DATA, name), cli, id, ctx), stateBuilder);
            parsePtpStateDate(blockingRead(f(SHOW_PTP_DATA, name), cli, id, ctx), stateBuilder);
            parseRpdChannel(channelOutput, stateBuilder);
            parseRpdChannelStatus(channelStateOutput, stateBuilder);
        }
    }

    @VisibleForTesting
    static void parseRpdStateData(String output, StateBuilder stateBuilder) {
        ParsingUtils.parseField(output, 0,
                CLOCK_STATE::matcher,
                matcher -> matcher.group("clock"),
                stateBuilder::setClockState);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> matcher.group("macadd"),
                stateBuilder::setMacAddress);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> Integer.valueOf(matcher.group("idx")),
                stateBuilder::setRpdIndex);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> {
                    if (!matcher.group("cmac").equals("-")) {
                        return Integer.valueOf(matcher.group("cmac"));
                    }
                    return null;
                },
                stateBuilder::setCableMac);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> Integer.valueOf(matcher.group("ucam")),
                stateBuilder::setUcam);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> Integer.valueOf(matcher.group("dcam")),
                stateBuilder::setDcam);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> matcher.group("type"),
                stateBuilder::setDsUsType);

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> matcher.group("astate").toUpperCase(Locale.ROOT),
                a -> stateBuilder.setAdminState(AdminState.valueOf(a)));

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> matcher.group("ostate").toUpperCase(Locale.ROOT),
                o -> stateBuilder.setOperState(OperState.valueOf(o)));

        ParsingUtils.parseField(output, 0,
                RPD_STATE::matcher,
                matcher -> matcher.group("ip"),
                stateBuilder::setIpv6Address);
    }

    @VisibleForTesting
    static void parsePtpStateDate(String output, StateBuilder stateBuilder) {
        final var ptpBuilder = new PtpBuilder();

        ParsingUtils.parseField(output, 0,
                CLOCK_STATE::matcher,
                matcher -> matcher.group("clock"),
                ptpBuilder::setClockState);

        ParsingUtils.parseField(output, 0,
                DSCP::matcher,
                matcher -> Integer.valueOf(matcher.group("dscp")),
                ptpBuilder::setDscp);

        ParsingUtils.parseField(output, 0,
                DOMAIN::matcher,
                matcher -> Integer.valueOf(matcher.group("domain")),
                ptpBuilder::setDomain);

        ParsingUtils.parseField(output, 0,
                PROFILE_NAME::matcher,
                matcher -> matcher.group("pname"),
                ptpBuilder::setProfile);

        ParsingUtils.parseField(output, 0,
                PROFILE_VERSION::matcher,
                matcher -> matcher.group("pversion"),
                ptpBuilder::setVersion);

        ParsingUtils.parseField(output, 0,
                STATE::matcher,
                matcher -> matcher.group("state"),
                ptpBuilder::setState);

        ParsingUtils.parseField(output, 0,
                LS_CHANGE::matcher,
                matcher -> matcher.group("lschange"),
                ptpBuilder::setLastStateChange);

        ParsingUtils.parseField(output, 0,
                P_OFF::matcher,
                matcher -> matcher.group("poffset"),
                ptpBuilder::setPhaseOffset);

        ParsingUtils.parseField(output, 0,
                F_OFF::matcher,
                matcher -> matcher.group("foffset"),
                ptpBuilder::setFrequencyOffset);

        stateBuilder.setPtp(ptpBuilder.build());
    }

    @VisibleForTesting
    static void parseRpdChannel(String output, StateBuilder stateBuilder) {
        if (!output.equals("")) {
            stateBuilder.setRpdChannel(ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(l -> PARSE_CHANNEL.matcher(l.trim()).matches())
                    .map(line -> parseRpdChannelItems(line))
                    .collect(Collectors.toList()));
        }
    }

    @VisibleForTesting
    static void parseRpdChannelStatus(String output, StateBuilder stateBuilder) {
        if (!output.equals("")) {
            stateBuilder.setRpdChannelStatus(ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(l -> PARSE_CHANNEL_STATUS.matcher(l.trim()).matches())
                    .map(line -> parseRpdChannelStatusItems(line))
                    .collect(Collectors.toList()));
        }
    }

    private static RpdChannel parseRpdChannelItems(String line) {
        var builder = new RpdChannelBuilder();
        var matcher = PARSE_CHANNEL.matcher(line.trim());

        if (matcher.matches()) {
            var name = matcher.group("name");
            var dir = matcher.group("dir");
            builder.setId(name + " " + dir)
                    .setRpdName(name)
                    .setRpdDir(dir)
                    .setRpdInterfaces(matcher.group("int"));
        }

        return builder.build();
    }

    private static RpdChannelStatus parseRpdChannelStatusItems(String line) {
        var builder = new RpdChannelStatusBuilder();
        var matcher = PARSE_CHANNEL_STATUS.matcher(line.trim());

        if (matcher.matches()) {
            var name = matcher.group("name");
            var dir = matcher.group("dir");
            var intf = matcher.group("int");
            builder.setId(name + " " + dir + " " + intf)
                    .setRpdName(name)
                    .setRpdDir(dir)
                    .setRpdInterfaces(intf)
                    .setChannelState(matcher.group("state"))
                    .setChannelSelection(matcher.group("ch"))
                    .setDepiChannelId(matcher.group("depi"))
                    .setCorePwId(matcher.group("core"));
        }
        return builder.build();
    }
}