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

import com.x5.template.Chunk;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosServicePolicyAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.Input;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String TEMPLATE = """
            configure terminal
            policy-map {$policy_name}
            {% if ($class_name) %}class {$class_name}
            {% if ($config.priority) %}{% if ($delete) %}no {% endif %}priority
            {% endif %}{% if ($aug.service_policy) %}{% if ($delete) %}no {% endif %}service-policy {$aug.service_policy}
            {% endif %}{% endif %}end""";

    private Cli cli;

    public SchedulerConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, writeContext);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(TEMPLATE,
                        "policy_name", policyName,
                        "class_name", className,
                        "config", config,
                        "aug", config.getAugmentation(QosServicePolicyAug.class)));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, writeContext);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(TEMPLATE,
                        "policy_name", policyName,
                        "class_name", className,
                        "config", config,
                        "aug", config.getAugmentation(QosServicePolicyAug.class),
                        "delete", Chunk.TRUE));
    }

    private String getClassName(InstanceIdentifier<Config> instanceIdentifier, WriteContext writeContext) {
        final Long seq = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        final Optional<Inputs> inputs = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                .child(Scheduler.class, new SchedulerKey(seq))
                .child(Inputs.class));
        if (inputs.isPresent()) {
            final List<Input> inputList = inputs.get().getInput();
            if (inputList != null && !inputList.isEmpty()) {
                return inputList.get(0).getId();
            }
        }
        return null;
    }
}