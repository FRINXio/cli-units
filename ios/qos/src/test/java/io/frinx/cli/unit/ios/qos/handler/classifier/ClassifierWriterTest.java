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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.CosValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpValueBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.CosBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.cos.CosListBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.dscp.DscpListBuilder;
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

    private static final String WRITE_ALL = "configure terminal\n"
            + "class-map match-all map1\n"
            + "match qos-group 10\n"
            + "match qos-group 35\n"
            + "match ip dscp default cs7\n"
            + "match ip dscp 13 af43 ef 61\n"
            + "match cos inner 1 2 5\n"
            + "match cos 3 6\n"
            + "end\n";

    private static final String WRITE_ANY_QOS = "configure terminal\n"
            + "class-map match-any map1\n"
            + "match qos-group 10\n"
            + "end\n";

    private static final String WRITE_ANY_DSCP = "configure terminal\n"
            + "class-map match-any map1\n"
            + "match ip dscp 13 af43 ef 61\n"
            + "end\n";

    private static final String WRITE_ANY_COS = "configure terminal\n"
            + "class-map match-any map1\n"
            + "match cos inner 1 2 5\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "no class-map map1\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private ClassifierWriter writer;
    private Classifier data;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(Classifiers.class)
            .child(Classifier.class, new ClassifierKey("map1"))
            .child(Terms.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new ClassifierWriter(cli);
    }

    @Test
    public void writeAll() throws WriteFailedException {
        data = getClassifier("map1", getTermsAll());
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_ALL, response.getValue().getContent());
    }

    @Test
    public void writeAny() throws WriteFailedException {
        data = getClassifier("map1", getTermsAny());
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli, Mockito.times(3)).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_ANY_QOS, response.getAllValues().get(0).getContent());
        Assert.assertEquals(WRITE_ANY_DSCP, response.getAllValues().get(1).getContent());
        Assert.assertEquals(WRITE_ANY_COS, response.getAllValues().get(2).getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private List<Term> getTermsAll() {
        Term termAll = new TermBuilder()
                .setId("all")
                .setConditions(new ConditionsBuilder()
                        .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                                .setQosGroup(Lists.newArrayList(new QosGroup(10L), new QosGroup(35L)))
                                .setDscp(new DscpBuilder()
                                        .setDscpList(Lists.newArrayList(
                                                new DscpListBuilder()
                                                        .setDscpValueList(getDscpValues("default", "cs7"))
                                                        .build(),
                                                new DscpListBuilder()
                                                        .setDscpValueList(getDscpValues("13", "af43", "ef", "61"))
                                                        .build()))
                                        .build())
                                .setCos(new CosBuilder()
                                        .setCosList(Lists.newArrayList(
                                                new CosListBuilder()
                                                        .setInner(true)
                                                        .setCosValueList(getCosValues("1", "2", "5"))
                                                        .build(),
                                                new CosListBuilder()
                                                        .setInner(false)
                                                        .setCosValueList(getCosValues("3", "6"))
                                                        .build()))
                                        .build())
                                .build())
                        .build())
                .build();

        return Lists.newArrayList(termAll);
    }

    private List<Term> getTermsAny() {
        Term term1 = new TermBuilder()
                .setId("1")
                .setConditions(new ConditionsBuilder()
                        .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                                .setQosGroup(Lists.newArrayList(new QosGroup(10L)))
                                .build())
                        .build())
                .build();

        Term term2 = new TermBuilder()
                .setId("2")
                .setConditions(new ConditionsBuilder()
                        .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                                .setDscp(new DscpBuilder()
                                        .setDscpList(Lists.newArrayList(
                                                new DscpListBuilder()
                                                        .setDscpValueList(getDscpValues("13", "af43", "ef", "61"))
                                                        .build()))
                                        .build())
                                .build())
                        .build())
                .build();

        Term term3 = new TermBuilder()
                .setId("3")
                .setConditions(new ConditionsBuilder()
                        .addAugmentation(QosConditionAug.class, new QosConditionAugBuilder()
                                .setCos(new CosBuilder()
                                        .setCosList(Lists.newArrayList(
                                                new CosListBuilder()
                                                        .setInner(true)
                                                        .setCosValueList(getCosValues("1", "2", "5"))
                                                        .build()))
                                        .build())
                                .build())
                        .build())
                .build();

        return Lists.newArrayList(term2, term3, term1);
    }

    private Classifier getClassifier(String name, List<Term> terms) {
        return new ClassifierBuilder()
                .setName(name)
                .setTerms(new TermsBuilder()
                        .setTerm(terms)
                        .build())
                .build();
    }

    private List<DscpValue> getDscpValues(String... values) {
        List<DscpValue> dscpValues = new ArrayList<>();
        for (String value : values) {
            dscpValues.add(DscpValueBuilder.getDefaultInstance(value));
        }
        return dscpValues;
    }

    private List<CosValue> getCosValues(String... values) {
        List<CosValue> cosValues = new ArrayList<>();
        for (String value : values) {
            cosValues.add(new CosValue(Short.valueOf(value)));
        }
        return cosValues;
    }

}