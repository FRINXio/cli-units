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

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConformAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ExceedAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.Input;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorWriter implements CliWriter<OneRateTwoColor> {

    private static final String TRANSMIT_LINE = "transmit";
    private static final String DROP_LINE = "drop";
    private static final String COS_LINE = "set-cos-transmit ";
    private static final String DEI_LINE = "set-dot1ad-dei-transmit ";
    private static final String DSCP_LINE = "set-dscp-transmit ";
    private static final String QOS_LINE = "set-qos-transmit ";

    private static final String TEMPLATE = "configure terminal\n"
            + "policy-map {$policy_name}\n"
            + "{% if ($class_name) %}class {$class_name}\n"
            + "{% if ($config.cir_pct_remaining) %}bandwidth remaining percent {$config.cir_pct_remaining.value}\n"
            + "{% else %}no bandwidth remaining percent\n{% endif %}"
            + "{% if ($config.cir_pct) %}bandwidth percent {$config.cir_pct.value}\n"
            + "{% else %}no bandwidth percent\n{% endif %}"
            + "{% if ($aug.max_queue_depth_bps) %}shape average {$aug.max_queue_depth_bps}\n"
            + "{% else %}no shape average\n{% endif %}"
            + "{% if ($config.cir) %}police cir {$config.cir}"
            + "{% if ($config.bc) %} bc {$config.bc}{% endif %}"
            + "{% if ($conform_action) %} conform-action {$conform_action}{% endif %}"
            + "{% if ($exceed_action) %} exceed-action {$exceed_action}{% endif %}"
            + "\nexit\n{% else %}no police 1m\n{% endif %}"  // exit because this command also enters police conf. mode
            + "{% endif %}"
            + "end";

    private Cli cli;

    public OneRateTwoColorWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<OneRateTwoColor> instanceIdentifier,
                                       @Nonnull OneRateTwoColor oneRateTwoColor,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, writeContext);
        final ConformAction conformAction = oneRateTwoColor.getConformAction();
        final ExceedAction exceedAction = oneRateTwoColor.getExceedAction();

        blockingWriteAndRead(cli, instanceIdentifier, oneRateTwoColor,
                fT(TEMPLATE,
                        "policy_name", policyName,
                        "class_name", className,
                        "config", oneRateTwoColor.getConfig(),
                        "conform_action", getConformActionCommand(conformAction),
                        "exceed_action", getExceedActionCommand(exceedAction),
                        "aug", getAug(oneRateTwoColor.getConfig())));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<OneRateTwoColor> id,
                                        @Nonnull OneRateTwoColor dataBefore,
                                        @Nonnull OneRateTwoColor dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<OneRateTwoColor> instanceIdentifier,
                                        @Nonnull OneRateTwoColor oneRateTwoColor,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String className = getClassName(instanceIdentifier, writeContext);

        blockingWriteAndRead(cli, instanceIdentifier, oneRateTwoColor,
                fT(TEMPLATE,
                        "policy_name", policyName,
                        "class_name", className));
    }

    private String getConformActionCommand(ConformAction conformAction) {
        if (conformAction != null && conformAction.getConfig() != null) {
            QosConformActionAug aug = conformAction.getConfig().getAugmentation(QosConformActionAug.class);
            if (aug != null) {
                if (aug.isTransmit() != null) {
                    return TRANSMIT_LINE;
                } else if (aug.getCosTransmit() != null) {
                    return COS_LINE + aug.getCosTransmit().getValue();
                } else if (aug.getDeiTransmit() != null) {
                    return DEI_LINE + aug.getDeiTransmit().getValue();
                } else if (aug.getDscpTransmit() != null) {
                    if (aug.getDscpTransmit().getDscp() != null) {
                        return DSCP_LINE + aug.getDscpTransmit().getDscp().getValue();
                    } else if (aug.getDscpTransmit().getDscpEnumeration() != null) {
                        return DSCP_LINE + aug.getDscpTransmit().getDscpEnumeration().getName();
                    }
                } else if (aug.getQosTransmit() != null) {
                    return QOS_LINE + aug.getQosTransmit().getUint32();
                }
            }
        }
        return null;
    }

    private String getExceedActionCommand(ExceedAction exceedAction) {
        if (exceedAction != null && exceedAction.getConfig() != null) {
            if (exceedAction.getConfig().isDrop() != null) {
                return DROP_LINE;
            }

            QosExceedActionAug aug = exceedAction.getConfig().getAugmentation(QosExceedActionAug.class);
            if (aug != null) {
                if (aug.isTransmit() != null) {
                    return TRANSMIT_LINE;
                } else if (aug.getCosTransmit() != null) {
                    return COS_LINE + aug.getCosTransmit().getValue();
                } else if (aug.getDeiTransmit() != null) {
                    return DEI_LINE + aug.getDeiTransmit().getValue();
                } else if (aug.getDscpTransmit() != null) {
                    if (aug.getDscpTransmit().getDscp() != null) {
                        return DSCP_LINE + aug.getDscpTransmit().getDscp().getValue();
                    } else if (aug.getDscpTransmit().getDscpEnumeration() != null) {
                        return DSCP_LINE + aug.getDscpTransmit().getDscpEnumeration().getName();
                    }
                } else if (aug.getQosTransmit() != null) {
                    return QOS_LINE + aug.getQosTransmit().getUint32();
                }
            }
        }
        return null;
    }

    private QosMaxQueueDepthBpsAug getAug(Config config) {
        return config != null ? config.getAugmentation(QosMaxQueueDepthBpsAug.class) : null;
    }

    private String getClassName(InstanceIdentifier<OneRateTwoColor> instanceIdentifier, WriteContext writeContext) {
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