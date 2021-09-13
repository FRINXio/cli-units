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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.SchedulerPolicies;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Schedulers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class InputConfigWriterTest {

    private static final String WRITE_INPUT = "system-view\n"
            + "traffic behavior VOICE\n"
            + "statistic enable\n"
            + "remark 8021p 0\n"
            + "return\n";

    private static final String UPDATE_INPUT = "system-view\n"
            + "traffic behavior VOICE\n"
            + "statistic enable\n"
            + "remark 8021p 0\n"
            + "return\n";

    private static final String DELETE_INPUT = "system-view\n"
            + "traffic behavior VOICE\n"
            + "undo statistic\n"
            + "undo remark 8021p\n"
            + "return\n";

    private static final String UPDATE_INPUT_DELETE_COS = "system-view\n"
            + "traffic behavior VOICE\n"
            + "statistic enable\n"
            + "return\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InputConfigWriter writer;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(SchedulerPolicies.class)
            .child(SchedulerPolicy.class, new SchedulerPolicyKey("VOICE"))
            .child(Schedulers.class)
            .child(Scheduler.class, new SchedulerKey(0L))
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                    .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config.class);

    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
            .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.Config inputs;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InputConfigWriter(cli);
        inputs = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top
                .scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder()
                .addAugmentation(VrpQosSchedulerConfAug.class, new VrpQosSchedulerConfAugBuilder()
                        .setBehavior("VOICE")
                        .setVrpPrecedence("5")
                        .build())
                .build();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(inputs));
    }

    @Test
    public void write() throws WriteFailedException {
        Config data = getConfig("enable", "0");
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateDelete() throws WriteFailedException {
        Config data = getConfig("enable", "0");
        writer.updateCurrentAttributes(piid, data, data, context);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getAllValues().get(1).getContent());
        Assert.assertEquals(DELETE_INPUT, response.getAllValues().get(0).getContent());
    }

    @Test
    public void updateDeleteCos() throws WriteFailedException {
        Config dataBefore = getConfig("enable", "0");
        Config dataAfter = getConfig("enable", null);
        writer.updateCurrentAttributes(piid, dataBefore, dataAfter, context);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getAllValues().get(0).getContent());
        Assert.assertEquals(UPDATE_INPUT_DELETE_COS,  response.getAllValues().get(1).getContent());
    }


    private Config getConfig(String statistic, String cos) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (cos != null) {
            configBuilder.addAugmentation(QosCosAug.class,
                    new QosCosAugBuilder().setCos(Cos.getDefaultInstance(cos)).build());
        }
        configBuilder.addAugmentation(VrpQosSchedulerInputAug.class,
                new VrpQosSchedulerInputAugBuilder().setStatistic(statistic).build());
        return configBuilder.build();
    }
}
