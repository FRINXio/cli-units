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

package io.frinx.cli.unit.huawei.system.handler.global.config;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.timezone.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.timezone.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClockTimezoneConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_CLOCK = "display current-configuration | include clock";
    private static final Pattern TIMEZONE_LINE = Pattern.compile("clock timezone (?<name>\\S+) \\S+ (?<shift>\\S+).*");
    private static final Pattern TIMEZONE_SHIFT_MODIFIER = Pattern.compile("clock timezone \\S+ add \\S+.*");

    private final Cli cli;

    public ClockTimezoneConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(DISPLAY_CLOCK, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        configBuilder.setModifier(Config.Modifier.Minus);
        ParsingUtils.parseField(output, 0,
            TIMEZONE_LINE::matcher,
            matcher -> matcher.group("name"),
            configBuilder::setName);
        ParsingUtils.parseField(output, 0,
            TIMEZONE_SHIFT_MODIFIER::matcher,
            matcher -> matcher.find(),
            value -> configBuilder.setModifier(Config.Modifier.Add));
        ParsingUtils.parseField(output, 0,
            TIMEZONE_LINE::matcher,
            matcher -> matcher.group("shift"),
            configBuilder::setOffset);
    }
}
