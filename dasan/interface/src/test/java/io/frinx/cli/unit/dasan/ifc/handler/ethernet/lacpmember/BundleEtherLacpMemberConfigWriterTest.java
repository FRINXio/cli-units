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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.IanaInterfaceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpMemberConfigWriterTest {

    private static final String WRITE_INPUT = "configure terminal\n"
            + "bridge\n"
            + "\n"
            + "lacp port 3/4 aggregator 8\n"
            + "end\n";
    private static final String UPDATE_INPUT = "configure terminal\n"
            + "bridge\n"
            + "no lacp port 3/4 aggregator 9\n"
            + "lacp port 3/4 aggregator 8\n"
            + "end\n";
    private static final String UPDATE_INPUT_NONASSIGN = "configure terminal\n"
            + "bridge\n"
            + "no lacp port 3/4 aggregator 9\n"
            + "\n"
            + "end\n";
    private static final String DELETE_INPUT = "configure terminal\n"
            + "bridge\n"
            + "no lacp port 2/4 aggregator 7\n"
            + "end\n";

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private BundleEtherLacpMemberConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new BundleEtherLacpMemberConfigWriter(cli));
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    private Config generateConfig(Class<? extends IanaInterfaceType> ifType, String ifName, String aggregateId) {
        Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
        ethIfAggregationConfigBuilder.setAggregateId(aggregateId);
        return new ConfigBuilder()
                 .addAugmentation(Config1.class, ethIfAggregationConfigBuilder.build())
                 .build();
    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName, String aggregateId) {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(ifName))
                .augmentation(Interface1.class).child(Ethernet.class).child(Config.class);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        data = generateConfig(ifType, ifName, aggregateId);
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet3/4", "Bundle-Ether8");
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet4/4", null);
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testWriteCurrentAttributes_003() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet2/4", "Bundle-Ether8");
        ConfigBuilder builder = new ConfigBuilder();
        data = builder.build();
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet3/4", "Bundle-Ether8");
        Config dataBefore = generateConfig(Ieee8023adLag.class, "Ethernet3/4", "Bundle-Ether9");
        target.updateCurrentAttributes(id, dataBefore, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet3/4", null);
        Config dataBefore = generateConfig(Ieee8023adLag.class, "Ethernet3/4", "Bundle-Ether9");
        target.updateCurrentAttributes(id, dataBefore, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT_NONASSIGN, response.getValue()
                .getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet2/4", "Bundle-Ether7");
        target.deleteCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}