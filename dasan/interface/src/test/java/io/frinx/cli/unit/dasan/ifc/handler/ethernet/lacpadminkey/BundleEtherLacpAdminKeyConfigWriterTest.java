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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpadminkey;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Config1Builder;
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

class BundleEtherLacpAdminKeyConfigWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            bridge
            lacp port admin-key 4/10 2
            end
            """;
    private static final String DELETE_INPUT = """
            configure terminal
            bridge
            no lacp port admin-key 4/12
            end
            """;

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private BundleEtherLacpAdminkeyConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new BundleEtherLacpAdminkeyConfigWriter(cli));
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName, Integer adminkey) {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(ifName))
                .augmentation(Interface1.class).child(Ethernet.class).child(Config.class);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
        ethIfAggregationConfigBuilder.setAdminKey(adminkey);
        data = createConfig(ifType, ifName, adminkey);
    }

    private Config createConfig(Class<? extends IanaInterfaceType> ifType, String ifName, Integer adminkey) {
        Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
        ethIfAggregationConfigBuilder.setAdminKey(adminkey);
        return new ConfigBuilder()
                 .addAugmentation(Config1.class, ethIfAggregationConfigBuilder.build())
                 .build();
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet4/10", 2);
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testWriteCurrentAttributes_002() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet4/10", null);
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testWriteCurrentAttributes_003() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet4/12", 2);
        ConfigBuilder builder = new ConfigBuilder();
        data = builder.build();
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet4/10", 2);
        Config dataBefore = createConfig(Ieee8023adLag.class, "Ethernet4/10", 1);
        target.updateCurrentAttributesWResult(id, dataBefore, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet4/12",2);
        target.deleteCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}