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

package io.frinx.cli.unit.ios.lldp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.LldpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.lldp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.lldp.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LldpConfigReader implements CliOperReader<Config, ConfigBuilder> {

    // The "sh ru" here is intentional to bypass the caching of entire "show run" output
    // We don't want that here
    // TODO expose control whether to cache or not from CliReader (add an option to Command interface ? )

    public static final String SHOW_HOSTNAME = "sh ru | include ^hostname|^ip domain name";

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("hostname (?<name>.+)");
    private static final Pattern DOMAINNAME_PATTERN = Pattern.compile("(ip )?domain name (?<name>.+)");

    private final Cli cli;
    private final String command;

    public LldpConfigReader(Cli cli, String command) {
        this.cli = cli;
        this.command = command;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
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

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull Config config) {
        ((LldpBuilder) builder).setConfig(config);
    }
}
