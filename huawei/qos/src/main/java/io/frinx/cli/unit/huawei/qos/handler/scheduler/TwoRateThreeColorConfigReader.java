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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerTwoColorConfig.TrafficAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TwoRateThreeColorConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final Pattern BW_CBS_LINE =
            Pattern.compile("Bandwidth (?<cir>.+) \\(Kbps\\) CBS (?<cbs>.+) \\(Bytes\\)");
    private static final Pattern BW_LINE = Pattern.compile("Bandwidth (?<bw>.+) \\(%\\)");
    private static final Pattern SHAPE_LINE = Pattern.compile("Queue [Ll]ength[:]* (?<bitrate>.+) \\(Packets\\).*");
    private static final Pattern CIR_CBS_LINE = Pattern.compile("CIR (?<cir>.+) \\(Kbps\\), CBS (?<cbs>.+) \\(byte\\)");

    private Cli cli;

    public TwoRateThreeColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, readContext);
        final String output = blockingRead(f(InputConfigReader.SH_POLICY_CLASS_MAP, policyName, className),
                cli, instanceIdentifier, readContext);
        parseThreeColorConfig(output, configBuilder);
    }

    @VisibleForTesting
    public static void parseThreeColorConfig(String output, ConfigBuilder builder) {
        QosTwoColorConfigBuilder augBuilder = new QosTwoColorConfigBuilder();
        // Bandwidth section for cir and bc
        ParsingUtils.parseField(output, BW_CBS_LINE::matcher,
            matcher -> matcher.group("cir"),
            g -> builder.setCir(new BigInteger(g)));
        ParsingUtils.parseField(output, CIR_CBS_LINE::matcher,
            matcher -> matcher.group("cbs"),
            g -> builder.setBc(new Long(g)));
        ParsingUtils.parseField(output, BW_CBS_LINE::matcher,
            matcher -> matcher.group("cbs"),
            g -> builder.setBc(new Long(g)));
        ParsingUtils.parseField(output, CIR_CBS_LINE::matcher,
            matcher -> matcher.group("cir"),
            g -> builder.setCir(new BigInteger(g)));

        ParsingUtils.parseField(output, BW_LINE::matcher,
            matcher -> matcher.group("bw"),
            g -> builder.setCirPct(new Percentage(Short.valueOf(g))));

        ParsingUtils.parseField(output, SHAPE_LINE::matcher,
            matcher -> matcher.group("bitrate"),
            g -> fillInAug(g, augBuilder));

        setTrafficAction(output, augBuilder);
        builder.addAugmentation(QosTwoColorConfig.class, augBuilder.build());
    }

    private static void fillInAug(String output, QosTwoColorConfigBuilder augBuilder) {
        augBuilder.setMaxQueueDepthPackets(new Long(output));

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

    private static void setTrafficAction(String policyOutput, QosTwoColorConfigBuilder augBuilder) {
        Map<String, String> myMap = new HashMap<String, String>() {{
                put("Low-latency", "llq");
                put("Flow based Weighted Fair Queueing", "wfq");
                put("Assured Forwarding", "af");
                put("General Traffic Shape", "gts");
            }
        };
        for (String e: myMap.keySet()) {
            Pattern pattern = Pattern.compile(e);
            Matcher matcher = pattern.matcher(policyOutput);
            if (matcher.find()) {
                augBuilder.setTrafficAction(getTrafficAction(myMap.get(e)));
                return;
            }
        }
    }

    private static TrafficAction getTrafficAction(final String name) {
        for (final TrafficAction type : TrafficAction.values()) {
            if (name.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }
}
