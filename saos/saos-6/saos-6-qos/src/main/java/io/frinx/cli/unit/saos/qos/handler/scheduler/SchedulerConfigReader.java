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

package io.frinx.cli.unit.saos.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search running-config string \"traffic-profiling\"";
    private static final String VS = "%s name %s %s vs %s";

    private Cli cli;

    public SchedulerConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        Long sequence = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        parseSchedulerConfig(output, configBuilder, policyName, sequence);
    }

    @VisibleForTesting
    void parseSchedulerConfig(String output, ConfigBuilder builder, String policyName, Long sequence) {
        SaosQosSchedulerAugBuilder augBuilder = new SaosQosSchedulerAugBuilder();

        builder.setSequence(sequence);
        augBuilder.setType(Type.PortPolicy);
        setVs(output, augBuilder, policyName);

        builder.addAugmentation(SaosQosSchedulerAug.class, augBuilder.build());
    }

    private void setVs(String output, SaosQosSchedulerAugBuilder augBuilder, String policyName) {
        Pattern vs = Pattern.compile(f(VS, ".*", policyName, ".*", "(?<name>\\w+)"));
        ParsingUtils.parseField(output,
            vs::matcher,
            matcher -> matcher.group("name"),
            augBuilder::setVsName);
    }
}
