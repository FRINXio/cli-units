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

package io.frinx.cli.unit.dasan.ifc.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.IanaInterfaceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


class PhysicalPortInterfaceConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;
    private PhysicalPortInterfaceConfigWriter target;
    private ArgumentCaptor<Command> response;
    //test data
    private InstanceIdentifier<Config> id;
    private Config data;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PhysicalPortInterfaceConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any()))
            .then(invocation -> CompletableFuture.completedFuture(""));
    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName) {
        id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey(ifName))
            .child(Config.class);

        data = new ConfigBuilder()
            .setEnabled(Boolean.TRUE)
            .setName(ifName)
            .setType(ifType)
            .setMtu(1000)
            .build();

        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Bundle-Ether100");

        target.writeCurrentAttributesWResult(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testWriteCurrentAttributes_002() throws Exception {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            prepare(EthernetCsmacd.class, "Ethernet100/200");

            target.writeCurrentAttributesWResult(id, data, context);

            Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
        });
        assertTrue(exception.getMessage().contains("Cannot create physical interface"));
    }

    @Test
    void testWriteCurrentAttributes_003() throws Exception {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            prepare(Ieee8023adLag.class, "Ethernet100/200");

            target.writeCurrentAttributesWResult(id, data, context);

            Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
        });
        assertTrue(exception.getMessage().contains(
            "Unexpected interface type: class org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana."
                + "_if.type.rev140508.Ieee8023adLag"));
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Bundle-Ether100");  //other interface name
        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributesWResult(id, data, newData, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testUpdateCurrentAttributes_002() throws Exception {
        prepare(EthernetCsmacd.class, "Ethernet100/200");
        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributesWResult(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "configure terminal",
            "bridge",
            "port enable 100/200",
            "jumbo-frame 100/200 1000",
            "end",
            ""
            ), "\n")));
    }

    @Test
    void testUpdateCurrentAttributes_003() throws Exception {
        prepare(EthernetCsmacd.class, "Ethernet100/200");
        Config newData = new ConfigBuilder(data)
            .setEnabled(Boolean.FALSE)  //if false, set disable
            .setMtu(null)               //if null, "no jumbo ..."
            .build();

        target.updateCurrentAttributesWResult(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "configure terminal",
            "bridge",
            "port disable 100/200",
            "no jumbo-frame 100/200",
            "end",
            ""
            ), "\n")));
    }

    @Test
    void testUpdateCurrentAttributes_004() throws Exception {
        prepare(EthernetCsmacd.class, "Ethernet100/200");
        Config newData = new ConfigBuilder(data)
            .setEnabled(null)  //if null, set disable
            .setMtu(null)
            .build();

        target.updateCurrentAttributesWResult(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "configure terminal",
            "bridge",
            "port disable 100/200",
            "no jumbo-frame 100/200",
            "end",
            ""
            ), "\n")));
    }

    @Test
    void testUpdateCurrentAttributes_005() throws Exception {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            prepare(EthernetCsmacd.class, "Ethernet100/200");
            Config newData = new ConfigBuilder(data)
                    .setType(Ieee8023adLag.class)
                    .build();

            target.updateCurrentAttributesWResult(id, data, newData, context);

            Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
        });
        assertTrue(exception.getMessage().contains("Changing interface type is not permitted. "
                + "Before: class org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508"
                + ".EthernetCsmacd, "
                + "After: class org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508"
                + ".Ieee8023adLag"));
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Bundle-Ether100");

        target.deleteCurrentAttributesWResult(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testDeleteCurrentAttributes_002() throws Exception {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            prepare(EthernetCsmacd.class, "Ethernet100/200");

            target.deleteCurrentAttributesWResult(id, data, context);

            Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
        });
        assertTrue(exception.getMessage().contains("Cannot delete physical interface"));
    }

    @Test
    void testDeleteCurrentAttributes_003() throws Exception {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            prepare(Ieee8023adLag.class, "Ethernet100/200");

            target.deleteCurrentAttributesWResult(id, data, context);

            Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
        });
        assertTrue(exception.getMessage().contains("Unexpected interface type: "
                + "class org.opendaylight.yang.gen.v1.urn.ietf.params.xml"
                + ".ns.yang.iana._if.type.rev140508.Ieee8023adLag"));
    }
}
