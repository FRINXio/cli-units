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
package io.frinx.cli.unit.huawei.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigInteger;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerColorConfig.ColorMode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerColorAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerColorAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern CIR_CBS_LINE = Pattern.compile("CIR (?<cir>.+) \\(Kbps\\), CBS (?<cbs>.+) \\(byte\\)");
    private static final Pattern BW_LINE = Pattern.compile("Bandwidth (?<bw>.+) \\(%\\)");
//    private static final Pattern BW_CBS_LINE =
//            Pattern.compile("Bandwidth (?<cir>.+) \\(Kbps\\) CBS (?<cbs>.+) \\(Bytes\\)");
    private static final Pattern SHAPE_LINE = Pattern.compile("Queue Length: (?<bitrate>.+) \\(Packets\\).*");
    private static final Pattern COLOR_MODE_LINE = Pattern.compile("Color Mode: (?<mode>.+)");
    private static final Pattern DROP_METHOD_LINE = Pattern.compile("Drop Method: (?<method>.+)");

    private Cli cli;

    public OneRateTwoColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        // By default, green packets and yellow packets are allowed to pass through, and red packets are discarded.
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, readContext);
        final String policyOutput = blockingRead(f(InputConfigReader.SH_POLICY_CLASS_MAP, policyName, className),
                cli, instanceIdentifier, readContext);
        parseConfig(policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String policyOutput, ConfigBuilder builder) {
        // cir and bc values can be found after CIR or Bandwidth command
        VrpQosSchedulerColorAugBuilder augBuilder = new VrpQosSchedulerColorAugBuilder();

        ParsingUtils.parseField(policyOutput, CIR_CBS_LINE::matcher,
            matcher -> matcher.group("cir"),
            g -> builder.setCir(new BigInteger(g)));
        ParsingUtils.parseField(policyOutput, CIR_CBS_LINE::matcher,
            matcher -> matcher.group("cbs"),
            g -> builder.setBc(new Long(g)));

        ParsingUtils.parseField(policyOutput, BW_LINE::matcher,
            matcher -> matcher.group("bw"),
            g -> builder.setCirPct(new Percentage(Short.valueOf(g))));

        ParsingUtils.parseField(policyOutput, SHAPE_LINE::matcher,
            matcher -> matcher.group("bitrate"),
            g -> builder.setMaxQueueDepthPackets(new Long(g)));

        ParsingUtils.parseField(policyOutput, COLOR_MODE_LINE::matcher,
            matcher -> matcher.group("mode"),
            g -> fillInAug(g, augBuilder, "color"));

        ParsingUtils.parseField(policyOutput, DROP_METHOD_LINE::matcher,
            matcher -> matcher.group("method"),
            s -> fillInAug(s, augBuilder, "method"));


        builder.addAugmentation(VrpQosSchedulerColorAug.class, augBuilder.build());
    }

    private static void fillInAug(String output, VrpQosSchedulerColorAugBuilder augBuilder, String type) {
        if ("method".equals(type)) {
            augBuilder.setDropMethod(output);
        } else if ("color".equals(type)) {
            augBuilder.setColorMode(getColorMode(output));
        }
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

    private static ColorMode getColorMode(String colorMode) {
        if (colorMode.toLowerCase().contains("blind")) {
            return ColorMode.ColorBlind;
        } else if (colorMode.toLowerCase().contains("aware")) {
            return ColorMode.ColorAware;
        }
        return null;
    }



}
