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

package io.frinx.cli.unit.huawei.system.handler.connection;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.rev210923.system.telnet.server.top.telnet.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.rev210923.system.telnet.server.top.telnet.server.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TelnetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String TELNET_CONFIG = "display current-configuration configuration | include ^ telnet";
    private static final Pattern TELNET_ENABLE = Pattern.compile(".*telnet server enable.*");

    private final Cli cli;

    public TelnetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        parseTelnetIds(blockingRead(TELNET_CONFIG, cli, id, ctx), configBuilder);
    }

    static void parseTelnetIds(String output, ConfigBuilder configBuilder) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(TELNET_ENABLE::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .ifPresent(s -> configBuilder.setEnable(true));
    }
}