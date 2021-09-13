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
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class OneRateTwoColorExceedActionConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final Pattern EXCEED_ACTION =
            Pattern.compile("Exceed  Action: remark 8021p (?<cos>.+) and (?<action>.+)");

    private Cli cli;

    public OneRateTwoColorExceedActionConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        // Exceed action for Huawei devices is also called a red action (or just red)
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, readContext);
        final String policyOutput = blockingRead(f(InputConfigReader.SH_POLICY_CLASS_MAP, policyName, className),
                cli, instanceIdentifier, readContext);
        parseConfig(policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String policyOutput, ConfigBuilder configBuilder) {
        final QosExceedActionAugBuilder augBuilder = new QosExceedActionAugBuilder();
        ParsingUtils.parseField(policyOutput, EXCEED_ACTION::matcher,
            matcher -> matcher.group("action"),
            g -> fillInAug(g, augBuilder));

        ParsingUtils.parseField(policyOutput, EXCEED_ACTION::matcher,
            matcher -> matcher.group("cos"),
            g -> augBuilder.setCosTransmit(Cos.getDefaultInstance(g)));

        configBuilder.addAugmentation(QosExceedActionAug.class, augBuilder.build());
    }


    private static void fillInAug(String action, QosExceedActionAugBuilder augBuilder) {
        if (action.equals("pass")) {
            augBuilder.setTransmit(true);
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
}
