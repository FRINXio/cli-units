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

package io.frinx.cli.iosxr.qos.handler.classifier;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.mpls.header.top.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Precedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv6ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv6ConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.Terms;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.TermsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.Classifiers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class ClassifierWriterTest {

    private static final String WRITE_INPUT_ALL = "class-map match-all map1\n" +
            "match qos-group 10 1-5\n" +
            "match precedence 2 3 network\n" +
            "match mpls experimental topmost 1\n" +
            "match access-group ipv4 acl4\n" +
            "match access-group ipv6 acl6\n" +
            "match precedence ipv4 0 7 network\n" +
            "match precedence ipv6 critical\n" +
            "root\n";

    private static final String WRITE_QOS_ANY = "class-map match-any map1\nmatch qos-group 10 1-5\nroot\n";
    private static final String WRITE_PREC_ANY = "class-map match-any map1\nmatch precedence 2 3 network\nroot\n";
    private static final String WRITE_MPLS_ANY = "class-map match-any map1\nmatch mpls experimental topmost 1\nroot\n";
    private static final String WRITE_ACG_IPV4_ANY = "class-map match-any map1\nmatch access-group ipv4 acl4\nroot\n";
    private static final String WRITE_PREC_IPV4_ANY = "class-map match-any map1\nmatch precedence ipv4 0 7 network\nroot\n";

    private static final String DELETE_INPUT = "no class-map map1\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private ClassifierWriter writer;

    private ArgumentCaptor<String> response = ArgumentCaptor.forClass(String.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(Classifiers.class)
            .child(Classifier.class, new ClassifierKey("map1"))
            .child(Terms.class);

    // test data
    private Classifier data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new ClassifierWriter(this.cli);

        initializeData();
    }

    private void initializeData() {
        TermBuilder termAll = new TermBuilder();
        termAll.setId("all");
        termAll.setConditions(new ConditionsBuilder()
            .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                .setQosGroup(Lists.newArrayList(new QosGroup(10L), new QosGroup(new QosGroupRange("1..5"))))
                .setPrecedences(Lists.newArrayList(new Precedence((short) 2), new Precedence((short) 3), new Precedence("network"))).build())
            .setIpv4(new Ipv4Builder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder()
                .addAugmentation(QosIpv4ConditionAug.class, new QosIpv4ConditionAugBuilder()
                    .setAclRef("acl4")
                    .setPrecedences(Lists.newArrayList(new Precedence((short) 0), new Precedence((short) 7), new Precedence("network")))
                    .build())
                .build())
            .build())
                .setIpv6(new Ipv6Builder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                    .addAugmentation(QosIpv6ConditionAug.class, new QosIpv6ConditionAugBuilder()
                        .setAclRef("acl6")
                        .setPrecedences(Lists.newArrayList(new Precedence("critical")))
                        .build())
                    .build())
                .build())
            .setMpls(new MplsBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.mpls.header.top.mpls.ConfigBuilder()
                    .setTrafficClass((short) 1).build()).build())
        .build());

        data = new ClassifierBuilder()
            .setName("map1")
            .setTerms(new TermsBuilder().setTerm(Lists.newArrayList(termAll.build())).build())
            .build();
    }

    @Test
    public void writeAny() throws WriteFailedException {
        Term term1 = new TermBuilder().setId("1")
            .setConditions(new ConditionsBuilder()
                .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                        .setQosGroup(Lists.newArrayList(new QosGroup(10L), new QosGroup(new QosGroupRange("1..5")))).build())
                .build())
        .build();

        Term term2 = new TermBuilder().setId("2")
            .setConditions(new ConditionsBuilder()
                .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                    .setPrecedences(Lists.newArrayList(new Precedence((short) 2), new Precedence((short) 3), new Precedence("network"))).build())
                .build())
        .build();

        Term term3 = new TermBuilder().setId("3")
            .setConditions(new ConditionsBuilder()
                .setIpv4(new Ipv4Builder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder()
                        .addAugmentation(QosIpv4ConditionAug.class, new QosIpv4ConditionAugBuilder()
                                .setAclRef("acl4").build())
                        .build()).build())
                .build())
        .build();

        Term term4 = new TermBuilder().setId("4")
            .setConditions(new ConditionsBuilder()
                .setIpv4(new Ipv4Builder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder()
                    .addAugmentation(QosIpv4ConditionAug.class, new QosIpv4ConditionAugBuilder()
                        .setPrecedences(Lists.newArrayList(new Precedence((short) 0), new Precedence((short) 7), new Precedence("network"))).build())
                    .build()).build())
                .build())
        .build();

        Term term5 = new TermBuilder().setId("5")
            .setConditions(new ConditionsBuilder()
                .setMpls(new MplsBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.mpls.header.top.mpls.ConfigBuilder()
                        .setTrafficClass((short) 1).build())
                    .build())
                .build())
        .build();

        data = new ClassifierBuilder()
            .setName("map1")
            .setTerms(new TermsBuilder().setTerm(Lists.newArrayList(term1, term2, term3, term4, term5)).build())
            .build();

        this.writer.writeCurrentAttributes(piid, data, context);

        Mockito.verify(cli, Mockito.times(5)).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_QOS_ANY, response.getAllValues().get(0));
        Assert.assertEquals(WRITE_PREC_ANY, response.getAllValues().get(1));
        Assert.assertEquals(WRITE_ACG_IPV4_ANY, response.getAllValues().get(2));
        Assert.assertEquals(WRITE_PREC_IPV4_ANY, response.getAllValues().get(3));
        Assert.assertEquals(WRITE_MPLS_ANY, response.getAllValues().get(4));
    }

    @Test
    public void writeAll() throws WriteFailedException {
        this.writer.writeCurrentAttributes(piid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_ALL, response.getValue());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(piid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue());
    }
}
