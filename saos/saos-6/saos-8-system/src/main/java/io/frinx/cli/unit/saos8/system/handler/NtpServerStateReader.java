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

package io.frinx.cli.unit.saos8.system.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaServerStateAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaServerStateAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.ciena.system.server.state.extension.ServerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.Server;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpServerStateReader implements CliOperReader<State, StateBuilder> {

    private final Cli cli;

    private static final String SH_NTP_CONFIG = "ntp client show server %s";

    private static final Pattern PARSE_INDEX = Pattern.compile("\\| Index *\\| (?<index>\\S+) *\\|");
    private static final Pattern PARSE_HOST_NAME = Pattern.compile("\\| Host Name *\\| (?<hostName>\\S+) *\\|");
    private static final Pattern PARSE_IP_ADDRESS = Pattern.compile("\\| IP Address *\\| (?<ipAddress>\\S+) *\\|");
    private static final Pattern PARSE_AUTH_KEY_ID = Pattern.compile("\\| Auth Key ID *\\| (?<authKeyID>\\S+) *\\|");
    private static final Pattern PARSE_CONFIG_STATE = Pattern.compile("\\| Config State\\| (?<configState>\\S+) *\\|");
    private static final Pattern PARSE_ADMIN_STATE = Pattern.compile("\\| Admin State *\\| (?<adminState>\\S+) *\\|");
    private static final Pattern PARSE_OPER_STATE = Pattern.compile("\\| Oper State *\\| (?<operState>\\S+) *\\|");
    private static final Pattern PARSE_SERVER_STATE = Pattern.compile("\\| Server State\\| (?<serverState>\\S+) *\\|");
    private static final Pattern PARSE_SERVER_CONDITION =
            Pattern.compile("\\|  Condition *\\| (?<serverCondition>\\S+) *\\|");
    private static final Pattern PARSE_SERVER_AUTH_STATE =
            Pattern.compile("\\|  Auth State *\\| (?<serverAuthState>\\S+) *\\|");
    private static final Pattern PARSE_SERVER_OFFSET =
            Pattern.compile("\\|  Offset \\(ms\\)\\| (?<serverOffset>\\S+) *\\|");
    private static final Pattern PARSE_STRATUM = Pattern.compile("\\| Stratum *\\| (?<stratum>\\S+) *\\|");

    public NtpServerStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var ipAddress = id.firstKeyOf(Server.class).getAddress().getIpAddress().getIpv4Address().getValue();
        var builder = new CienaServerStateAugBuilder();
        parseServerState(stateBuilder, builder,
                blockingRead(f(SH_NTP_CONFIG, ipAddress), cli, id, readContext), ipAddress);
        stateBuilder.addAugmentation(CienaServerStateAug.class, builder.build());
    }

    public static void parseServerState(@NotNull StateBuilder stateBuilder,
                                         CienaServerStateAugBuilder builder,
                                         String output, String ipAddress) {
        stateBuilder.setAddress(new Host(new IpAddress(new Ipv4Address(ipAddress))));

        ParsingUtils.parseFields(output, 0,
            PARSE_IP_ADDRESS::matcher,
            matcher -> matcher.group("ipAddress"),
            v -> builder.setIpAddress(new Host(new IpAddress(new Ipv4Address(v)))));

        ParsingUtils.parseFields(output, 0,
            PARSE_INDEX::matcher,
            matcher -> matcher.group("index"),
            v -> builder.setIndex(Short.valueOf(v)));

        ParsingUtils.parseFields(output, 0,
            PARSE_HOST_NAME::matcher,
            matcher -> matcher.group("hostName"),
            v -> builder.setHostName(new Host(new IpAddress(new Ipv4Address(v)))));

        ParsingUtils.parseFields(output, 0,
            PARSE_AUTH_KEY_ID::matcher,
            matcher -> matcher.group("authKeyID"),
            builder::setAuthKeyId);

        ParsingUtils.parseFields(output, 0,
            PARSE_CONFIG_STATE::matcher,
            matcher -> matcher.group("configState"),
            builder::setConfigState);

        ParsingUtils.parseFields(output, 0,
            PARSE_ADMIN_STATE::matcher,
            matcher -> matcher.group("adminState"),
            builder::setAdminState);

        ParsingUtils.parseFields(output, 0,
            PARSE_OPER_STATE::matcher,
            matcher -> matcher.group("operState"),
            builder::setOperState);

        var serverBuilder = new ServerBuilder();

        ParsingUtils.parseFields(output, 0,
            PARSE_SERVER_STATE::matcher,
            matcher -> matcher.group("serverState"),
            serverBuilder::setServerState);

        ParsingUtils.parseFields(output, 0,
            PARSE_SERVER_CONDITION::matcher,
            matcher -> matcher.group("serverCondition"),
            serverBuilder::setCondition);

        ParsingUtils.parseFields(output, 0,
            PARSE_SERVER_AUTH_STATE::matcher,
            matcher -> matcher.group("serverAuthState"),
            serverBuilder::setAuthState);

        ParsingUtils.parseFields(output, 0,
            PARSE_SERVER_OFFSET::matcher,
            matcher -> matcher.group("serverOffset"),
            v -> serverBuilder.setOffset(new BigDecimal(v)));

        ParsingUtils.parseFields(output, 0,
            PARSE_STRATUM::matcher,
            matcher -> matcher.group("stratum"),
            v -> builder.setStratum(Short.valueOf(v)));

        builder.setServer(serverBuilder.build());
    }
}