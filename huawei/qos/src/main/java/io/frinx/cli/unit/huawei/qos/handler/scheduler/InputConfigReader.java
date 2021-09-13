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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigInteger;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.Input;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_POLICY_CLASS_MAP = "display traffic policy user-defined %s classifier %s";
    private static final Pattern COS_LINE = Pattern.compile("Remark 8021p (?<cos>.+)");
    private static final Pattern PRIORITY_LINE = Pattern.compile("Max number of hashed queues: (?<prio>.+)");
    private static final Pattern STATISTICS_LINE = Pattern.compile("statistic: (?<stats>.+)");

    private Cli cli;

    public InputConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = instanceIdentifier.firstKeyOf(Input.class).getId();
        configBuilder.setId(className);
        configBuilder.setQueue(className);
        final String policyClassOutput = blockingRead(
                f(SH_POLICY_CLASS_MAP, policyName, className), cli, instanceIdentifier, readContext);
        setPriority(policyClassOutput, configBuilder);
        parseConfig(policyClassOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String policyClassOutput, ConfigBuilder configBuilder) {
        VrpQosSchedulerInputAugBuilder augBuilder = new VrpQosSchedulerInputAugBuilder();
        ParsingUtils.parseField(policyClassOutput, COS_LINE::matcher,
            matcher -> matcher.group("cos"),
            s -> fillInAugCos(s, configBuilder));

        ParsingUtils.parseField(policyClassOutput, STATISTICS_LINE::matcher,
            matcher -> matcher.group("stats"),
            s -> fillInAugHua(s, augBuilder));

        configBuilder.addAugmentation(VrpQosSchedulerInputAug.class, augBuilder.build());
    }

    private static void fillInAugCos(String line, ConfigBuilder configBuilder) {
        QosCosAugBuilder augBuilder = new QosCosAugBuilder();
        augBuilder.setCos(Cos.getDefaultInstance(line));
        configBuilder.addAugmentation(QosCosAug.class, augBuilder.build());
    }

    private static void fillInAugHua(String line, VrpQosSchedulerInputAugBuilder augBuilder) {
        augBuilder.setStatistic(line);
    }

    @VisibleForTesting
    public static void setPriority(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, PRIORITY_LINE::matcher,
            matcher -> matcher.group("prio"),
            g -> configBuilder.setWeight(BigInteger.valueOf(Long.valueOf(g))));
    }
}
