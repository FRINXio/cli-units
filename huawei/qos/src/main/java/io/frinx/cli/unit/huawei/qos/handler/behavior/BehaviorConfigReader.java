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

package io.frinx.cli.unit.huawei.qos.handler.behavior;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.Behavior;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.behavior.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.behavior.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BehaviorConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_BE_CONFIG = "display traffic behavior user-defined %s";
    private static final Pattern BE_RULE_PERMIT = Pattern.compile("Permit");
    private static final Pattern BE_RULE_REMARK = Pattern.compile("Remark (?<remark>.+)");
    private static final Pattern BE_RULE_STATISTIC = Pattern.compile("Statistic: (?<statistic>.+)");
    private static final Pattern CIR_CBS = Pattern.compile("CIR (?<cir>.+), CBS (?<cbs>.+)");
    private static final Pattern PIR_PBS = Pattern.compile("PIR (?<pir>.+), PBS (?<pbs>.+)");
    private static final Pattern C_MODE = Pattern.compile("Color Mode: (?<colorMode>.+)");
    private static final Pattern G_ACTION = Pattern.compile("Green Action\\s+: (?<gAction>.+)");
    private static final Pattern Y_ACTION = Pattern.compile("Yellow Action\\s+: (?<yAction>.+)");
    private static final Pattern R_ACTION = Pattern.compile("Red Action\\s+: (?<rAction>.+)");

    private Cli cli;

    public BehaviorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final String name = id.firstKeyOf(Behavior.class).getName();
        final String showCommand = String.format(SH_BE_CONFIG, name);
        parseBehaviorConfig(blockingRead(showCommand, cli, id, ctx), builder, name);
        setCommittedAccessRate(blockingRead(showCommand, cli, id, ctx), builder);
    }

    @VisibleForTesting
    static void parseBehaviorConfig(String output, ConfigBuilder config, String configName) {
        ParsingUtils.parseField(output, 0,
                BE_RULE_REMARK::matcher,
                matcher -> matcher.group("remark"),
                config::setRemark);

        ParsingUtils.parseField(output, 0,
                BE_RULE_PERMIT::matcher,
                matcher -> matcher.group(),
                config::setPermitOccurence);

        ParsingUtils.parseField(output, 0,
                BE_RULE_STATISTIC::matcher,
                matcher -> matcher.group("statistic"),
                config::setStatistic);

        config.setName(configName);
    }

    @VisibleForTesting
    static void setCommittedAccessRate(String output, ConfigBuilder config) {
        ParsingUtils.parseField(output, 0,
                CIR_CBS::matcher,
                matcher -> matcher.group("cir"),
                config::setCir);

        ParsingUtils.parseField(output, 0,
                CIR_CBS::matcher,
                matcher -> matcher.group("cbs"),
                config::setCbs);

        ParsingUtils.parseField(output, 0,
                PIR_PBS::matcher,
                matcher -> matcher.group("pir"),
                config::setPir);

        ParsingUtils.parseField(output, 0,
                PIR_PBS::matcher,
                matcher -> matcher.group("pbs"),
                config::setPbs);

        ParsingUtils.parseField(output, 0,
                C_MODE::matcher,
                matcher -> matcher.group("colorMode"),
                config::setColorMode);

        ParsingUtils.parseField(output, 0,
                G_ACTION::matcher,
                matcher -> matcher.group("gAction"),
                config::setGreenAction);

        ParsingUtils.parseField(output, 0,
                Y_ACTION::matcher,
                matcher -> matcher.group("yAction"),
                config::setYellowAction);

        ParsingUtils.parseField(output, 0,
                R_ACTION::matcher,
                matcher -> matcher.group("rAction"),
                config::setRedAction);
    }
}