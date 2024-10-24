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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerTwoColorConfig.TrafficAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.TwoRateThreeColor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.TwoRateThreeColorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.SchedulerPolicies;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;


class TwoRateThreeColorWriterTest {

    private static final String WRITE_INPUT = """
            system-view
            traffic behavior VOICE
            queue llq bandwidth 2100 cbs 52500
            return
            """;

    private static final String UPDATE_INPUT = """
            system-view
            traffic behavior VOICE
            queue af bandwidth pct 15
            return
            """;

    private static final String DELETE_INPUT = """
            system-view
            traffic behavior VOICE
            undo queue llq
            return
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private TwoRateThreeColorWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(SchedulerPolicies.class)
            .child(SchedulerPolicy.class, new SchedulerPolicyKey("VOICE"))
            .child(Schedulers.class)
            .child(Scheduler.class, new SchedulerKey(0L))
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                    .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config.class);

    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top
            .scheduler.policies.scheduler.policy.schedulers.scheduler.Config inputs;
    private TwoRateThreeColor data;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new TwoRateThreeColorWriter(cli);
        inputs = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top
                .scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder()
                .addAugmentation(VrpQosSchedulerConfAug.class, new VrpQosSchedulerConfAugBuilder()
                        .setBehavior("VOICE")
                        .build())
                .build();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
        initializeData();
    }

    private void initializeData() {
        data = new TwoRateThreeColorBuilder()
                .setConfig(new ConfigBuilder()
//                        .setCirPct(new Percentage((short) 30))
                        .setCir(new BigInteger("2100"))
                        .setBc(52500L)
                        .addAugmentation(QosTwoColorConfig.class, new QosTwoColorConfigBuilder()
                                .setTrafficAction(TrafficAction.Llq)
                                .build())
                        .build())
                .build();
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        TwoRateThreeColor dataAfter = new TwoRateThreeColorBuilder()
                .setConfig(new ConfigBuilder()
                        .setCirPct(new Percentage((short) 15))
                        .addAugmentation(QosTwoColorConfig.class,
                                new QosTwoColorConfigBuilder()
                                        .setTrafficAction(TrafficAction.Af)
                                        .build())
                        .build())
                .build();

        writer.updateCurrentAttributes(piid, data, dataAfter, context);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getAllValues().get(1).getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

}
