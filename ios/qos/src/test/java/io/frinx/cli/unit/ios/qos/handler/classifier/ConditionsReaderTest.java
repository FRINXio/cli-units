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

package io.frinx.cli.unit.ios.qos.handler.classifier;

import io.frinx.cli.unit.ios.qos.Util;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.CosValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpValueBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.cos.CosList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.dscp.DscpList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;

public class ConditionsReaderTest {

    private static final String OUTPUT = " Class Map match-all TEST (id 11)\n"
            + "   Match qos-group 12\n"
            + "   Match cos inner  1  2  5 \n"
            + "   Match qos-group 35\n"
            + "   Match ip  dscp default (0) cs7 (56)\n"
            + "   Match cos  3  6 \n"
            + "   Match cos inner  4 \n"
            + "   Match qos-group 64\n"
            + "   Match ip  dscp af11 (10)\n"
            + "   Match ip  dscp 13  af43 (38) ef (46) 61 \n";

    private ConditionsBuilder builder;
    private QosConditionAug aug;

    private void setup(String line) {
        this.builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(Util.deleteBrackets(OUTPUT), line, builder);
        this.aug = builder.getAugmentation(QosConditionAug.class);
    }

    @Test
    public void testAllQos() {
        setup("all");
        Assert.assertEquals(new QosGroup(12L), aug.getQosGroup().get(0));
        Assert.assertEquals(new QosGroup(35L), aug.getQosGroup().get(1));
        Assert.assertEquals(new QosGroup(64L), aug.getQosGroup().get(2));
    }

    @Test
    public void testAnyQos() {
        setup("1");
        Assert.assertNull(aug.getCos());
        Assert.assertNull(aug.getDscp());
        Assert.assertEquals(new QosGroup(12L), aug.getQosGroup().get(0));

        setup("3");
        Assert.assertEquals(new QosGroup(35L), aug.getQosGroup().get(0));

        setup("7");
        Assert.assertEquals(new QosGroup(64L), aug.getQosGroup().get(0));
    }

    @Test
    public void testAllCos() {
        setup("all");
        Assert.assertEquals(3, aug.getCos().getCosList().size());

        CosList cosList = aug.getCos().getCosList().get(0);
        Assert.assertEquals(true, cosList.isInner());
        Assert.assertEquals(3, cosList.getCosValueList().size());
        Assert.assertEquals(new CosValue(Short.valueOf("1")), cosList.getCosValueList().get(0));
        Assert.assertEquals(new CosValue(Short.valueOf("2")), cosList.getCosValueList().get(1));
        Assert.assertEquals(new CosValue(Short.valueOf("5")), cosList.getCosValueList().get(2));

        cosList = aug.getCos().getCosList().get(1);
        Assert.assertEquals(false, cosList.isInner());
        Assert.assertEquals(2, cosList.getCosValueList().size());
        Assert.assertEquals(new CosValue(Short.valueOf("3")), cosList.getCosValueList().get(0));
        Assert.assertEquals(new CosValue(Short.valueOf("6")), cosList.getCosValueList().get(1));

        cosList = aug.getCos().getCosList().get(2);
        Assert.assertEquals(true, cosList.isInner());
        Assert.assertEquals(1, cosList.getCosValueList().size());
        Assert.assertEquals(new CosValue(Short.valueOf("4")), cosList.getCosValueList().get(0));
    }

    @Test
    public void testAnyCos() {
        setup("2");
        Assert.assertNull(aug.getQosGroup());
        Assert.assertNull(aug.getDscp());
        Assert.assertEquals(1, aug.getCos().getCosList().size());
        CosList cosList = aug.getCos().getCosList().get(0);
        Assert.assertEquals(true, cosList.isInner());
        Assert.assertEquals(3, cosList.getCosValueList().size());
        Assert.assertEquals(new CosValue(Short.valueOf("1")), cosList.getCosValueList().get(0));
        Assert.assertEquals(new CosValue(Short.valueOf("2")), cosList.getCosValueList().get(1));
        Assert.assertEquals(new CosValue(Short.valueOf("5")), cosList.getCosValueList().get(2));

        setup("5");
        cosList = aug.getCos().getCosList().get(0);
        Assert.assertEquals(false, cosList.isInner());
        Assert.assertEquals(2, cosList.getCosValueList().size());
        Assert.assertEquals(new CosValue(Short.valueOf("3")), cosList.getCosValueList().get(0));
        Assert.assertEquals(new CosValue(Short.valueOf("6")), cosList.getCosValueList().get(1));

        setup("6");
        cosList = aug.getCos().getCosList().get(0);
        Assert.assertEquals(true, cosList.isInner());
        Assert.assertEquals(1, cosList.getCosValueList().size());
        Assert.assertEquals(new CosValue(Short.valueOf("4")), cosList.getCosValueList().get(0));
    }

    @Test
    public void testAllDscp() {
        setup("all");
        Assert.assertEquals(3, aug.getDscp().getDscpList().size());

        DscpList dscpList = aug.getDscp().getDscpList().get(0);
        Assert.assertEquals(2, dscpList.getDscpValueList().size());
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("default"),
                dscpList.getDscpValueList().get(0));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("cs7"),
                dscpList.getDscpValueList().get(1));

        dscpList = aug.getDscp().getDscpList().get(1);
        Assert.assertEquals(1, dscpList.getDscpValueList().size());
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("af11"),
                dscpList.getDscpValueList().get(0));

        dscpList = aug.getDscp().getDscpList().get(2);
        Assert.assertEquals(4, dscpList.getDscpValueList().size());
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("13"),
                dscpList.getDscpValueList().get(0));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("af43"),
                dscpList.getDscpValueList().get(1));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("ef"),
                dscpList.getDscpValueList().get(2));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("61"),
                dscpList.getDscpValueList().get(3));
    }

    @Test
    public void testAnyDscp() {
        setup("4");
        Assert.assertNull(aug.getQosGroup());
        Assert.assertNull(aug.getCos());
        Assert.assertEquals(1, aug.getDscp().getDscpList().size());
        DscpList dscpList = aug.getDscp().getDscpList().get(0);
        Assert.assertEquals(2, dscpList.getDscpValueList().size());
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("default"),
                dscpList.getDscpValueList().get(0));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("cs7"),
                dscpList.getDscpValueList().get(1));

        setup("8");
        dscpList = aug.getDscp().getDscpList().get(0);
        Assert.assertEquals(1, dscpList.getDscpValueList().size());
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("af11"),
                dscpList.getDscpValueList().get(0));

        setup("9");
        dscpList = aug.getDscp().getDscpList().get(0);
        Assert.assertEquals(4, dscpList.getDscpValueList().size());
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("13"),
                dscpList.getDscpValueList().get(0));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("af43"),
                dscpList.getDscpValueList().get(1));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("ef"),
                dscpList.getDscpValueList().get(2));
        Assert.assertEquals(DscpValueBuilder.getDefaultInstance("61"),
                dscpList.getDscpValueList().get(3));
    }

}