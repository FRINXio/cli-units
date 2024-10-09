/*
 * Copyright © 2021 Frinx and others.
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
package io.frinx.cli.unit.huawei.qos.handler.scheduler;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class InputConfigWriter implements CliWriter<Config> {
    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            traffic behavior {$behavior_name}
            {% if ($statistic) %}statistic {$statistic}
            {% endif %}{% if ($remark) %}remark 8021p {$remark}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            traffic behavior {$behavior_name}
            {% if ($statistic) %}undo statistic
            {% endif %}{% if ($remark) %}undo remark 8021p
            {% endif %}return""";

    private Cli cli;

    public InputConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = getBehavior(instanceIdentifier, writeContext);
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_UPDATE_TEMPLATE,
                        "behavior_name", behavior,
                        "statistic", config.getAugmentation(VrpQosSchedulerInputAug.class).getStatistic(),
                        "remark", getCos(config)));
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
        final String behavior = getBehavior(instanceIdentifier, writeContext);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE,
                        "behavior_name", behavior,
                        "statistic", (config.getAugmentation(VrpQosSchedulerInputAug.class).getStatistic() != null)
                                ? "delete" : null,
                        "remark", (getCos(config) != null) ? "delete" : null));
    }

    private Short getCos(Config config) {
        final QosCosAug aug = config.getAugmentation(QosCosAug.class);
        if (aug != null && aug.getCos() != null) {
            return aug.getCos().getValue();
        }
        return null;
    }

    private String getBehavior(InstanceIdentifier<Config> instanceIdentifier, WriteContext readContext) {
        final Long seq = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        return readContext.readBefore(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                        .child(Scheduler.class, new SchedulerKey(seq))
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                                .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config.class)
                        )
                .get()
                .getAugmentation(VrpQosSchedulerConfAug.class)
                .getBehavior();
    }
}