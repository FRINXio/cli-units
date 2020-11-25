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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Dei;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorExceedActionConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern EXCEED_ACTION = Pattern.compile("exceed-action (?<action>.+)");

    private Cli cli;

    public OneRateTwoColorExceedActionConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, readContext);
        final String policyOutput = blockingRead(f(InputConfigReader.SH_POLICY_MAP, policyName),
                cli, instanceIdentifier, readContext);
        parseConfig(className, policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String className, String policyOutput, ConfigBuilder configBuilder) {
        final String classOutput = Util.extractClass(className, policyOutput);
        ParsingUtils.parseField(classOutput, EXCEED_ACTION::matcher,
            matcher -> matcher.group("action"),
            g -> fillInConfig(g, configBuilder));
    }

    private static void fillInConfig(String action, ConfigBuilder configBuilder) {
        if (action.equals("drop")) {
            configBuilder.setDrop(true);
            return;
        }

        final QosExceedActionAugBuilder augBuilder = new QosExceedActionAugBuilder();
        fillInAug(action, augBuilder);
        configBuilder.addAugmentation(QosExceedActionAug.class, augBuilder.build());
    }

    private static void fillInAug(String action, QosExceedActionAugBuilder augBuilder) {
        if (action.equals("transmit")) {
            augBuilder.setTransmit(true);
            return;
        }

        final String value = action.split("\\s")[1];
        if (action.contains("cos")) {
            augBuilder.setCosTransmit(Cos.getDefaultInstance(value));
        } else if (action.contains("dei")) {
            augBuilder.setDeiTransmit(Dei.getDefaultInstance(value));
        } else if (action.contains("dscp")) {
            augBuilder.setDscpTransmit(DscpBuilder.getDefaultInstance(value));
        } else if (action.contains("qos")) {
            augBuilder.setQosTransmit(QosGroupBuilder.getDefaultInstance(value));
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