/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpinterval;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class BundleEtherLacpIntervalConfigWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            bridge
            lacp port timeout 4/10 short
            end
            """;
    private static final String DELETE_INPUT = """
            configure terminal
            bridge
            no lacp port timeout 4/10
            end
            """;

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private BundleEtherLacpIntervalConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new BundleEtherLacpIntervalConfigWriter(cli);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    private void prepare(String ifName) {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(ifName))
                .augmentation(Interface1.class).child(Ethernet.class).child(Config.class);

        LacpEthConfigAugBuilder lcaB = new LacpEthConfigAugBuilder();
        lcaB.setInterval(LacpPeriodType.FAST);
        data = new ConfigBuilder()
                 .addAugmentation(LacpEthConfigAug.class, lcaB.build())
                 .build();
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        prepare("Ethernet4/10");
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        //run as delete
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("Ethernet4/10"))
            .augmentation(Interface1.class)
            .child(Ethernet.class)
            .child(Config.class);
        LacpEthConfigAugBuilder lcaB = new LacpEthConfigAugBuilder();
        lcaB.setInterval(LacpPeriodType.FAST);
        Config dataAfter = new ConfigBuilder().build();
        Config dataBefore = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, lcaB.build())
                .build();
        target.updateCurrentAttributesWResult(id, dataBefore, dataAfter, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributes_002() throws Exception {
        // run as write
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("Ethernet4/10"))
            .augmentation(Interface1.class)
            .child(Ethernet.class)
            .child(Config.class);
        LacpEthConfigAugBuilder lcaBefore = new LacpEthConfigAugBuilder();
        lcaBefore.setInterval(LacpPeriodType.SLOW);
        LacpEthConfigAugBuilder lcaB = new LacpEthConfigAugBuilder();
        lcaB.setInterval(LacpPeriodType.FAST);
        Config dataAfter = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, lcaB.build())
                .build();
        Config dataBefore = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, lcaBefore.build())
                .build();
        target.updateCurrentAttributesWResult(id, dataBefore, dataAfter, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        prepare("Ethernet4/10");
        target.deleteCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}