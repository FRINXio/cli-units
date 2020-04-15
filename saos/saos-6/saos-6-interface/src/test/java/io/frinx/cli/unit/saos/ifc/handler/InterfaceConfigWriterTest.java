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

package io.frinx.cli.unit.saos.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriterTest {

    private static final String UPDATE_INPUT = "port disable port 4\n"
            + "port set port 4 description \"updated desc\"\n"
            + "port set port 4 max-frame-size 50\n"
            + "port set port 4 acceptable-frame-type all\n"
            + "port set port 4 mode default\n"
            + "port set port 4 vs-ingress-filter off\n"
            + "virtual-circuit ethernet set port 4 vlan-ethertype-policy all\n"
            + "port set port 4 ingress-to-egress-qmap NNI-NNI\n"
            + "flow access-control set port 4 max-dynamic-macs 200\n"
            + "flow access-control set port 4 forward-unlearned off\n"
            + "configuration save"
            + "\n";

    private static final String UPDATE_CLEAN_INPUT = "port disable port 4\n"
            + "port unset port 4 description\n"
            + "port set port 4 max-frame-size 9216\n"
            + "port set port 4 acceptable-frame-type all\n"
            + "port set port 4 mode default\n"
            + "port set port 4 vs-ingress-filter off\n"
            + "virtual-circuit ethernet set port 4 vlan-ethertype-policy all\n"
            + "flow access-control set port 4 forward-unlearned on\n"
            + "configuration save"
            + "\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder()
                .setEnabled(true)
                .setName("4")
                .setType(EthernetCsmacd.class)
                .setMtu(35)
                .setDescription("test")
                .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                        .setPhysicalType(PhysicalType.Rj45)
                        .setVlanIds(Arrays.asList("25", "50", "1234"))
                        .setIngressToEgressQmap(IngressToEgressQmap.DefaultRCOS)
                        .setMaxDynamicMacs(200)
                        .setForwardUnlearned(false)
                        .build())
                .build();
    }

    @Test
    public void write() {
        try {
            this.writer.writeCurrentAttributes(iid, data, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            TestCase.fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test
    public void update() throws WriteFailedException {
        // update values
        Config newData = new ConfigBuilder().setEnabled(false).setName("4").setType(EthernetCsmacd.class)
                .setMtu(50)
                .setDescription("updated desc")
                .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                        .setVlanIds(Arrays.asList("25", "50"))
                        .setIngressToEgressQmap(IngressToEgressQmap.NNINNI)
                        .setMaxDynamicMacs(200)
                        .setForwardUnlearned(false)
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().setEnabled(false).setName("4").setType(EthernetCsmacd.class)
                .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setForwardUnlearned(true)
                    .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() {
        try {
            this.writer.deleteCurrentAttributes(iid, data, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            TestCase.fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }
}