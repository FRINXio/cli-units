/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.qos.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigInteger;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern BW_REM_LINE = Pattern.compile("bandwidth remaining percent (?<rem>.+)");
    private static final Pattern BW_LINE = Pattern.compile("bandwidth (percent )?(?<bw>\\d+)");
    private static final Pattern CIR_BC_LINE = Pattern.compile("police cir (?<bitrate>\\S+) bc (?<burst>\\S+)");
    private static final Pattern SHAPE_LINE = Pattern.compile("shape average (?<bitrate>.+)");

    private Cli cli;

    public OneRateTwoColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, readContext);
        final String policyOutput = blockingRead(f(InputConfigReader.SH_POLICY_MAP, policyName),
                cli, instanceIdentifier, readContext);
        parseConfig(className, policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String className, String policyOutput, ConfigBuilder builder) {
        final String classOutput = Util.deleteBrackets(Util.extractClass(className, policyOutput));
        ParsingUtils.parseField(classOutput, BW_REM_LINE::matcher,
            matcher -> matcher.group("rem"),
            g -> builder.setCirPctRemaining(new Percentage(Short.valueOf(g))));
        ParsingUtils.parseField(classOutput, BW_LINE::matcher,
            matcher -> matcher.group("bw"),
            g -> builder.setCirPct(new Percentage(Short.valueOf(g))));
        ParsingUtils.parseField(classOutput, CIR_BC_LINE::matcher,
            matcher -> matcher.group("bitrate"),
            g -> builder.setCir(new BigInteger(g)));
        ParsingUtils.parseField(classOutput, CIR_BC_LINE::matcher,
            matcher -> matcher.group("burst"),
            g -> builder.setBc(Long.valueOf(g)));
        ParsingUtils.parseField(classOutput, SHAPE_LINE::matcher,
            matcher -> matcher.group("bitrate"),
            g -> fillInAug(g, builder));
    }

    private static void fillInAug(String output, ConfigBuilder builder) {
        QosMaxQueueDepthBpsAugBuilder augBuilder = new QosMaxQueueDepthBpsAugBuilder();
        augBuilder.setMaxQueueDepthBps(Long.valueOf(output));
        builder.addAugmentation(QosMaxQueueDepthBpsAug.class, augBuilder.build());
    }

    private String getClassName(InstanceIdentifier<Config> instanceIdentifier, ReadContext readContext) {
        final Long seq = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        return readContext.read(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                .child(Scheduler.class, new SchedulerKey(seq))
                .child(Inputs.class))
                .get()
                .getInput()
                .get(0)
                .getId();
    }
}