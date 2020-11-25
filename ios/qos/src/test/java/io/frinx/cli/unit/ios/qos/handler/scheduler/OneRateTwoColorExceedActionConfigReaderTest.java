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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Dei;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosExceedActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.exceed.action.ConfigBuilder;

public class OneRateTwoColorExceedActionConfigReaderTest {

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
        OneRateTwoColorExceedActionConfigReader.parseConfig("DROP", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getAugmentation(QosExceedActionAug.class));
        Assert.assertEquals(true, configBuilder.isDrop());
    }

    @Test
    public void testTransmit() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("TRANSMIT", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(true, aug.isTransmit());
    }

    @Test
    public void testCos() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("COS", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(Cos.getDefaultInstance("2"), aug.getCosTransmit());
    }

    @Test
    public void testDei() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("DEI", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(Dei.getDefaultInstance("1"), aug.getDeiTransmit());
    }

    @Test
    public void testDscp() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("DSCP", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(DscpBuilder.getDefaultInstance("cs7"), aug.getDscpTransmit());
    }

    @Test
    public void testQosGroup() {
        OneRateTwoColorExceedActionConfigReader.parseConfig("QOS", OUTPUT, configBuilder);
        final QosExceedActionAug aug = configBuilder.getAugmentation(QosExceedActionAug.class);
        Assert.assertEquals(QosGroupBuilder.getDefaultInstance("54"), aug.getQosTransmit());
    }

}