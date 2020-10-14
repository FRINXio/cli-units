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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.qos.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.CosValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.Input;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_POLICY_MAP = "show policy-map %s";
    private static final Pattern COS_LINE = Pattern.compile("set cos (?<cos>.+)");

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
        final String policyOutput = blockingRead(f(SH_POLICY_MAP, policyName), cli, instanceIdentifier, readContext);
        parseConfig(className, policyOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String className, String policyOutput, ConfigBuilder configBuilder) {
        final String classOutput = Util.extractClass(className, policyOutput);
        QosCosAugBuilder augBuilder = new QosCosAugBuilder();
        ParsingUtils.parseField(classOutput, COS_LINE::matcher,
            matcher -> matcher.group("cos"),
            s -> augBuilder.setCos(CosValue.getDefaultInstance(s)));
        configBuilder.addAugmentation(QosCosAug.class, augBuilder.build());
    }

}