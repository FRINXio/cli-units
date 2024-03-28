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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAug;

class ServiceThreeColorConfigReaderTest {

    private static final String OUTPUT = """
            traffic-profiling set meter-provisioning eir
            traffic-profiling set port 1 mode advanced
            traffic-profiling set port 2 mode advanced
            traffic-profiling set port 6 mode hierarchical-vlan
            traffic-profiling standard-profile create port 1 profile 1 name CIA_CoS0 cir 50048 eir 0 cbs 8 ebs 0
            traffic-profiling standard-profile set port 1 profile CIA_CoS0 green-remark-rcos 0 yellow-remark-rcos 0
            traffic-profiling standard-profile create port 1 profile 2 name V40 cir 10048 ebs 0 vs VLAN111222
            traffic-profiling standard-profile create port 4 profile 1 name Prof_1 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling standard-profile create port 5 profile 1 name Test1 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling standard-profile create port 5 profile 2 name Test2 cir 10048 eir 0 cbs 128 ebs 0
            traffic-services queuing egress-port-queue-group set queue 0 port 4 eir 64 ebs 5 scheduler-weight 6 \
            congestion-avoidance-profile Q0-BE
            traffic-services queuing egress-port-queue-group set queue 1 port 4 eir 128 ebs 4 scheduler-weight 3 \
            congestion-avoidance-profile Q1-BE
            traffic-services queuing egress-port-queue-group set queue 2 port 4 eir 254 ebs 8 scheduler-weight 9
            traffic-services queuing egress-port-queue-group set queue 3 port 4 eir 512 ebs 12 scheduler-weight 2
            traffic-profiling enable port 2
            traffic-profiling enable port 6
            traffic-profiling enable port 8
            traffic-profiling enable
            """;

    @Test
    void parseTwoRateThreeColorConfigTest() {
        ServiceThreeColorConfigReader reader = new ServiceThreeColorConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder configBuilder = new ConfigBuilder();
        reader.parseThreeColorConfig(OUTPUT, configBuilder,"0", "4");
        assertEquals("64", configBuilder.getPir().toString());
        assertEquals("5", configBuilder.getBe().toString());
        assertEquals("6", configBuilder.getAugmentation(SaosQos2r3cAug.class).getWeight().toString());
        assertEquals("Q0-BE",
                configBuilder.getAugmentation(SaosQos2r3cAug.class).getCongestionAvoidance());
    }
}
