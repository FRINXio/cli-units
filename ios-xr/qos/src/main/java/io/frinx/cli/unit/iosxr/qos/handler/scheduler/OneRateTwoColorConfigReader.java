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

package io.frinx.cli.unit.iosxr.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_POLICY_MAP = "show running-config policy-map %s | begin class %s";
    private static final Pattern RATE_LINE = Pattern.compile("police rate percent (?<rate>.+)");
    private static final Pattern QUEUE_LINE = Pattern.compile("queue-limit (?<queue>.+) ms");
    private static final Pattern BW_REM_LINE = Pattern.compile("bandwidth remaining percent (?<rem>.+)");
    private static final Pattern BW_LINE = Pattern.compile("bandwidth percent (?<bw>.+)");

    public static final String CLASS_DEFAULT = "class-default";
    private static final String CLASS_LINE = "(.*)class %s ?";
    private static final Pattern NEXT_CLASS_LINE = Pattern.compile("(.*)class(.*)");
    private Cli cli;

    public OneRateTwoColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull ConfigBuilder
            configBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();
        Long seq = instanceIdentifier.firstKeyOf(Scheduler.class)
                .getSequence();
        // there is always at least class-default
        Inputs inp = readContext.read(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                .child(Scheduler.class, new SchedulerKey(seq))
                .child(Inputs.class))
                .get();
        String classname = inp.getInput()
                .get(0)
                .getId();
        String output = blockingRead(f(SH_POLICY_MAP, policyName, classname), cli, instanceIdentifier, readContext);
        String finalOutput = limitOutput(output, classname);
        fillInConfig(finalOutput, configBuilder);
    }

    public static String limitOutput(String output, String classname) {
        String[] full = output.split(ParsingUtils.NEWLINE.pattern());
        // class-default is always the last class in the list
        if (!classname.endsWith(CLASS_DEFAULT)) {
            Pattern classDef = Pattern.compile(String.format(CLASS_LINE, classname));
            // however if we do have a class other than class-default, we need to make
            // sure we parse only fields belonging to that class, so limit the output
            // to the line where the next class declaration begins
            int first = -1;
            int until = -1;
            for (int i = 0; i < full.length; i++) {
                // first occurence should be the class definition
                Matcher matchDef = classDef.matcher(full[i]);
                if (matchDef.matches()) {
                    first = i;
                // we cannot match the same line for last class definition too
                } else {
                    // last is any other class definition, including class-default
                    Matcher last = NEXT_CLASS_LINE.matcher(full[i]);
                    if (last.matches()) {
                        until = i;
                    }
                }
                if (first != -1 && until != -1 && first < until) {
                    // do not continue searching if we already found what we are looking for
                    break;
                }
            }
            // we did not find next class, return the whole part from first find to the rest
            if (until == -1) {
                until = first + 1;
            }
            return String.join("\n", Arrays.asList(full)
                    .subList(first, until));
        }
        return String.join("\n", full);
    }

    @VisibleForTesting
    public static void fillInConfig(String finalOutput, ConfigBuilder builder) {
        ParsingUtils.parseField(finalOutput, RATE_LINE::matcher,
            matcher -> matcher.group("rate"),
            g -> builder.setMaxQueueDepthPercent(new Percentage(Short.valueOf(g))));

        QosMaxQueueDepthMsAugBuilder aug = new QosMaxQueueDepthMsAugBuilder();
        ParsingUtils.parseField(finalOutput, QUEUE_LINE::matcher,
            matcher -> matcher.group("queue"),
            g -> aug.setMaxQueueDepthMs(Long.valueOf(g)));
        if (aug.getMaxQueueDepthMs() != null) {
            builder.addAugmentation(QosMaxQueueDepthMsAug.class, aug.build());
        }
        ParsingUtils.parseField(finalOutput, BW_REM_LINE::matcher,
            matcher -> matcher.group("rem"),
            g -> builder.setCirPctRemaining(new Percentage(Short.valueOf(g))));

        ParsingUtils.parseField(finalOutput, BW_LINE::matcher,
            matcher -> matcher.group("bw"),
            g -> builder.setCirPct(new Percentage(Short.valueOf(g))));
    }
}