/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.system.handler.ntp;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
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

public class NtpServerReader implements CliConfigListReader<Server, ServerKey, ServerBuilder> {

    private static final Pattern NTP_LINE = Pattern.compile("ntp-service unicast-server (?<address>\\S+).*");
    private final Cli cli;

    public NtpServerReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<ServerKey> getAllIds(@NotNull InstanceIdentifier<Server> instanceIdentifier,
                                     @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(NtpConfigReader.DISPLAY_NTP, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<ServerKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            NTP_LINE::matcher,
            matcher -> matcher.group("address"),
            address -> new ServerKey(new Host(new IpAddress(new Ipv4Address(address)))));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Server> instanceIdentifier,
                                      @NotNull ServerBuilder builder,
                                      @NotNull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(Server.class));
    }
}