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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.top.ntp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.top.ntp.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String DISPLAY_NTP = "display current-configuration | include ntp";

    private static final Pattern ENABLED_LINE = Pattern.compile("undo ntp-service enable.*");

    private Cli cli;

    public NtpConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(DISPLAY_NTP, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        configBuilder.setEnabled(true);
        ParsingUtils.parseField(output, 0,
            ENABLED_LINE::matcher,
            matcher -> matcher.find(),
            value -> configBuilder.setEnabled(false));
    }
}