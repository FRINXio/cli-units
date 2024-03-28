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
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.Server;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.ServerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.ServerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpServerReader implements CliOperListReader<Server, ServerKey, ServerBuilder> {

    private final Cli cli;

    private static final String SH_NTP = "ntp client show";
    private static final Pattern PARSE_NTP_SERVER = Pattern.compile(
            "\\|(?<ipAddress>\\S+) *.*");

    public NtpServerReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Server> id,
                                      @NotNull ServerBuilder serverBuilder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        serverBuilder.setKey(new ServerKey(id.firstKeyOf(Server.class).getAddress()));
    }

    @NotNull
    @Override
    public List<ServerKey> getAllIds(@NotNull InstanceIdentifier<Server> id,
                                     @NotNull ReadContext context) throws ReadFailedException {
        var serverKeys = parseAllServerKeys(blockingRead(SH_NTP, cli, id, context));
        return serverKeys;
    }

    public static List<ServerKey> parseAllServerKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_NTP_SERVER::matcher,
            matcher -> matcher.group("ipAddress"),
            v -> new ServerKey(new Host(new IpAddress(new Ipv4Address(v)))));
    }
}