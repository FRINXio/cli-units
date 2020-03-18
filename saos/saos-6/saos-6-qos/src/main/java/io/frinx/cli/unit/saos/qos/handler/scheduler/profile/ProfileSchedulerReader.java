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

package io.frinx.cli.unit.saos.qos.handler.scheduler.profile;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProfileSchedulerReader implements CompositeListReader.Child<Scheduler, SchedulerKey,
        SchedulerBuilder>, CliConfigListReader<Scheduler, SchedulerKey, SchedulerBuilder> {

    private Cli cli;

    public ProfileSchedulerReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SchedulerKey> getAllIds(@Nonnull InstanceIdentifier<Scheduler> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return Collections.singletonList(new SchedulerKey(0L));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Scheduler> instanceIdentifier,
                                      @Nonnull SchedulerBuilder schedulerBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isProfile(instanceIdentifier, readContext)) {
            schedulerBuilder.setSequence(instanceIdentifier.firstKeyOf(Scheduler.class).getSequence());
        }
    }

    private boolean isProfile(InstanceIdentifier<Scheduler> id, ReadContext context) throws ReadFailedException {
        return ProfileSchedulerPolicyReader.getAllIds(cli, this, id, context)
                .contains(id.firstKeyOf(SchedulerPolicy.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
