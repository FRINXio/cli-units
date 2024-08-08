/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.huawei.qos.handler.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.behavior.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.behavior.ConfigBuilder;

class BehaviorConfigWriterTest {

    private static final String WRITE_INPUT = """
            system-view
            traffic behavior test
            statistic enable
            remark 8021p inner-8021p
            car cir 50000 green pass red discard
            return""";

    private static final String DELETE_INPUT = """
            system-view
            undo traffic behavior test
            return""";

    private BehaviorConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new BehaviorConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void testWriteBehavior() {
        assertEquals(
               WRITE_INPUT,
               writer.writeTemplate("test", createConfig("8021p inner-8021p", "enable", "50000", "pass", "discard")));
    }

    @Test
    void testDeleteBehavior() {
        assertEquals(
                DELETE_INPUT,
                writer.deleteTemplate("test"));
    }

    private Config createConfig(String remark, String statistic, String cir, String greenAction, String redAction) {
        return new ConfigBuilder().setRemark(remark)
                .setStatistic(statistic)
                .setCir(cir)
                .setGreenAction(greenAction)
                .setRedAction(redAction)
                .build();
    }
}
