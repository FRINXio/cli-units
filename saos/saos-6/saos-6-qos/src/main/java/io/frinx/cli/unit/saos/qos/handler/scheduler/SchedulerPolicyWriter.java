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

package io.frinx.cli.unit.saos.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerPolicyWriter implements CliWriter<SchedulerPolicy> {

    private static final String WRITE_PROFILE =
            "traffic-profiling standard-profile create port {$ifcId} name {$data.name} vs {$vs} cir {$cir}\n";

    private static final String UPDATE_PROFILE =
            "{% if ($cir) %}traffic-profiling standard-profile set port "
            + "{$ifcId} profile {$data.name} cir {$cir}{% endif %}\n";

    private static final String DELETE_PROFILE =
            "traffic-profiling standard-profile delete port %s profile %s\n";

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

    public SchedulerPolicyWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                       @Nonnull SchedulerPolicy schedulerPolicy,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, schedulerPolicy, writeTemplate(schedulerPolicy, policyName));
    }

    @VisibleForTesting
    String writeTemplate(SchedulerPolicy config, String policyName) {
        Type type = config.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(Type.PortPolicy)) {
            String ifcId = config.getConfig().getAugmentation(SaosQosScPolicyIfcId.class).getInterfaceId();
            Scheduler scheduler = config.getSchedulers().getScheduler().get(0);

            return fT(WRITE_PROFILE, "data", config,
                    "ifcId", ifcId,
                    "cir", scheduler.getTwoRateThreeColor().getConfig().getCir(),
                    "vs", getVs(scheduler));
        }

        List<Scheduler> schedulers = config.getSchedulers().getScheduler();
        StringBuilder commands = new StringBuilder();

        for (Scheduler scheduler : schedulers) {
            Config threeColorConfig = scheduler.getTwoRateThreeColor() != null
                    ? scheduler.getTwoRateThreeColor().getConfig() : null;
            SaosQos2r3cAug qos2r3cAug = threeColorConfig != null
                    ? threeColorConfig.getAugmentation(SaosQos2r3cAug.class) : null;

            commands.append(fT(WRITE_SERVICE, "data", config,
                    "queue", scheduler.getSequence(),
                    "port", policyName,
                    "eir", threeColorConfig != null ? threeColorConfig.getPir() : null,
                    "ebs", threeColorConfig != null ? threeColorConfig.getBe() : null,
                    "weight", qos2r3cAug != null ? qos2r3cAug.getWeight() : null,
                    "congestion", qos2r3cAug != null ? qos2r3cAug.getCongestionAvoidance() : null));
        }

        return commands.toString();
    }

    private String getVs(Scheduler scheduler) {
        SaosQosSchedulerAug schedulerAug = scheduler.getConfig()
                .getAugmentation(SaosQosSchedulerAug.class);

        return Optional.ofNullable(schedulerAug.getVsName()).orElse(null);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> id,
                                        @Nonnull SchedulerPolicy dataBefore,
                                        @Nonnull SchedulerPolicy dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = id.firstKeyOf(SchedulerPolicy.class).getName();
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(policyName, dataBefore, dataAfter));
    }

    @VisibleForTesting
    String updateTemplate(String policyName, SchedulerPolicy before, SchedulerPolicy after) {
        Type type = after.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(Type.PortPolicy)) {
            BigInteger beforeCir = before.getSchedulers().getScheduler().get(0)
                    .getTwoRateThreeColor().getConfig().getCir();
            BigInteger afterCir = after.getSchedulers().getScheduler().get(0)
                    .getTwoRateThreeColor().getConfig().getCir();

            return fT(UPDATE_PROFILE, "data", after, "before", before,
                    "ifcId", after.getConfig().getAugmentation(SaosQosScPolicyIfcId.class).getInterfaceId(),
                    "cir", !(beforeCir.equals(afterCir)) ? afterCir : null);
        }

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
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                        @Nonnull SchedulerPolicy schedulerPolicy,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(policyName, schedulerPolicy));
    }

    @VisibleForTesting
    String deleteTemplate(String policyName, SchedulerPolicy config) {
        Type type = config.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(Type.PortPolicy)) {
            String ifcId = config.getConfig().getAugmentation(SaosQosScPolicyIfcId.class)
                    .getInterfaceId();
            return f(DELETE_PROFILE, ifcId, config.getName());
        }

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