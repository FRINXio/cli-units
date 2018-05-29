/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.qos.handler.scheduler;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.Inputs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.InputsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.InputBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.SchedulerPolicies;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class OneRateTwoColorConfigWriterTest {


    private static final String WRITE_INPUT = "policy-map plmap\n" +
            "class map1\n" +
            "bandwidth percent 30\n" +
            "bandwidth remaining percent 50\n" +
            "police rate percent 40\n" +
            "queue-limit 1000 ms\n" +
            "root\n";

    private static final String UPDATE_INPUT = "policy-map plmap\n" +
            "class map1\n" +
            "bandwidth percent 35\n" +
            "bandwidth remaining percent 55\n" +
            "police rate percent 45\n" +
            "queue-limit 100 ms\n" +
            "root\n";

    private static final String DELETE_INPUT = "policy-map plmap\n" +
            "class map1\n" +
            "no bandwidth\n" +
            "no bandwidth remaining\n" +
            "no police\n" +
            "no queue-limit\n" +
            "root\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private OneRateTwoColorConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(SchedulerPolicies.class)
            .child(SchedulerPolicy.class, new SchedulerPolicyKey("plmap")).child(Schedulers.class)
            .child(Scheduler.class, new SchedulerKey(0L)).child(Inputs.class);

    // test data
    private Inputs inputs;
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new OneRateTwoColorConfigWriter(this.cli);

        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder()
            .setCirPct(new Percentage((short)30))
            .setMaxQueueDepthPercent(new Percentage((short) 40))
            .setCirPctRemaining(new Percentage((short) 50))
            .addAugmentation(QosMaxQueueDepthMsAug.class,
                new QosMaxQueueDepthMsAugBuilder().setMaxQueueDepthMs(1000L).build())
        .build();

        inputs = new InputsBuilder().setInput(Lists.newArrayList(new InputBuilder()
                .setId("map1").build())).build();

        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(piid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        data = new ConfigBuilder()
            .setCirPct(new Percentage((short)35))
            .setMaxQueueDepthPercent(new Percentage((short) 45))
            .setCirPctRemaining(new Percentage((short) 55))
            .addAugmentation(QosMaxQueueDepthMsAug.class,
                new QosMaxQueueDepthMsAugBuilder().setMaxQueueDepthMs(100L).build())
        .build();

        this.writer.updateCurrentAttributes(piid, data, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        data = new ConfigBuilder().build();

        this.writer.updateCurrentAttributes(piid, data, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(piid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
