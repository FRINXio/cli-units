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
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Dei;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConformActionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ExceedActionBuilder;
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

public class OneRateTwoColorWriterTest {

    private static final String WRITE_INPUT = "configure terminal\n"
            + "policy-map plmap\n"
            + "class map1\n"
            + "bandwidth remaining percent 50\n"
            + "bandwidth percent 30\n"
            + "shape average 36000000\n"
            + "police cir 10000000 bc 5000000 conform-action transmit exceed-action set-cos-transmit 2\n"
            + "exit\n"
            + "end\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "policy-map plmap\n"
            + "class map1\n"
            + "bandwidth remaining percent 25\n"
            + "bandwidth percent 15\n"
            + "no shape average\n"
            + "police cir 5000000 bc 2500000 conform-action set-dot1ad-dei-transmit 0 "
            + "exceed-action set-dscp-transmit cs7\n"
            + "exit\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "policy-map plmap\n"
            + "class map1\n"
            + "no bandwidth remaining percent\n"
            + "no bandwidth percent\n"
            + "no shape average\n"
            + "no police 1m\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private OneRateTwoColorWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(SchedulerPolicies.class)
            .child(SchedulerPolicy.class, new SchedulerPolicyKey("plmap"))
            .child(Schedulers.class)
            .child(Scheduler.class, new SchedulerKey(0L))
            .child(Inputs.class);

    private Inputs inputs;
    private OneRateTwoColor data;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new OneRateTwoColorWriter(cli);
        inputs = new InputsBuilder().setInput(Lists.newArrayList(new InputBuilder()
                .setId("map1")
                .build()))
                .build();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
        initializeData();
    }

    private void initializeData() {
        data = new OneRateTwoColorBuilder()
                .setConfig(new ConfigBuilder()
                        .setCirPctRemaining(new Percentage((short) 50))
                        .setCirPct(new Percentage((short) 30))
                        .setCir(new BigInteger("10000000"))
                        .setBc(5000000L)
                        .addAugmentation(QosMaxQueueDepthBpsAug.class,
                                new QosMaxQueueDepthBpsAugBuilder().setMaxQueueDepthBps(36000000L).build())
                        .build())
                .setConformAction(new ConformActionBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                                .qos.scheduler._1r2c.top.one.rate.two.color.conform.action.ConfigBuilder()
                                .addAugmentation(QosConformActionAug.class, new QosConformActionAugBuilder()
                                        .setTransmit(true)
                                        .build())
                                .build())
                        .build())
                .setExceedAction(new ExceedActionBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                                .qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder()
                                .addAugmentation(QosExceedActionAug.class, new QosExceedActionAugBuilder()
                                        .setCosTransmit(Cos.getDefaultInstance("2"))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        data = new OneRateTwoColorBuilder()
                .setConfig(new ConfigBuilder()
                        .setCirPctRemaining(new Percentage((short) 25))
                        .setCirPct(new Percentage((short) 15))
                        .setCir(new BigInteger("5000000"))
                        .setBc(2500000L)
                        .build())
                .setConformAction(new ConformActionBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                                .qos.scheduler._1r2c.top.one.rate.two.color.conform.action.ConfigBuilder()
                                .addAugmentation(QosConformActionAug.class, new QosConformActionAugBuilder()
                                        .setDeiTransmit(Dei.getDefaultInstance("0"))
                                        .build())
                                .build())
                        .build())
                .setExceedAction(new ExceedActionBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                                .qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder()
                                .addAugmentation(QosExceedActionAug.class, new QosExceedActionAugBuilder()
                                        .setDscpTransmit(DscpBuilder.getDefaultInstance("cs7"))
                                        .build())
                                .build())
                        .build())
                .build();

        writer.updateCurrentAttributes(piid, data, data, context);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getAllValues().get(1).getContent());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        data = new OneRateTwoColorBuilder().build();

        writer.updateCurrentAttributes(piid, data, data, context);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getAllValues().get(0).getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

}