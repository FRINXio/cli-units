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

import io.frinx.cli.io.Cli;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.TwoRateThreeColorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.SchedulersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcIdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;

public class ServiceSchedulerPolicyWriterTest {

    private static final String SERVICES =
        "traffic-services queuing egress-port-queue-group set queue 0 port 1 eir 1212 scheduler-weight 495\n"
        + "traffic-services queuing egress-port-queue-group set queue 1 port 1 eir 1214 scheduler-weight 501\n"
        + "traffic-services queuing egress-port-queue-group set queue 2 port 1 eir 1216 scheduler-weight 507\n"
        + "traffic-services queuing egress-port-queue-group set queue 3 port 1 eir 1218 scheduler-weight 513\n"
        + "configuration save";

    private static final String SERVICE_WITH_EBS =
        "traffic-services queuing egress-port-queue-group set queue 0 port 1 eir 1458 ebs 35 scheduler-weight 478\n"
        + "traffic-services queuing egress-port-queue-group set queue 1 port 1 eir 1460 ebs 39 scheduler-weight 484\n"
        + "traffic-services queuing egress-port-queue-group set queue 2 port 1 eir 1462 ebs 43 scheduler-weight 490\n"
        + "traffic-services queuing egress-port-queue-group set queue 3 port 1 eir 1464 ebs 47 scheduler-weight 496\n"
        + "configuration save";

    private static final String SERVICE_WITH_PROFILE =
        "traffic-services queuing egress-port-queue-group set queue 0 port 1 eir 1458 ebs 35 scheduler-weight 478 "
        + "congestion-avoidance-profile Default-0\n"
        + "traffic-services queuing egress-port-queue-group set queue 1 port 1 eir 1460 ebs 39 scheduler-weight 484 "
        + "congestion-avoidance-profile Default-1\n"
        + "traffic-services queuing egress-port-queue-group set queue 2 port 1 eir 1462 ebs 43 scheduler-weight 490 "
        + "congestion-avoidance-profile Default-2\n"
        + "traffic-services queuing egress-port-queue-group set queue 3 port 1 eir 1464 ebs 47 scheduler-weight 496 "
        + "congestion-avoidance-profile Default-3\n"
        + "configuration save";

    private static final String SERVICE_DELETE_PROFILE =
        "traffic-services queuing egress-port-queue-group unset queue 0 port 1 congestion-avoidance-profile\n"
        + "traffic-services queuing egress-port-queue-group unset queue 2 port 1 congestion-avoidance-profile\n"
        + "configuration save";

    private ServiceSchedulerPolicyWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new ServiceSchedulerPolicyWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals(SERVICES,
            writer.writeTemplate(createConfig("2", null,
                    createSchedulers(null, "1212", "495", null, false)), "1"));

        Assert.assertEquals(SERVICE_WITH_EBS,
            writer.writeTemplate(createConfig("1", null,
                    createSchedulers("35", "1458", "478", null, false)), "1"));

        Assert.assertEquals(SERVICE_WITH_PROFILE,
            writer.writeTemplate(createConfig("1", null,
                    createSchedulers("35", "1458", "478", "Default-", false)), "1"));
    }

    @Test
    public void updateTemplateTest() {
        Assert.assertEquals(SERVICE_WITH_PROFILE,
            writer.updateTemplate("1",
                    createConfig("1", null,
                            createSchedulers("29", "4", "10", "Qts", false)),
                    createConfig("1", null,
                            createSchedulers("35", "1458", "478", "Default-", false))
            )
        );
    }

    @Test
    public void deleteTemplateTest() {
        Assert.assertEquals(SERVICE_DELETE_PROFILE,
            writer.deleteTemplate("1",
                    createConfig("1", null,
                            createSchedulers("35", "1458", "478", "Default-", true))));
    }

    private SchedulerPolicy createConfig(String policyName, String ifcId, List<Scheduler> schedulerList) {
        return new SchedulerPolicyBuilder()
            .setName(policyName)
            .setConfig(new ConfigBuilder()
                .setName(policyName)
                .addAugmentation(SaosQosScPolicyIfcId.class, new SaosQosScPolicyIfcIdBuilder()
                    .setInterfaceId(ifcId)
                    .build())
                .build())
            .setSchedulers(new SchedulersBuilder()
                .setScheduler(schedulerList)
                .build())
            .build();
    }

    private List<Scheduler> createSchedulers(String be, String pir, String weight, String con, boolean delVersion) {
        List<Scheduler> schedulerList = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            schedulerList.add(new SchedulerBuilder()
                .setSequence((long) i)
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                    .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder()
                    .addAugmentation(SaosQosSchedulerAug.class, new SaosQosSchedulerAugBuilder()
                        .setType(Type.QueueGroupPolicy).build())
                    .build())
                .setTwoRateThreeColor(new TwoRateThreeColorBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                        .qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder()
                        .setBe(be != null ? (long) ((i * 4) + Integer.parseInt(be)) : null)
                        .setPir(BigInteger.valueOf((i * 2) + Integer.parseInt(pir)))
                        .addAugmentation(SaosQos2r3cAug.class, new SaosQos2r3cAugBuilder()
                            .setWeight(weight != null ? (long)(i * 6) + Integer.parseInt(weight) : null)
                            .setCongestionAvoidance(delVersion ? ((i % 2 == 0) ? (con + i) : null) :
                                (con != null) ? con + i : null)
                            .build())
                        .build())
                    .build())
                .build());
        }
        return schedulerList;
    }
}
