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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerReader implements CliConfigListReader<Scheduler, SchedulerKey, SchedulerBuilder> {

    private static final String SHOW_COMMAND = "configuration search running-config string \"traffic-services\"";
    private static final String SEQUENCE = "traffic-services %s %s port %s%s";

    private Cli cli;

    public SchedulerReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SchedulerKey> getAllIds(@Nonnull InstanceIdentifier<Scheduler> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();

        if (!name.matches("\\d+")) {
            return Collections.singletonList(new SchedulerKey(0L));
        } else {
            String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            return getAllIds(output, name);
        }
    }

    @VisibleForTesting
    List<SchedulerKey> getAllIds(String output, String name) {
        Pattern pattern = Pattern.compile(f(SEQUENCE, ".*", "(?<sequence>\\d+)", name, ".*"));
        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("sequence"),
            seq -> new SchedulerKey(Long.parseLong(seq)));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Scheduler> instanceIdentifier,
                                      @Nonnull SchedulerBuilder schedulerBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        schedulerBuilder.setSequence(instanceIdentifier.firstKeyOf(Scheduler.class).getSequence());
    }
}
