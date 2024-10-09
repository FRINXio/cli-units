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

package io.frinx.cli.unit.saos.qos.handler.scheduler.service;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceSchedulerReader implements CompositeListReader.Child<Scheduler, SchedulerKey,
        SchedulerBuilder>, CliConfigListReader<Scheduler, SchedulerKey, SchedulerBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"traffic-services\"";

    private Cli cli;

    public ServiceSchedulerReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<SchedulerKey> getAllIds(@NotNull InstanceIdentifier<Scheduler> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        if (isService(instanceIdentifier, readContext)) {
            var portId = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
            var output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            return getAllIds(output, portId);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    List<SchedulerKey> getAllIds(String output, String portId) {
        Pattern pattern = Pattern.compile("traffic-services queuing egress-port-queue-group set "
                + "queue (?<sequence>\\d+) port " + portId + ".*");

        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("sequence"),
            seq -> new SchedulerKey(Long.parseLong(seq)));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Scheduler> instanceIdentifier,
                                      @NotNull SchedulerBuilder schedulerBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        schedulerBuilder.setSequence(instanceIdentifier.firstKeyOf(Scheduler.class).getSequence());
    }

    private boolean isService(InstanceIdentifier<Scheduler> id, ReadContext context) throws ReadFailedException {
        return ServiceSchedulerPolicyReader.getAllIds(cli, this, id, context)
                .contains(id.firstKeyOf(SchedulerPolicy.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}