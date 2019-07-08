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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigInteger;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.Input;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern PRIORITY_LINE = Pattern.compile("priority level (?<prio>([12]))");
    private Cli cli;

    public InputConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder
            configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();
        String className = instanceIdentifier.firstKeyOf(Input.class)
                .getId();
        String output = blockingRead(f(OneRateTwoColorConfigReader.SH_POLICY_MAP, policyName, className), cli,
                instanceIdentifier, readContext);
        setPriority(output, className, configBuilder);
        configBuilder.setId(className);
        configBuilder.setQueue(className);
    }

    @VisibleForTesting
    public static void setPriority(String output, String className, ConfigBuilder configBuilder) {
        String finalOutput = OneRateTwoColorConfigReader.limitOutput(output, className);
        ParsingUtils.parseField(finalOutput, PRIORITY_LINE::matcher,
            matcher -> matcher.group("prio"),
            g -> configBuilder.setWeight(BigInteger.valueOf(Long.valueOf(g))));
    }
}
