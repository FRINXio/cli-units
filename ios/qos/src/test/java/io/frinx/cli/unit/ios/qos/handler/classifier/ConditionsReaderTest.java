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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.access.group.config.AccessGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.access.group.config.access.group.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.MultipleCosBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.CosSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.cos.sets.ElementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.cos.sets.elements.ElementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;

public class ConditionsReaderTest {

    private static final String OUTPUT_ALL = " Class Map match-all TEST (id 3)\n\n"
            + "   Match qos-group 12\n"
            + "   Match cos inner  1 \n"
            + "   Match access-group name ingress-uplink-VLAN998877\n"
            + "   Match access-group name ACL_CPE_PROT_VLAN011220_WAN_IN_V6\n"
            + "   Match ip  dscp cs3 (24)\n\n";

    private static final String OUTPUT_ANY = " Class Map match-any TEST (id 3)\n\n"
            + "   Match qos-group 12\n"
            + "   Match qos-group 64\n"
            + "   Match cos inner  1 \n"
            + "   Match cos  4 \n"
            + "   Match ip  dscp 63 \n"
            + "   Match ip  dscp cs3 (24)\n\n";

    private ConditionsBuilder builder;

    private void setup(String output, String line) {
        this.builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(output, line, builder);
    }

    @Test
    public void testAllQos() {
        setup(OUTPUT_ALL, "all");
        QosConditionAug conditionAug = builder.getAugmentation(QosConditionAug.class);
        Assert.assertEquals(QosGroupBuilder.getDefaultInstance("12"), conditionAug.getQosGroup().get(0));
    }

    @Test
    public void testAnyQos() {
        setup(OUTPUT_ANY, "1");
        QosConditionAug conditionAug = builder.getAugmentation(QosConditionAug.class);
        Assert.assertEquals(QosGroupBuilder.getDefaultInstance("12"), conditionAug.getQosGroup().get(0));

        setup(OUTPUT_ANY, "2");
        conditionAug = builder.getAugmentation(QosConditionAug.class);
        Assert.assertEquals(QosGroupBuilder.getDefaultInstance("64"), conditionAug.getQosGroup().get(0));
    }

    @Test
    public void testAllCos() {
        setup(OUTPUT_ALL, "all");
        QosConditionAug conditionAug = builder.getAugmentation(QosConditionAug.class);
        MultipleCosBuilder expectedCos = new MultipleCosBuilder().setCosSets(List.of(
                new CosSetsBuilder()
                        .setId(1)
                        .setElements(new ElementsBuilder()
                                .setInner(true)
                                .setElement(List.of(
                                        new ElementBuilder()
                                                .setId(Cos.getDefaultInstance("1"))
                                                .build()
                                )).build()).build()
        ));
        AccessGroupBuilder expectedAcl = new AccessGroupBuilder().setAclSets(List.of(
                new AclSetsBuilder()
                        .setId(1)
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                                .yang.qos.extension.rev180304.qos.condition.access.group
                                .config.access.group.acl.sets.ConfigBuilder()
                                .setName("ingress-uplink-VLAN998877")
                                .build())
                        .build(),
                new AclSetsBuilder()
                        .setId(2)
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                                .yang.qos.extension.rev180304.qos.condition.access.group
                                .config.access.group.acl.sets.ConfigBuilder()
                                .setName("ACL_CPE_PROT_VLAN011220_WAN_IN_V6")
                                .build())
                        .build()
                ));
        Assert.assertEquals(expectedCos.build(), conditionAug.getMultipleCos());
        Assert.assertEquals(expectedAcl.build(), conditionAug.getAccessGroup());

    }

    @Test
    public void testAnyCos() {
        setup(OUTPUT_ANY, "3");
        QosConditionAug conditionAug = builder.getAugmentation(QosConditionAug.class);
        MultipleCosBuilder expected = new MultipleCosBuilder().setCosSets(List.of(
                new CosSetsBuilder()
                        .setId(1)
                        .setElements(new ElementsBuilder()
                                .setInner(true)
                                .setElement(List.of(
                                        new ElementBuilder()
                                                .setId(Cos.getDefaultInstance("1"))
                                                .build()
                                )).build()).build()
        ));
        Assert.assertEquals(expected.build(), conditionAug.getMultipleCos());

        setup(OUTPUT_ANY, "4");
        conditionAug = builder.getAugmentation(QosConditionAug.class);
        expected = new MultipleCosBuilder().setCosSets(List.of(
                new CosSetsBuilder()
                        .setId(1)
                        .setElements(new ElementsBuilder()
                                .setInner(false)
                                .setElement(List.of(
                                        new ElementBuilder()
                                                .setId(Cos.getDefaultInstance("4"))
                                                .build()
                                )).build()).build()
        ));
        Assert.assertEquals(expected.build(), conditionAug.getMultipleCos());
    }

    @Test
    public void testAllDscp() {
        setup(OUTPUT_ALL, "all");
        QosIpv4ConditionAug v4aug = builder.getIpv4().getConfig().getAugmentation(QosIpv4ConditionAug.class);

        Assert.assertEquals(DscpBuilder.getDefaultInstance("cs3").getDscpEnumeration(),
                v4aug.getDscpEnum());
    }

    @Test
    public void testAnyDscp() {
        setup(OUTPUT_ANY, "5");
        Assert.assertEquals(DscpBuilder.getDefaultInstance("63").getDscp(),
                builder.getIpv4().getConfig().getDscp());

        setup(OUTPUT_ANY, "6");
        QosIpv4ConditionAug v4aug = builder.getIpv4().getConfig().getAugmentation(QosIpv4ConditionAug.class);
        Assert.assertEquals(DscpBuilder.getDefaultInstance("cs3").getDscpEnumeration(),
                v4aug.getDscpEnum());
    }

}