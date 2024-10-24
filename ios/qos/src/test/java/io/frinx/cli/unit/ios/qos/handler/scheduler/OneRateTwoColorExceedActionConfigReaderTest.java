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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Dei;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder;

class OneRateTwoColorExceedActionConfigReaderTest {

    private static final String OUTPUT = """
              Policy Map TEST
                Class DROP
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action drop\s
                Class TRANSMIT
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action transmit\s
                Class COS
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action set-cos-transmit 2
                Class DEI
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action set-dei-transmit 1
                Class DSCP
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action set-dscp-transmit cs7
                Class QOS
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action set-qos-transmit 54
            """;

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    void testDrop() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("DROP", OUTPUT, configBuilder);
        assertNull(configBuilder.getAugmentation(QosExceedActionAug.class));
        assertEquals(true, configBuilder.isDrop());
    }

    @Test
    void testTransmit() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("TRANSMIT", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        assertEquals(true, aug.isTransmit());
    }

    @Test
    void testCos() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("COS", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        assertEquals(Cos.getDefaultInstance("2"), aug.getCosTransmit());
    }

    @Test
    void testDei() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("DEI", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        assertEquals(Dei.getDefaultInstance("1"), aug.getDeiTransmit());
    }

    @Test
    void testDscp() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("DSCP", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        assertEquals(DscpBuilder.getDefaultInstance("cs7"), aug.getDscpTransmit());
    }

    @Test
    void testQosGroup() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("QOS", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        assertEquals(QosGroupBuilder.getDefaultInstance("54"), aug.getQosTransmit());
    }

}