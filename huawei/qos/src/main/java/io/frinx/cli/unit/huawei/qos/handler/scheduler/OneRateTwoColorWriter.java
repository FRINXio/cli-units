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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerColorAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpYellowAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConformAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ExceedAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class OneRateTwoColorWriter implements CliWriter<OneRateTwoColor> {

    private static final String TRANSMIT_LINE = "pass";
    private static final String COS_LINE = "remark-8021p ";

    @SuppressWarnings("checkstyle:linelength")
    private static final String TEMPLATE = """
            system-view
            traffic behavior {$behavior}
            {% if ($config) %}car cir{% if ($config.cir_pct) %} pct {$config.cir_pct.value} {% endif %}{% if ($aug_hua.color_mode) %}mode {$aug_hua.color_mode.name}{% endif %}{% if ($conform_action) %} {$conform_action}{% endif %}{% if ($yellowAction) %} {$yellowAction}{% endif %}{% if ($exceed_action) %} {$exceed_action}
            {% endif %}{% else %}undo car cir
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            traffic behavior {$behavior}
            {% if ($config) %}undo car cir
            {% endif %}return""";

    private Cli cli;

    public OneRateTwoColorWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<OneRateTwoColor> instanceIdentifier,
                                       @NotNull OneRateTwoColor oneRateTwoColor,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = getBehaviorName(instanceIdentifier, writeContext);
        final ConformAction conformAction = oneRateTwoColor.getConformAction();
        final ExceedAction exceedAction = oneRateTwoColor.getExceedAction();
        final VrpYellowAction yellowAction = oneRateTwoColor.getAugmentation(VrpYellowAction.class);
        if (yellowAction != null || conformAction != null || exceedAction != null) {
            blockingWriteAndRead(cli, instanceIdentifier, oneRateTwoColor,
                    fT(TEMPLATE,
                            "behavior", behavior,
                            "config", oneRateTwoColor.getConfig(),
                            "aug_hua", getAugHua(oneRateTwoColor.getConfig()),
                            "conform_action", getConformActionCommand(conformAction),
                            "exceed_action", getExceedActionCommand(exceedAction),
                            "yellowAction", getYellowActionCommand(yellowAction)));
        } else {
            blockingWriteAndRead(cli, instanceIdentifier, oneRateTwoColor,
                    fT(TEMPLATE,
                            "behavior", behavior,
                            "config", oneRateTwoColor.getConfig(),
                            "aug", getAug(oneRateTwoColor.getConfig())));
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<OneRateTwoColor> id,
                                        @NotNull OneRateTwoColor dataBefore,
                                        @NotNull OneRateTwoColor dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<OneRateTwoColor> instanceIdentifier,
                                        @NotNull OneRateTwoColor oneRateTwoColor,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String behavior = getBehaviorName(instanceIdentifier, writeContext);

        blockingWriteAndRead(cli, instanceIdentifier, oneRateTwoColor,
                fT(DELETE_TEMPLATE,
                        "behavior", behavior,
                        "config", oneRateTwoColor.getConfig()));
    }

    private String getConformActionCommand(ConformAction conformAction) {
        String conformActionCommand = "green ";
        if (conformAction != null && conformAction.getConfig() != null) {
            QosConformActionAug aug = conformAction.getConfig().getAugmentation(QosConformActionAug.class);
            if (aug != null) {
                if (aug.isTransmit() != null) {
                    conformActionCommand += TRANSMIT_LINE + " ";
                }
                if (aug.getCosTransmit() != null) {
                    conformActionCommand += COS_LINE + aug.getCosTransmit().getValue();
                }
            }
            return conformActionCommand;
        }
        return null;
    }

    private String getExceedActionCommand(ExceedAction exceedAction) {
        String exceedActionCommand = "red ";
        if (exceedAction != null && exceedAction.getConfig() != null) {
            QosExceedActionAug aug = exceedAction.getConfig().getAugmentation(QosExceedActionAug.class);
            if (aug != null) {
                if (aug.isTransmit() != null) {
                    exceedActionCommand += TRANSMIT_LINE + " ";
                }
                if (aug.getCosTransmit() != null) {
                    exceedActionCommand += COS_LINE + aug.getCosTransmit().getValue();
                }
            }
            return exceedActionCommand;
        }
        return null;
    }

    private String getYellowActionCommand(VrpYellowAction yellowAction) {
        String yellowActionCommand = "yellow ";
        if (yellowAction != null && yellowAction.getYellowAction().getConfig() != null) {
            if (yellowAction.getYellowAction().getConfig().isTransmit()) {
                yellowActionCommand += TRANSMIT_LINE + " ";
            }
            if (yellowAction.getYellowAction().getConfig().getCosTransmit() != null) {
                yellowActionCommand += COS_LINE
                        + yellowAction.getYellowAction().getConfig().getCosTransmit().getValue();
            }
            return yellowActionCommand;
        }
        return null;
    }

    private VrpQosSchedulerColorAug getAugHua(Config config) {
        return config != null ? config.getAugmentation(VrpQosSchedulerColorAug.class) : null;
    }

    private QosMaxQueueDepthBpsAug getAug(Config config) {
        return config != null ? config.getAugmentation(QosMaxQueueDepthBpsAug.class) : null;
    }

    private String getBehaviorName(InstanceIdentifier<OneRateTwoColor> instanceIdentifier, WriteContext writeContext) {
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