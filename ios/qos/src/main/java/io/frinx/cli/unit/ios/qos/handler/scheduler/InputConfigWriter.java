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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_INPUT = "configure terminal\n"
            + "policy-map {$policy_name}\n"
            + "class {$class_name}\n"
            + "{% if ($cos) %}set cos {$cos}\n"
            + "{% else if ($cos_before) %}no set cos {$cos_before}\n"
            + "{% endif %}"
            + "end";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "policy-map {$policy_name}\n"
            + "no class {$class_name}\n"
            + "end";

    private Cli cli;

    public InputConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_UPDATE_INPUT,
                "policy_name", policyName,
                "class_name", config.getId(),
                "cos", config.getAugmentation(QosCosAug.class).getCos().getValue()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = id.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, id, dataAfter,
                fT(WRITE_UPDATE_INPUT,
                "policy_name", policyName,
                "class_name", dataAfter.getId(),
                "cos", getCos(dataAfter),
                "cos_before", getCos(dataBefore)));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_INPUT,
                "policy_name", policyName,
                "class_name", config.getId()));
    }

    private Short getCos(Config config) {
        QosCosAug aug = config.getAugmentation(QosCosAug.class);
        if (aug != null) {
            if (aug.getCos() != null) {
                return aug.getCos().getValue();
            }
        }
        return null;
    }

}