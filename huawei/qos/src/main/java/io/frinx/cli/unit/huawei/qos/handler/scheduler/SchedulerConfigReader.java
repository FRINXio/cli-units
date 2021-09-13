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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern PRECEDENCE_LINE = Pattern.compile("Precedence: (?<precedence>.+)");
    private static final Pattern BEHAVIOR_LINE = Pattern.compile("Behavior: (?<name>.+)");

    private Cli cli;

    public SchedulerConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final Long sequence = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, readContext);
        final String policyOutput = blockingRead(f(InputConfigReader.SH_POLICY_CLASS_MAP, policyName, className),
                cli, instanceIdentifier, readContext);

        configBuilder.setSequence(sequence);
        fillInConfig(policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void fillInConfig(String policyOutput, ConfigBuilder configBuilder) {
        VrpQosSchedulerConfAugBuilder huaweiAugBuilder = new VrpQosSchedulerConfAugBuilder();

        ParsingUtils.parseField(policyOutput, 0, PRECEDENCE_LINE::matcher,
            m -> m.group("precedence"),
            s -> fillInAugHua(s, huaweiAugBuilder, "precedence"));
        ParsingUtils.parseField(policyOutput, BEHAVIOR_LINE::matcher,
            matcher -> matcher.group("name"),
            s -> fillInAugHua(s, huaweiAugBuilder, "behavior"));
        configBuilder.addAugmentation(VrpQosSchedulerConfAug.class, huaweiAugBuilder.build());

    }

    private static void fillInAugHua(String line, VrpQosSchedulerConfAugBuilder configBuilder, String type) {
        if ("behavior".equals(type)) {
            configBuilder.setBehavior(line);
        } else if ("precedence".equals(type)) {
            configBuilder.setVrpPrecedence(line);
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
