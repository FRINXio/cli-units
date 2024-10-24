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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.Server;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpServerConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern NTP_PREFERENCE = Pattern.compile("ntp-service unicast-server \\S+ .*preference");

    private final Cli cli;

    public NtpServerConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        Host hostAddress = instanceIdentifier.firstKeyOf(Server.class).getAddress();
        parseConfigAttributes(blockingRead(NtpConfigReader.DISPLAY_NTP, cli, instanceIdentifier,
                readContext), configBuilder, hostAddress);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, Host hostAddress) {
        configBuilder.setPrefer(false);
        parsingServerFields(output, hostAddress, NTP_PREFERENCE, value -> configBuilder.setPrefer(true));
    }

    private static void parsingServerFields(String output, Host hostAddress, Pattern pattern,
                                            Consumer<Matcher> consumer) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(line -> line.contains(hostAddress.getIpAddress().getIpv4Address().getValue()))
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .ifPresent(consumer);
    }
}