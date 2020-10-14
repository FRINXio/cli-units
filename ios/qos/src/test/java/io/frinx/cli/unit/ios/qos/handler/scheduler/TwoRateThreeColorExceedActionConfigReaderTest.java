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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.exceed.action.ConfigBuilder;

public class TwoRateThreeColorExceedActionConfigReaderTest {

    private static final String OUTPUT = "  Policy Map TEST\n"
            + "    Class DROP\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action drop \n"
            + "    Class TRANSMIT\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action transmit \n"
            + "    Class COS\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action set-cos-transmit 2\n"
            + "    Class DEI\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action set-dei-transmit 1\n"
            + "    Class DSCP\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action set-dscp-transmit cs7\n"
            + "    Class QOS\n"
            + "     police cir 6000000 bc 187500\n"
            + "       conform-action transmit \n"
            + "       exceed-action set-qos-transmit 54\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testDrop() {
        TwoRateThreeColorExceedActionConfigReader.parseConfig("DROP", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getAugmentation(QosExceedActionAug.class));
        Assert.assertEquals(true, configBuilder.isDrop());
    }

    @Test
    public void testTransmit() {
        TwoRateThreeColorExceedActionConfigReader.parseConfig("TRANSMIT", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(true, aug.isTransmit());
    }

    @Test
    public void testCos() {
        TwoRateThreeColorExceedActionConfigReader.parseConfig("COS", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(CosValue.getDefaultInstance("2"), aug.getCos());
    }

    @Test
    public void testDei() {
        TwoRateThreeColorExceedActionConfigReader.parseConfig("DEI", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(DeiValue.getDefaultInstance("1"), aug.getDei());
    }

    @Test
    public void testDscp() {
        TwoRateThreeColorExceedActionConfigReader.parseConfig("DSCP", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("cs7"), aug.getDscp());
    }

    @Test
    public void testQosGroup() {
        TwoRateThreeColorExceedActionConfigReader.parseConfig("QOS", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(QosGroupBuilder.getDefaultInstance("54"), aug.getQos());
    }

}