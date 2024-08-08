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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Dei;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.conform.action.ConfigBuilder;

class OneRateTwoColorConformActionConfigReaderTest {

    private static final String OUTPUT = """
              Policy Map TEST
                Class TRANSMIT
                 police cir 6000000 bc 187500
                   conform-action transmit\s
                   exceed-action drop\s
                Class COS
                 police cir 6000000 bc 187500
                   conform-action set-cos-transmit 7
                   exceed-action drop\s
                Class DEI
                 police cir 6000000 bc 187500
                   conform-action set-dei-transmit 1
                   exceed-action drop\s
                Class DSCP
                 police cir 6000000 bc 187500
                   conform-action set-dscp-transmit af32
                   exceed-action drop\s
                Class QOS
                 police cir 6000000 bc 187500
                   conform-action set-qos-transmit 13
                   exceed-action drop\s
            """;

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    void testTransmit() {
        OneRateTwoColorConformActionConfigReader.parseConfig("TRANSMIT", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(true, aug.isTransmit());
    }

    @Test
    void testCos() {
        OneRateTwoColorConformActionConfigReader.parseConfig("COS", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(Cos.getDefaultInstance("7"), aug.getCosTransmit());
    }

    @Test
    void testDei() {
        OneRateTwoColorConformActionConfigReader.parseConfig("DEI", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(Dei.getDefaultInstance("1"), aug.getDeiTransmit());
    }

    @Test
    void testDscp() {
        OneRateTwoColorConformActionConfigReader.parseConfig("DSCP", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(DscpBuilder.getDefaultInstance("af32"), aug.getDscpTransmit());
    }

    @Test
    void testQos() {
        OneRateTwoColorConformActionConfigReader.parseConfig("QOS", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(QosGroupBuilder.getDefaultInstance("13"), aug.getQosTransmit());
    }

}