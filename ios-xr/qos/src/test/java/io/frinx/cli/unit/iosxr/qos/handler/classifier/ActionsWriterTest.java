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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Precedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupRange;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.Terms;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.actions.RemarkBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.Classifiers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class ActionsWriterTest {

    private static final String WRITE_INPUT = """
            policy-map plmap
            class map1
            set mpls experimental topmost 1
            set qos-group 30
            set precedence 4
            root
            """;

    private static final String UPDATE_INPUT = """
            policy-map plmap
            class map1
            set mpls experimental topmost 2
            set qos-group 5-8
            set precedence something
            root
            """;

    private static final String DELETE_INPUT = """
            policy-map plmap
            no class map1
            root
            """;

    private static final String UPDATE_POLICY_INPUT = """
            policy-map plmap1
            class map1
            set mpls experimental topmost 1
            no set qos-group
            no set precedence
            root
            """;


    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private ActionsWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(Classifiers.class)
            .child(Classifier.class, new ClassifierKey("map1"))
            .child(Terms.class)
            .child(Term.class, new TermKey("all"))
            .child(Actions.class);

    // test data
    private Actions data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new ActionsWriter(this.cli);

        initializeData();
    }

    private void initializeData() {
        data = new ActionsBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                        .classifier.terms.top.terms.term.actions.ConfigBuilder()
                        .setTargetGroup("plmap")
                        .build())
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 1)
                                .addAugmentation(QosRemarkQosGroupAug.class,
                                        new QosRemarkQosGroupAugBuilder()
                                                .setSetQosGroup(Lists.newArrayList(new QosGroup(30L)))
                                                .setSetPrecedences(Lists.newArrayList(new Precedence((short) 4)))
                                                .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(piid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void update() throws WriteFailedException {
        data = new ActionsBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                        .classifier.terms.top.terms.term.actions.ConfigBuilder()
                        .setTargetGroup("plmap")
                        .build())
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 2)
                                .addAugmentation(QosRemarkQosGroupAug.class,
                                        new QosRemarkQosGroupAugBuilder()
                                                .setSetQosGroup(Lists.newArrayList(new QosGroup(
                                                        new QosGroupRange("5..8"))))
                                                .setSetPrecedences(Lists.newArrayList(new Precedence("something")))
                                                .build())
                                .build())
                        .build())
                .build();

        this.writer.updateCurrentAttributes(piid, data, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updatePolicy() throws WriteFailedException {
        Actions newData = new ActionsBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                        .classifier.terms.top.terms.term.actions.ConfigBuilder()
                        .setTargetGroup("plmap1")
                        .build())
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 1)
                                .build())
                        .build())
                .build();

        this.writer.updateCurrentAttributes(piid, data, newData, context);

        Mockito.verify(cli, Mockito.times(2))
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getAllValues()
                .get(0)
                .getContent());
        assertEquals(UPDATE_POLICY_INPUT, response.getAllValues()
                .get(1)
                .getContent());
    }

    @Test
    void addClassToPolicy() throws WriteFailedException {
        data = new ActionsBuilder()
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 1)
                                .addAugmentation(QosRemarkQosGroupAug.class,
                                        new QosRemarkQosGroupAugBuilder()
                                                .setSetQosGroup(Lists.newArrayList(new QosGroup(30L)))
                                                .setSetPrecedences(Lists.newArrayList(new Precedence((short) 4)))
                                                .build())
                                .build())
                        .build())
                .build();

        Actions newData = new ActionsBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                        .classifier.terms.top.terms.term.actions.ConfigBuilder()
                        .setTargetGroup("plmap")
                        .build())
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 1)
                                .addAugmentation(QosRemarkQosGroupAug.class,
                                        new QosRemarkQosGroupAugBuilder()
                                                .setSetQosGroup(Lists.newArrayList(new QosGroup(30L)))
                                                .setSetPrecedences(Lists.newArrayList(new Precedence((short) 4)))
                                                .build())
                                .build())
                        .build())
                .build();

        this.writer.updateCurrentAttributes(piid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void removeClassFromPolicy() throws WriteFailedException {
        data = new ActionsBuilder()
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 1)
                                .addAugmentation(QosRemarkQosGroupAug.class,
                                        new QosRemarkQosGroupAugBuilder()
                                                .setSetQosGroup(Lists.newArrayList(new QosGroup(30L)))
                                                .setSetPrecedences(Lists.newArrayList(new Precedence((short) 4)))
                                                .build())
                                .build())
                        .build())
                .build();

        Actions newData = new ActionsBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                        .classifier.terms.top.terms.term.actions.ConfigBuilder()
                        .setTargetGroup("plmap")
                        .build())
                .setRemark(new RemarkBuilder()
                        .setConfig(new ConfigBuilder()
                                .setSetMplsTc((short) 1)
                                .addAugmentation(QosRemarkQosGroupAug.class,
                                        new QosRemarkQosGroupAugBuilder()
                                                .setSetQosGroup(Lists.newArrayList(new QosGroup(30L)))
                                                .setSetPrecedences(Lists.newArrayList(new Precedence((short) 4)))
                                                .build())
                                .build())
                        .build())
                .build();
        this.writer.updateCurrentAttributes(piid, newData, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(piid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
