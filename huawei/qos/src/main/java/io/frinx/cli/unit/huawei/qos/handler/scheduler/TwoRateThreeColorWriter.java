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
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.TwoRateThreeColor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TwoRateThreeColorWriter implements CliWriter<TwoRateThreeColor> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String TEMPLATE = """
            system-view
            traffic behavior {$behavior}
            {% if ($aug_hua.traffic_action == Gts) %}gts {% if ($config.cir) %}cir {$config.cir}{% endif %}{% if ($config.bc) %} cbs {$config.bc}{% endif %}{% if ($config.max_queue_depth_packets) %} queue-length {$config.max_queue_depth_packets}{% endif %}{% elseif ($aug_hua.traffic_action == Llq) %}queue {$aug_hua.traffic_action.name} bandwidth{% if ($config.cir) %} {$config.cir}{% endif %}{% if ($config.bc) %} cbs {$config.bc}{% endif %}
            {% elseif ($aug_hua.traffic_action == Af) %}queue {$aug_hua.traffic_action.name} bandwidth{% if ($config.cir_pct) %} pct {$config.cir_pct.value}
            {% endif %}{% elseif ($aug_hua.traffic_action == Wfq) %}queue {$aug_hua.traffic_action.name}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            traffic behavior {$behavior}
            {% if ($aug_hua.traffic_action == Gts) %}undo {$aug_hua.traffic_action.name}
            {% elseif ($aug_hua.traffic_action) %}undo queue {$aug_hua.traffic_action.name}
            {% endif %}return""";

    private Cli cli;

    public TwoRateThreeColorWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<TwoRateThreeColor> instanceIdentifier,
                                       @NotNull TwoRateThreeColor twoRateThreeColor,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = getBehavior(instanceIdentifier, writeContext);
        blockingWriteAndRead(cli, instanceIdentifier, twoRateThreeColor,
                fT(TEMPLATE,
                        "behavior", behavior,
                        "config", twoRateThreeColor.getConfig(),
                        "aug_hua", getAugHua(twoRateThreeColor.getConfig())));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<TwoRateThreeColor> id,
                                        @NotNull TwoRateThreeColor dataBefore,
                                        @NotNull TwoRateThreeColor dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<TwoRateThreeColor> instanceIdentifier,
                                        @NotNull TwoRateThreeColor twoRateThreeColor,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = getBehavior(instanceIdentifier, writeContext);

        blockingWriteAndRead(cli, instanceIdentifier, twoRateThreeColor,
                fT(DELETE_TEMPLATE,
                        "behavior", behavior,
                        "config", twoRateThreeColor.getConfig(),
                        "aug_hua", getAugHua(twoRateThreeColor.getConfig())));
    }

    private QosTwoColorConfig getAugHua(Config config) {
        return config != null ? config.getAugmentation(QosTwoColorConfig.class) : null;
    }

    private String getBehavior(InstanceIdentifier<TwoRateThreeColor> instanceIdentifier, WriteContext writeContext) {
        final Long seq = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence();
        final Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config> inputs =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Schedulers.class)
                        .child(Scheduler.class, new SchedulerKey(seq))
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                                .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config.class));
        if (inputs.isPresent()) {
            return inputs.get().getAugmentation(VrpQosSchedulerConfAug.class).getBehavior();
        }
        return null;
    }
}