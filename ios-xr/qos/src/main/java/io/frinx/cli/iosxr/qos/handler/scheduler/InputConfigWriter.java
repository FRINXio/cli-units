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
import io.frinx.openconfig.openconfig.qos.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputConfigWriter implements CliWriter<Config> {

    private static final String INPUT_T = "policy-map {$name}\n"
            + "{% if ($delete) %}no {% endif %}class {$className}\n"
            + "{% if (!$delete) %} {% if ($priority) %}priority level {$priority}\n{% else %}no priority\n{% endif "
            + "%}{% endif %}"
            + "root";
    private static final String CLASS_DEFAULT = "class-default";

    private Cli cli;

    public InputConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext) throws WriteFailedException, IllegalArgumentException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();

        if (!config.getId().equals(CLASS_DEFAULT)) {
            if (writeContext.readAfter(RWUtils.cutId(instanceIdentifier,
                    IIDs.QO_SC_SC_SC_SCHEDULER)).get().getOneRateTwoColor() == null) {
                throw new IllegalArgumentException("Cannot define empty scheduler. Scheduler needs to have settings under OneRateTwoColor");
            }
        }

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(INPUT_T, "name", policyName, "className", config.getId(), "priority", config.getWeight()));
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
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(INPUT_T, "name", policyName, "className", config.getId(), "delete", true));
    }
}
