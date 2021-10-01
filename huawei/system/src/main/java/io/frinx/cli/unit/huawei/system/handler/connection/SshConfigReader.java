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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.rev210923.system.ssh.server.top.ssh.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.rev210923.system.ssh.server.top.ssh.server.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SshConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SSH_CONFIG = "display ssh server status";
    private static final Pattern SSH_TIMEOUT = Pattern.compile("SSH connection timeout\\s+:(?<value>\\d+).*");
    private static final Pattern SSH_RETRIES = Pattern.compile("SSH Authentication retries\\s+:(?<value>\\d+).*");

    private final Cli cli;

    public SshConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        parseConfigAttributes(blockingRead(SSH_CONFIG, cli, id, ctx), configBuilder);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder) {
        configBuilder.setEnable(true);
        parseLineValue(output, SSH_TIMEOUT, value -> configBuilder.setTimeout(Integer.valueOf(value)));
        parseLineValue(output, SSH_RETRIES, value -> configBuilder.setSessionLimit(Integer.valueOf(value)));
    }

    private static void parseLineValue(String output, Pattern pattern, Consumer<String> action) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("value"))
                .findFirst()
                .ifPresent(action);
    }
}