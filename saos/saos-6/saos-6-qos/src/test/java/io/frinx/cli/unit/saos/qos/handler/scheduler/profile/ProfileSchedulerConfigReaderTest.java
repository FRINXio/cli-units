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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;

class ProfileSchedulerConfigReaderTest {

    private static final String OUTPUT = """
            traffic-profiling standard-profile create port 1 profile 1 name CIA_CoS0 cir 50048 eir 0 cbs 8 ebs 0
            traffic-profiling standard-profile set port 1 profile CIA_CoS0 green-remark-rcos 0 yellow-remark-rcos 0
            traffic-profiling standard-profile create port 1 profile 2 name V40 cir 10048 ebs 0 vs VLAN111222
            traffic-profiling standard-profile create port 4 profile 1 name Prof_1 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling standard-profile create port 5 profile 1 name Test1 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling standard-profile create port 5 profile 2 name Test2 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling enable port 2
            traffic-profiling enable port 6
            traffic-profiling enable port 8
            traffic-profiling enable
            """;

    @Test
    void parseSchedulerConfigTest() {
        ProfileSchedulerConfigReader reader = new ProfileSchedulerConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder configBuilder = new ConfigBuilder();

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "V40", 0L);
        assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        assertEquals("VLAN111222",
                configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "Prof_1", 0L);
        assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        assertNull(configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "Prof_1", 0L);
        assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        assertNull(configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "Test2", 0L);
        assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        assertNull(configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());
    }
}
