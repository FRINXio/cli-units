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

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorConfigWriter implements CliWriter<Config> {

    private static final String COLOR_T = "policy-map {$name}\n"
            + "class {$className}\n"
            + "{% if ($config.cir_pct) %}bandwidth percent {$config.cir_pct.value}\n{% else %}no bandwidth\n{% endif %}"
            + "{% if ($config.cir_pct_remaining) %}bandwidth remaining percent {$config.cir_pct_remaining.value}\n{% "
            + "else %}no bandwidth remaining\n{% endif %}"
            + "{% if ($config.max_queue_depth_percent) %}police rate percent {$config.max_queue_depth_percent"
            + ".value}\n{% else %}no police\n{% endif %}"
            + "{% if ($aug) %}{% if ($aug.max_queue_depth_ms) %}"
            + "queue-limit {$aug.max_queue_depth_ms} ms\n{% endif %}"
            + "{% else %}no queue-limit\n"
            + "{% endif %}"
            + "root";

    private Cli cli;

    public OneRateTwoColorConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();
        Long seq = instanceIdentifier.firstKeyOf(Scheduler.class)
                .getSequence();
        Inputs inp = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                .child(Scheduler.class, new SchedulerKey(seq))
                .child(Inputs.class))
                .get();
        String classname = inp.getInput()
                .get(0)
                .getId();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(COLOR_T, "name", policyName, "className", classname,
                        "config", config, "aug", config.getAugmentation(QosMaxQueueDepthMsAug.class)));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull
            Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();
        Long seq = instanceIdentifier.firstKeyOf(Scheduler.class)
                .getSequence();
        Inputs inp = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                .child(Scheduler.class, new SchedulerKey(seq))
                .child(Inputs.class))
                .get();
        String classname = inp.getInput()
                .get(0)
                .getId();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(COLOR_T, "name", policyName, "className", classname));
    }
}
