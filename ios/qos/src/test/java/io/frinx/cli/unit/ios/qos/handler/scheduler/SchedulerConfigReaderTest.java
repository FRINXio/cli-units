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

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosServicePolicyAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.QosSchedulerConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;

public class SchedulerConfigReaderTest {

    private static final String OUTPUT = "  Policy Map plmap\n"
            + "    Class cmap1\n"
            + "      priority\n"
            + "     police cir 2000000 bc 62500\n"
            + "       conform-action transmit \n"
            + "       exceed-action drop \n"
            + "    Class cmap2\n"
            + "      bandwidth percent 3\n"
            + "      service-policy TEST\n"
            + "    Class cmap3\n"
            + "      bandwidth percent 30\n";

    @Test
    public void testPriority() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        SchedulerConfigReader.fillInConfig("cmap1", OUTPUT, configBuilder);
        Assert.assertEquals(QosSchedulerConfig.Priority.STRICT, configBuilder.getPriority());

        configBuilder = new ConfigBuilder();
        SchedulerConfigReader.fillInConfig("cmap2", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getPriority());
    }

    @Test
    public void testServicePolicy() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        SchedulerConfigReader.fillInConfig("cmap2", OUTPUT, configBuilder);
        QosServicePolicyAug aug = configBuilder.getAugmentation(QosServicePolicyAug.class);
        Assert.assertEquals("TEST", aug.getServicePolicy());

        configBuilder = new ConfigBuilder();
        SchedulerConfigReader.fillInConfig("cmap3", OUTPUT, configBuilder);
        aug = configBuilder.getAugmentation(QosServicePolicyAug.class);
        Assert.assertNull(aug);
    }

}