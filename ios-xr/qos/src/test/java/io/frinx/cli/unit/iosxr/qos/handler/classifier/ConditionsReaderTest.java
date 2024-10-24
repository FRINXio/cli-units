/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.qos.handler.classifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Precedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv6ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;


class ConditionsReaderTest {

    private static final String OUTPUT = """
            Mon Mar 19 12:26:57.492 UTC
            class-map match-any map1
             match access-group ipv4 inacl222\s
             match access-group ipv6 ahojgroup\s
             match precedence ipv4 1 5\s
             match precedence ipv6 1\s
             match precedence priority 4 network\s
             match mpls experimental topmost 6\s
             match qos-group 10 1-2
             end-class-map
            """;

    private static final String TOPMOST = """
            Mon Mar 19 12:26:57.492 UTC
            class-map match-any map1
             match access-group ipv4 inacl222\s
             match access-group ipv6 ahojgroup\s
             match precedence ipv4 1 5\s
             match precedence ipv6 1\s
             match precedence priority 4 network\s
             match mpls experimental topmost 6 7\s
             match qos-group 10 1-2
             end-class-map
            """;

    @Test
    void testConditionsReaderAll() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "all", builder);

        QosConditionAug qos = builder.getAugmentation(QosConditionAug.class);
        assertEquals(new QosGroup(10L), qos.getQosGroup()
                .get(0));
        assertEquals(new QosGroup(new QosGroupRange("1..2")), qos.getQosGroup()
                .get(1));

        QosIpv6ConditionAug aug4 = builder.getIpv6()
                .getConfig()
                .getAugmentation(QosIpv6ConditionAug.class);
        assertEquals("ahojgroup", aug4.getAclRef());

        QosIpv4ConditionAug aug6 = builder.getIpv4()
                .getConfig()
                .getAugmentation(QosIpv4ConditionAug.class);
        assertEquals("inacl222", aug6.getAclRef());

        assertEquals(6, builder.getMpls()
                .getConfig()
                .getTrafficClass()
                .get(0).shortValue());
    }

    @Test
    void testConditionsReaderTopMost() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(TOPMOST, "all", builder);

        QosConditionAug qos = builder.getAugmentation(QosConditionAug.class);
        assertEquals(new QosGroup(10L), qos.getQosGroup()
                .get(0));
        assertEquals(new QosGroup(new QosGroupRange("1..2")), qos.getQosGroup()
                .get(1));

        QosIpv6ConditionAug aug4 = builder.getIpv6()
                .getConfig()
                .getAugmentation(QosIpv6ConditionAug.class);
        assertEquals("ahojgroup", aug4.getAclRef());

        QosIpv4ConditionAug aug6 = builder.getIpv4()
                .getConfig()
                .getAugmentation(QosIpv4ConditionAug.class);
        assertEquals("inacl222", aug6.getAclRef());

        assertEquals(6, builder.getMpls()
                .getConfig()
                .getTrafficClass()
                .get(0).shortValue());

        assertEquals(7, builder.getMpls()
                .getConfig()
                .getTrafficClass()
                .get(1).shortValue());
    }

    @Test
    void testConditionsReaderAny1() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "1", builder);

        assertNull(builder.getMpls());
        assertNull(builder.getIpv6());

        QosIpv4ConditionAug aug = builder.getIpv4()
                .getConfig()
                .getAugmentation(QosIpv4ConditionAug.class);
        assertEquals("inacl222", aug.getAclRef());
    }

    @Test
    void testConditionsReaderAny2() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "2", builder);

        assertNull(builder.getMpls());
        assertNull(builder.getIpv4());

        QosIpv6ConditionAug aug = builder.getIpv6()
                .getConfig()
                .getAugmentation(QosIpv6ConditionAug.class);
        assertEquals("ahojgroup", aug.getAclRef());
    }

    @Test
    void testConditionsReaderAny3() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "3", builder);

        assertNull(builder.getMpls());
        assertNull(builder.getIpv6());

        assertEquals(
                Lists.newArrayList(
                        new Precedence((short) 1), new Precedence((short) 5)),
                builder.getIpv4()
                        .getConfig()
                        .getAugmentation(QosIpv4ConditionAug.class)
                        .getPrecedences());
    }

    @Test
    void testConditionsReaderAny4() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "4", builder);

        assertNull(builder.getMpls());
        assertNull(builder.getIpv4());

        assertEquals(
                Lists.newArrayList(new Precedence((short) 1)),
                builder.getIpv6()
                        .getConfig()
                        .getAugmentation(QosIpv6ConditionAug.class)
                        .getPrecedences());
    }

    @Test
    void testConditionsReaderAny5() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "5", builder);

        assertNull(builder.getMpls());
        assertNull(builder.getIpv4());
        assertNull(builder.getIpv6());

        assertEquals(
                Lists.newArrayList(new Precedence("priority"), new Precedence((short) 4), new Precedence("network")),
                builder.getAugmentation(QosConditionAug.class)
                        .getPrecedences());
    }

    @Test
    void testConditionsReaderAny6() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "6", builder);

        assertNull(builder.getIpv4());
        assertNull(builder.getIpv6());

        assertEquals(6, builder.getMpls()
                .getConfig()
                .getTrafficClass()
                .get(0).shortValue());
    }

    @Test
    void testConditionsReaderAny7() {
        ConditionsBuilder builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(OUTPUT, "7", builder);

        assertNull(builder.getMpls());
        assertNull(builder.getIpv4());
        assertNull(builder.getIpv6());

        assertEquals(Long.valueOf(10), builder.getAugmentation(QosConditionAug.class)
                .getQosGroup()
                .get(0)
                .getUint32());
        assertEquals("1..2", builder.getAugmentation(QosConditionAug.class)
                .getQosGroup()
                .get(1)
                .getQosGroupRange()
                .getValue());
    }
}