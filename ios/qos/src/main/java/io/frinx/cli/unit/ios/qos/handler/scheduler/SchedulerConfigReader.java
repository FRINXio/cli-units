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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosServicePolicyAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosServicePolicyAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.QosSchedulerConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String CLASS_PRIORITY = "priority";
    private static final Pattern SERVICE_POLICY = Pattern.compile("service-policy (?<name>.+)");

    private Cli cli;

    public SchedulerConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final Long sequence = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String policyOutput = blockingRead(f(InputConfigReader.SH_POLICY_MAP, policyName),
                cli, instanceIdentifier, readContext);
        final String className = getClassName(instanceIdentifier, readContext);

        configBuilder.setSequence(sequence);
        fillInConfig(className, policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void fillInConfig(String className, String policyOutput, ConfigBuilder configBuilder) {
        final String classOutput = Util.extractClass(className, policyOutput);
        if (classOutput.contains(CLASS_PRIORITY)) {
            configBuilder.setPriority(QosSchedulerConfig.Priority.STRICT);
        }
        ParsingUtils.parseField(classOutput, 0, SERVICE_POLICY::matcher,
            m -> m.group("name"),
            s -> fillInAug(s, configBuilder));
    }

    private static void fillInAug(String name, ConfigBuilder configBuilder) {
        QosServicePolicyAugBuilder augBuilder = new QosServicePolicyAugBuilder();
        augBuilder.setServicePolicy(name);
        configBuilder.addAugmentation(QosServicePolicyAug.class, augBuilder.build());
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