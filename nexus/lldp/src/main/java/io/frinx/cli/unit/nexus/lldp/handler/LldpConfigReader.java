/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.nexus.lldp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.lldp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.lldp.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LldpConfigReader implements CliOperReader<Config, ConfigBuilder> {

    public static final String SHOW_HOSTNAME = "show running-config | include ^switchname|^ip";

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("switchname (?<name>.+)");
    private static final Pattern DOMAINNAME_PATTERN = Pattern.compile("ip domain-name (?<name>.+)");

    private final Cli cli;
    private final Command command;

    public LldpConfigReader(Cli cli, Command command) {
        this.cli = cli;
        this.command = command;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String hostnameOutput = blockingRead(command, cli, instanceIdentifier, readContext);
        parseConfig(configBuilder, hostnameOutput);
    }

    @VisibleForTesting
    static void parseConfig(ConfigBuilder configBuilder, String hostnameOutput) {
        Optional<String> host = ParsingUtils.parseField(hostnameOutput, 0,
                HOSTNAME_PATTERN::matcher,
            m -> m.group("name"));

        if (!host.isPresent()) {
            return;
        }

        Optional<String> domain = ParsingUtils.parseField(hostnameOutput, 0,
                DOMAINNAME_PATTERN::matcher,
            m -> m.group("name"));

        configBuilder.setSystemName(domain
            .map(s -> host.get() + "." + s)
                .orElseGet(host::get));
    }
}