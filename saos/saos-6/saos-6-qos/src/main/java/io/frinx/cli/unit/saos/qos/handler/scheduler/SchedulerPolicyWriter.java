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
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerPolicyWriter implements CliWriter<SchedulerPolicy> {

    private static final String WRITE_TEMPLATE =
            "traffic-profiling standard-profile create port {$ifcId} name {$data.name} "
            + "{% if ($vs) %}vs {$vs} cir {$cir}"
            + "{% else %}cir {$cir}{% endif %}\n";

    private static final String UPDATE_TEMPLATE =
            "{% if ($cir) %}traffic-profiling standard-profile set port "
            + "{$ifcId} profile {$data.name} cir {$cir}\n{% endif %}";

    private static final String DELETE_TEMPLATE =
            "traffic-profiling standard-profile delete port %s profile %s\n";

    private Cli cli;

    public SchedulerPolicyWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                       @Nonnull SchedulerPolicy schedulerPolicy,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli,instanceIdentifier, schedulerPolicy, writeTemplate(schedulerPolicy));
    }

    @VisibleForTesting
    String writeTemplate(SchedulerPolicy config) {
        String ifcId = config.getConfig().getAugmentation(SaosQosScPolicyIfcId.class).getInterfaceId();
        Scheduler scheduler = config.getSchedulers().getScheduler().get(0);
        BigInteger cir = scheduler.getOneRateTwoColor().getConfig().getCir();

        return fT(WRITE_TEMPLATE, "data", config,
                "ifcId", ifcId,
                "cir", cir,
                "vs", getVs(scheduler));
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
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
    }

    @VisibleForTesting
    String updateTemplate(SchedulerPolicy before, SchedulerPolicy after) {
        return fT(UPDATE_TEMPLATE, "data", after, "before", before,
                "ifcId", after.getConfig().getAugmentation(SaosQosScPolicyIfcId.class).getInterfaceId(),
                "cir", setCir(before, after));
    }

    private String setCir(SchedulerPolicy before, SchedulerPolicy after) {
        BigInteger beforeCir = before.getSchedulers().getScheduler().get(0)
                .getOneRateTwoColor().getConfig().getCir();
        BigInteger afterCir = after.getSchedulers().getScheduler().get(0)
                .getOneRateTwoColor().getConfig().getCir();

        return (!beforeCir.equals(afterCir)) ? afterCir.toString() : null;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                        @Nonnull SchedulerPolicy schedulerPolicy,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(schedulerPolicy));
    }

    @VisibleForTesting
    String deleteTemplate(SchedulerPolicy config) {
        String ifcId = config.getConfig().getAugmentation(SaosQosScPolicyIfcId.class)
                .getInterfaceId();

        return f(DELETE_TEMPLATE, ifcId, config.getName());
    }
}