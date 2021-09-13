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

import com.google.common.base.Optional;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerColorConfig.ColorMode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerColorAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerColorAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpYellowAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpYellowActionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.yellow.action.aug.YellowActionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConformActionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ExceedActionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.SchedulerPolicies;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;


public class OneRateTwoColorWriterTest {

    private static final String WRITE_INPUT_COLOR = "system-view\n"
            + "traffic behavior VOICE\n"
            + "car cir pct 30 mode color-blind green pass remark-8021p 3"
            + " yellow pass remark-8021p 2 red pass remark-8021p 2\n"
            + "return\n";

    private static final String UPDATE_INPUT = "system-view\n"
            + "traffic behavior VOICE\n"
            + "car cir pct 15 mode color-blind green pass remark-8021p 2"
            + " red pass remark-8021p 2\n"
            + "return\n";

    private static final String DELETE_INPUT = "system-view\n"
            + "traffic behavior VOICE\n"
            + "undo car cir\n"
            + "return\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private OneRateTwoColorWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(SchedulerPolicies.class)
            .child(SchedulerPolicy.class, new SchedulerPolicyKey("VOICE"))
            .child(Schedulers.class)
            .child(Scheduler.class, new SchedulerKey(0L))
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                    .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config.class);

    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
            .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config inputs;
    private OneRateTwoColor data;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new OneRateTwoColorWriter(cli);
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
        data = new OneRateTwoColorBuilder()
            .setConfig(new ConfigBuilder()
//                        .setCirPctRemaining(new Percentage((short) 50))
                    .setCirPct(new Percentage((short) 30))
//                        .setCir(new BigInteger("2100"))
//                        .setBc(52500L)
                    .addAugmentation(VrpQosSchedulerColorAug.class,
                            new VrpQosSchedulerColorAugBuilder()
//                                        .setTrafficAction(TrafficAction.Af)
                                    .setColorMode(ColorMode.ColorBlind)
                                    .build())
                    .build())
            .setConformAction(new ConformActionBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                            .qos.scheduler._1r2c.top.one.rate.two.color.conform.action.ConfigBuilder()
                            .addAugmentation(QosConformActionAug.class, new QosConformActionAugBuilder()
                                    .setTransmit(true)
                                    .setCosTransmit(Cos.getDefaultInstance("3"))
                                    .build())
                            .build())
                    .build())
            .setExceedAction(new ExceedActionBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                            .qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder()
                            .addAugmentation(QosExceedActionAug.class, new QosExceedActionAugBuilder()
                                    .setCosTransmit(Cos.getDefaultInstance("2"))
                                    .setTransmit(true)
                                    .build())
                            .build())
                    .build())
            .addAugmentation(VrpYellowAction.class, new VrpYellowActionBuilder()
                    .setYellowAction(new YellowActionBuilder().setConfig(
                            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304
                                .yellow.action.aug.yellow.action.ConfigBuilder()
                                .setTransmit(true)
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
        Assert.assertEquals(WRITE_INPUT_COLOR, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        data = new OneRateTwoColorBuilder()
            .setConfig(new ConfigBuilder()
                    .setCirPct(new Percentage((short) 15))
                    .addAugmentation(VrpQosSchedulerColorAug.class,
                            new VrpQosSchedulerColorAugBuilder()
                                    .setColorMode(ColorMode.ColorBlind)
                                    .build())
                    .build())
            .setConformAction(new ConformActionBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                            .qos.scheduler._1r2c.top.one.rate.two.color.conform.action.ConfigBuilder()
                            .addAugmentation(QosConformActionAug.class, new QosConformActionAugBuilder()
                                    .setCosTransmit(Cos.getDefaultInstance("2"))
                                    .setTransmit(true)
                                    .build())
                            .build())
                    .build())
            .setExceedAction(new ExceedActionBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216
                            .qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder()
                            .addAugmentation(QosExceedActionAug.class, new QosExceedActionAugBuilder()
                                    .setCosTransmit(Cos.getDefaultInstance("2"))
                                    .setTransmit(true)
                                    .build())
                            .build())
                    .build())
            .build();

        writer.updateCurrentAttributes(piid, data, data, context);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getAllValues().get(1).getContent());
    }


    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
