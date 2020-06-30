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

package io.frinx.cli.unit.saos8.ifc.handler.port;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private PortConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config dataBefore;
    private Config lagDataBefore;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new PortConfigWriter(this.cli);
        initializeData();
        initializeLagData();
    }

    private void initializeData() {
        dataBefore = new ConfigBuilder()
                .setEnabled(true)
                .setName("4")
                .setType(EthernetCsmacd.class)
                .setMtu(35)
                .setDescription("test")
                .build();
    }

    private void initializeLagData() {
        lagDataBefore = new ConfigBuilder()
                .setEnabled(true)
                .setName("Lag=FRINX_TEST")
                .setType(Ieee8023adLag.class)
                .setMtu(35)
                .setDescription("lag interface")
                .build();
    }

    @Test
    public void write() {
        try {
            this.writer.writeCurrentAttributesWResult(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            Assert.fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test
    public void updateTemplateTest() throws WriteFailedException {
        // nothig
        Assert.assertEquals("configuration save",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 35, "test")));

        // enabled
        Assert.assertEquals("port disable port 4\nconfiguration save",
                writer.updateTemplate(dataBefore,
                        createConfig("4", false, EthernetCsmacd.class, 35, "test")));

        // mtu
        Assert.assertEquals("port set port 4 max-frame-size 3555\nconfiguration save",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 3555, "test")));

        // description
        Assert.assertEquals("port set port 4 description \"new desc\"\nconfiguration save",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 35, "new desc")));

        // all
        Assert.assertEquals("port disable port 4\n"
                        + "port set port 4 description \"new desc\"\n"
                        + "port set port 4 max-frame-size 3555\n"
                        + "configuration save",
                writer.updateTemplate(dataBefore,
                        createConfig("4", false, EthernetCsmacd.class, 3555, "new desc")));
    }

    @Test
    public void delete() {
        try {
            this.writer.deleteCurrentAttributesWResult(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            Assert.fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test(expected = WriteFailedException.CreateFailedException.class)
    public void writeLag() throws WriteFailedException {
        writer.writeCurrentAttributesWResult(iid, lagDataBefore, context);
    }

    @Test(expected = WriteFailedException.DeleteFailedException.class)
    public void deleteLag() throws WriteFailedException {
        writer.deleteCurrentAttributesWResult(iid, lagDataBefore, context);
    }

    private Config createConfig(@Nonnull String name, @Nonnull Boolean enabled,
                                @Nonnull Class<? extends InterfaceType> type,
                                Integer mtu, String desc) {
        ConfigBuilder builder = new ConfigBuilder().setName(name).setEnabled(enabled).setType(type);

        if (mtu != null) {
            builder.setMtu(mtu);
        }
        if (desc != null) {
            builder.setDescription(desc);
        }

        return builder.build();
    }
}