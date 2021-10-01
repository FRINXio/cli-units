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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.stelnet.top.stelnet.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.stelnet.top.stelnet.server.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class STelnetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String STELNET_CONFIG = "display current-configuration configuration | include ^ stelnet";
    private static final Pattern STELNET_ENABLE = Pattern.compile(".*stelnet server enable.*");

    private final Cli cli;

    public STelnetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        configBuilder.setEnable(false);
        parseSTelnetIds(blockingRead(STELNET_CONFIG, cli, id, ctx), configBuilder);
    }

    @VisibleForTesting
    static void parseSTelnetIds(String output, ConfigBuilder configBuilder) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(STELNET_ENABLE::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .ifPresent(s -> configBuilder.setEnable(true));
    }
}