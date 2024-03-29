/*
 * Copyright © 2021 Frinx and others.
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
package io.frinx.cli.unit.huawei.qos.handler.classifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpEnumeration;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.Terms;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.TermsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.Classifiers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Dscp;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class ClassifierWriterTest {

    private static final String WRITE_ALL = """
            system-view
            traffic classifier map1 operator or
            if-match dscp cs3
            return
            """;

    private static final String WRITE_MORE = """
            system-view
            traffic classifier map1 operator or
            if-match dscp 13 12
            return
            """;

    private static final String DELETE_INPUT = """
            system-view
            undo traffic classifier map1
            return
            """;

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

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new ClassifierWriter(cli);
    }

    @Test
    void writeAll() throws WriteFailedException {
        data = getClassifier("map1", getTermsAll());
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_ALL, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    void writeAny() throws WriteFailedException {
        data = getClassifier("map1", getTermsAny());
        writer.writeCurrentAttributes(piid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_MORE, response.getValue().getContent());
    }

    private List<Term> getTermsAll() {
        Term termAll = new TermBuilder()
                .setId("all")
                .setConditions(new ConditionsBuilder()
                        .setIpv4(new Ipv4Builder()
                                .setConfig(new ConfigBuilder()
                                        .addAugmentation(QosIpv4ConditionAug.class, new QosIpv4ConditionAugBuilder()
                                                .setDscpEnum(DscpEnumeration.Cs3)
                                                .build())
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
                        .setIpv4(new Ipv4Builder()
                                .setConfig(new ConfigBuilder()
                                        .setDscp(Dscp.getDefaultInstance("13"))
                                        .build())
                                .build())
                        .build())
                .build();

        Term term2 = new TermBuilder()
                .setId("2")
                .setConditions(new ConditionsBuilder()
                        .setIpv4(new Ipv4Builder()
                                .setConfig(new ConfigBuilder()
                                        .setDscp(Dscp.getDefaultInstance("12"))
                                        .build())
                                .build())
                        .build())
                .build();

        return Lists.newArrayList(term2, term1);
    }

    private Classifier getClassifier(String name, List<Term> terms) {
        return new ClassifierBuilder()
                .setName(name)
                .setTerms(new TermsBuilder()
                        .setTerm(terms)
                        .build())
                .build();
    }
}
