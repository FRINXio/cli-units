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

package io.frinx.cli.unit.saos.qos.handler.scheduler.service;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceSchedulerPolicyWriter
        implements CompositeWriter.Child<SchedulerPolicy>, CliWriter<SchedulerPolicy> {

    private static final String WRITE_SERVICE =
            "traffic-services queuing egress-port-queue-group set queue {$queue} port {$port}"
            + "{% if ($eir) %} eir {$eir}{% endif %}"
            + "{% if ($ebs) %} ebs {$ebs}{% endif %}"
            + "{% if ($weight) %} scheduler-weight {$weight}{% endif %}"
            + "{% if ($congestion) %} congestion-avoidance-profile {$congestion}{% endif %}\n";

    private static final String DELETE_SERVICE_PROFILE =
            "traffic-services queuing egress-port-queue-group unset queue {$queue} port {$port} "
            + "congestion-avoidance-profile\n";

    private Cli cli;

    public ServiceSchedulerPolicyWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<SchedulerPolicy> iid,
                                                 @NotNull SchedulerPolicy data,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        Type type = data.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(Type.QueueGroupPolicy)) {
            String policyName = iid.firstKeyOf(SchedulerPolicy.class).getName();
            blockingWriteAndRead(cli, iid, data, writeTemplate(data, policyName));

            return true;
        }

        return false;
    }

    @VisibleForTesting
    String writeTemplate(SchedulerPolicy data, String policyName) {
        List<Scheduler> schedulers = data.getSchedulers().getScheduler();
        StringBuilder commands = new StringBuilder();

        for (Scheduler scheduler : schedulers) {
            Config threeColorConfig = scheduler.getTwoRateThreeColor() != null
                    ? scheduler.getTwoRateThreeColor().getConfig() : null;
            SaosQos2r3cAug qos2r3cAug = threeColorConfig != null
                    ? threeColorConfig.getAugmentation(SaosQos2r3cAug.class) : null;

            commands.append(fT(WRITE_SERVICE, "data", data,
                    "queue", scheduler.getSequence(),
                    "port", policyName,
                    "eir", threeColorConfig != null ? threeColorConfig.getPir() : null,
                    "ebs", threeColorConfig != null ? threeColorConfig.getBe() : null,
                    "weight", qos2r3cAug != null ? qos2r3cAug.getWeight() : null,
                    "congestion", qos2r3cAug != null ? qos2r3cAug.getCongestionAvoidance() : null));
        }

        return commands.toString();
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<SchedulerPolicy> iid,
                                                  @NotNull SchedulerPolicy dataBefore,
                                                  @NotNull SchedulerPolicy dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        Type type = dataAfter.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(Type.QueueGroupPolicy)) {
            String policyName = iid.firstKeyOf(SchedulerPolicy.class).getName();
            blockingWriteAndRead(cli, iid, dataAfter, updateTemplate(policyName, dataBefore, dataAfter));

            return true;
        }

        return false;
    }

    @VisibleForTesting
    String updateTemplate(String policyName, SchedulerPolicy before, SchedulerPolicy after) {
        List<Scheduler> schedulersBefore = before.getSchedulers().getScheduler();
        List<Scheduler> schedulersAfter = after.getSchedulers().getScheduler();
        StringBuilder commands = new StringBuilder();

        for (Scheduler schedulerAfter : schedulersAfter) {
            String queueAfter = schedulerAfter.getSequence().toString();
            Config threeColorConfig = schedulerAfter.getTwoRateThreeColor() != null
                    ? schedulerAfter.getTwoRateThreeColor().getConfig() : null;
            SaosQos2r3cAug qos2r3cAug = threeColorConfig != null
                    ? threeColorConfig.getAugmentation(SaosQos2r3cAug.class) : null;
            Scheduler schedulerBefore = schedulersBefore.stream()
                    .filter(scheduler -> scheduler.getSequence().toString().equals(queueAfter))
                    .findFirst().orElse(null);

            if (!schedulerAfter.equals(schedulerBefore)) {
                commands.append(fT(WRITE_SERVICE, "data", after, "before", after,
                        "queue", queueAfter,
                        "port", policyName,
                        "eir", threeColorConfig != null ? threeColorConfig.getPir() : null,
                        "ebs", threeColorConfig != null ? threeColorConfig.getBe() : null,
                        "weight", qos2r3cAug != null ? qos2r3cAug.getWeight() : null,
                        "congestion", qos2r3cAug != null ? qos2r3cAug.getCongestionAvoidance() : null));
            }
        }

        return commands.toString();
    }


    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<SchedulerPolicy> iid,
                                                  @NotNull SchedulerPolicy dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        Type type = dataBefore.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(Type.QueueGroupPolicy)) {
            String policyName = iid.firstKeyOf(SchedulerPolicy.class).getName();
            blockingDeleteAndRead(cli, iid, deleteTemplate(policyName, dataBefore));

            return true;
        }

        return false;
    }

    @VisibleForTesting
    String deleteTemplate(String policyName, SchedulerPolicy config) {
        List<String> queues = config.getSchedulers().getScheduler().stream()
                .filter(scheduler -> (scheduler.getTwoRateThreeColor().getConfig()
                        .getAugmentation(SaosQos2r3cAug.class).getCongestionAvoidance() != null))
                .map(Scheduler::getSequence)
                .map(Object::toString)
                .collect(Collectors.toList());

        StringBuilder commands = new StringBuilder();

        for (String queue : queues) {
            String cmd = fT(DELETE_SERVICE_PROFILE, "data", config,
                    "queue", queue, "port", policyName);
            commands.append(cmd);
        }

        return commands.toString();
    }
}