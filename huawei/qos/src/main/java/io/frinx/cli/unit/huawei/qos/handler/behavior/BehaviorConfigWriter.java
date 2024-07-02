/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.huawei.qos.handler.behavior;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.Behavior;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.behavior.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BehaviorConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            traffic behavior {$behavior_name}
            {% if ($data.statistic) %}statistic {$data.statistic}
            {% endif %}{% if ($data.remark) %}remark {$data.remark}
            {% endif %}{% if ($data.cir) %}car cir {$data.cir} green {$data.green_action} red {$data.red_action}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo traffic behavior {$behavior_name}
            return""";

    private Cli cli;

    public BehaviorConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = instanceIdentifier.firstKeyOf(Behavior.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, writeTemplate(behavior, dataAfter));
    }

    @VisibleForTesting
    String writeTemplate(String behavior, Config dataAfter) {
        return fT(WRITE_UPDATE_TEMPLATE,
                "behavior_name", behavior,
                "data", dataAfter);
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
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = instanceIdentifier.firstKeyOf(Behavior.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataBefore, deleteTemplate(behavior));
    }

    @VisibleForTesting
    String deleteTemplate(String behavior) {
        return fT(DELETE_TEMPLATE,
                "behavior_name", behavior);
    }
}