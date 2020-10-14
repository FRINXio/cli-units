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
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.CosValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DeiValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpValueBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.conform.action.ConfigBuilder;

public class TwoRateThreeColorConformActionConfigReaderTest {

    private static final String OUTPUT = "  Policy Map TEST\n"
            + "    Class TRANSMIT\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action drop \n"
            + "    Class COS\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action set-cos-transmit 7\n"
            + "       exceed-action drop \n"
            + "    Class DEI\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action set-dei-transmit 1\n"
            + "       exceed-action drop \n"
            + "    Class DSCP\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action set-dscp-transmit af32\n"
            + "       exceed-action drop \n"
            + "    Class QOS\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action set-qos-transmit 13\n"
            + "       exceed-action drop \n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testTransmit() {
        TwoRateThreeColorConformActionConfigReader.parseConfig("TRANSMIT", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        Assert.assertEquals(true, aug.isTransmit());
    }

    @Test
    public void testCos() {
        TwoRateThreeColorConformActionConfigReader.parseConfig("COS", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        Assert.assertEquals(CosValue.getDefaultInstance("7"), aug.getCos());
    }

    @Test
    public void testDei() {
        TwoRateThreeColorConformActionConfigReader.parseConfig("DEI", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        Assert.assertEquals(DeiValue.getDefaultInstance("1"), aug.getDei());
    }

    @Test
    public void testDscp() {
        TwoRateThreeColorConformActionConfigReader.parseConfig("DSCP", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("af32"), aug.getDscp());
    }

    @Test
    public void testQos() {
        TwoRateThreeColorConformActionConfigReader.parseConfig("QOS", OUTPUT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        Assert.assertEquals(QosGroupBuilder.getDefaultInstance("13"), aug.getQos());
    }

}