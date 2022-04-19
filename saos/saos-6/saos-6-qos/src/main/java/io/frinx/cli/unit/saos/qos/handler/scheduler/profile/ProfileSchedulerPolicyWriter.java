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

package io.frinx.cli.unit.saos.qos.handler.scheduler.profile;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProfileSchedulerPolicyWriter
        implements CompositeWriter.Child<SchedulerPolicy>, CliWriter<SchedulerPolicy> {

    private static final String WRITE_PROFILE = "traffic-profiling standard-profile create port {$ifcId} name "
            + "{$data.name} vs {$vs} cir {$cir}";

    private static final String UPDATE_PROFILE = "{% if ($cir) %}traffic-profiling standard-profile set port "
            + "{$ifcId} profile {$data.name} cir {$cir}{% endif %}";

    private static final String DELETE_PROFILE = "traffic-profiling standard-profile delete port %s profile %s";

    private Cli cli;

    public ProfileSchedulerPolicyWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<SchedulerPolicy> iid,
                                                 @Nonnull SchedulerPolicy data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        SaosQosSchedulerConfig.Type type = data.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(SaosQosSchedulerConfig.Type.PortPolicy)) {
            String policyName = iid.firstKeyOf(SchedulerPolicy.class).getName();
            blockingWriteAndRead(cli, iid, data, writeTemplate(data, policyName));

            return true;
        }

        return false;
    }

    @VisibleForTesting
    String writeTemplate(SchedulerPolicy data, String policyName) {

        String ifcId = data.getConfig().getAugmentation(SaosQosScPolicyIfcId.class).getInterfaceId();
        Scheduler scheduler = data.getSchedulers().getScheduler().get(0);

        return fT(WRITE_PROFILE, "data", data,
                "ifcId", ifcId,
                "cir", scheduler.getTwoRateThreeColor().getConfig().getCir(),
                "vs", scheduler.getConfig().getAugmentation(SaosQosSchedulerAug.class).getVsName());
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<SchedulerPolicy> iid,
                                                  @Nonnull SchedulerPolicy dataBefore,
                                                  @Nonnull SchedulerPolicy dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        SaosQosSchedulerConfig.Type type = dataAfter.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(SaosQosSchedulerConfig.Type.PortPolicy)) {
            String policyName = iid.firstKeyOf(SchedulerPolicy.class).getName();
            blockingWriteAndRead(cli, iid, dataAfter, updateTemplate(policyName, dataBefore, dataAfter));

            return true;
        }
        return false;
    }

    @VisibleForTesting
    String updateTemplate(String policyName, SchedulerPolicy before, SchedulerPolicy after) {
        BigInteger beforeCir = before.getSchedulers().getScheduler().get(0)
                    .getTwoRateThreeColor().getConfig().getCir();
        BigInteger afterCir = after.getSchedulers().getScheduler().get(0)
                    .getTwoRateThreeColor().getConfig().getCir();

        return fT(UPDATE_PROFILE, "data", after, "before", before,
                "ifcId", after.getConfig().getAugmentation(SaosQosScPolicyIfcId.class).getInterfaceId(),
                "cir", !(beforeCir.equals(afterCir)) ? afterCir : null);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<SchedulerPolicy> iid,
                                                  @Nonnull SchedulerPolicy dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        SaosQosSchedulerConfig.Type type = dataBefore.getSchedulers().getScheduler().get(0).getConfig()
                .getAugmentation(SaosQosSchedulerAug.class).getType();

        if (type.equals(SaosQosSchedulerConfig.Type.PortPolicy)) {
            String policyName = iid.firstKeyOf(SchedulerPolicy.class).getName();
            blockingDeleteAndRead(cli, iid, deleteTemplate(policyName, dataBefore));

            return true;
        }
        return false;
    }

    @VisibleForTesting
    String deleteTemplate(String policyName, SchedulerPolicy config) {
        String ifcId = config.getConfig().getAugmentation(SaosQosScPolicyIfcId.class)
                .getInterfaceId();
        return f(DELETE_PROFILE, ifcId, config.getName());
    }
}