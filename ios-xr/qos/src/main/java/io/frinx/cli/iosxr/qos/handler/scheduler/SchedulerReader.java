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

package io.frinx.cli.iosxr.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerReader implements CliConfigListReader<Scheduler, SchedulerKey, SchedulerBuilder> {

    private static final String SH_POLICY_MAP = "show running-config policy-map %s";
    static final Pattern CLASS_NAME_LINE = Pattern.compile("class (?<name>.+)");
    private Cli cli;

    public SchedulerReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SchedulerKey> getAllIds(@Nonnull InstanceIdentifier<Scheduler> instanceIdentifier, @Nonnull
            ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();
        String output = blockingRead(f(SH_POLICY_MAP, policyName), cli, instanceIdentifier, readContext);
        return getSequenceIds(output);
    }

    @VisibleForTesting
    public static List<SchedulerKey> getSequenceIds(String output) {
        long seq = 1;
        List<SchedulerKey> keys = new ArrayList<>();
        for (String line : output.split(ParsingUtils.NEWLINE.pattern())) {
            line = line.trim();
            Matcher matcher = CLASS_NAME_LINE.matcher(line);
            if (matcher.matches()) {
                keys.add(new SchedulerKey(seq));
                seq++;
            }
        }
        return keys;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Scheduler> instanceIdentifier, @Nonnull
            SchedulerBuilder builder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final Long sequence = instanceIdentifier.firstKeyOf(Scheduler.class)
                .getSequence();
        builder.setSequence(sequence);
        builder.setConfig(new ConfigBuilder().setSequence(sequence)
                .build());
    }
}
