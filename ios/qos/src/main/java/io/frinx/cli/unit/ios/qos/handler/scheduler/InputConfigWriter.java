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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            configure terminal
            policy-map {$policy_name}
            class {$class_name}
            {% if ($cos) %}set cos {$cos}
            {% else if ($cos_before) %}no set cos {$cos_before}
            {% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            policy-map {$policy_name}
            no class {$class_name}
            end""";

    private Cli cli;

    public InputConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_UPDATE_TEMPLATE,
                        "policy_name", policyName,
                        "class_name", config.getId(),
                        "cos", getCos(config)));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String policyName = id.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, id, dataAfter,
                fT(WRITE_UPDATE_TEMPLATE,
                        "policy_name", policyName,
                        "class_name", dataAfter.getId(),
                        "cos", getCos(dataAfter),
                        "cos_before", getCos(dataBefore)));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE,
                        "policy_name", policyName,
                        "class_name", config.getId()));
    }

    private Short getCos(Config config) {
        final QosCosAug aug = config.getAugmentation(QosCosAug.class);
        if (aug != null && aug.getCos() != null) {
            return aug.getCos().getValue();
        }
        return null;
    }
}