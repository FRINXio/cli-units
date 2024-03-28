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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PortConfigWriterTest {

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

    @BeforeEach
    void setUp() {
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
                .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder().setNegotiationAuto(false).build())
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
    void write() {
        try {
            this.writer.writeCurrentAttributesWResult(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test
    void updateTemplateTest() {
        // nothig
        assertEquals("",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 35, "test", false, null)));

        // enabled
        assertEquals("port disable port 4\n",
                writer.updateTemplate(dataBefore,
                        createConfig("4", false, EthernetCsmacd.class, 35, "test", false, null)));

        // mtu
        assertEquals("port set port 4 max-frame-size 3555\n",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 3555, "test", false, null)));

        // description
        assertEquals("port set port 4 description \"new desc\"\n",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 35, "new desc", false, null)));

        // speed
        assertEquals("port set port 4 speed gigabit\n",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 35, "test", false,
                                SaosIfExtensionConfig.SpeedType.Gigabit)));

        // negotiation auto
        assertEquals("port set port 4 auto-neg on\n",
                writer.updateTemplate(dataBefore,
                        createConfig("4", true, EthernetCsmacd.class, 35, "test", true, null)));

        // all
        assertEquals("""
                        port disable port 4
                        port set port 4 description "new desc"
                        port set port 4 max-frame-size 3555
                        port set port 4 speed gigabit
                        port set port 4 auto-neg on
                        """,
                writer.updateTemplate(dataBefore,
                        createConfig("4", false, EthernetCsmacd.class, 3555, "new desc", true,
                                SaosIfExtensionConfig.SpeedType.Gigabit)));
    }

    @Test
    void delete() {
        try {
            this.writer.deleteCurrentAttributesWResult(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test
    void writeLag() throws WriteFailedException {
        assertThrows(WriteFailedException.CreateFailedException.class, () -> {
            writer.writeCurrentAttributesWResult(iid, lagDataBefore, context);
        });
    }

    @Test
    void deleteLag() throws WriteFailedException {
        assertThrows(WriteFailedException.DeleteFailedException.class, () -> {
            writer.deleteCurrentAttributesWResult(iid, lagDataBefore, context);
        });
    }

    private Config createConfig(@NotNull String name, @NotNull Boolean enabled,
                                @NotNull Class<? extends InterfaceType> type,
                                Integer mtu, String desc, Boolean negotiationAuto,
                                SaosIfExtensionConfig.SpeedType speedType) {
        ConfigBuilder builder = new ConfigBuilder().setName(name).setEnabled(enabled).setType(type);

        if (mtu != null) {
            builder.setMtu(mtu);
        }
        if (desc != null) {
            builder.setDescription(desc);
        }
        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        if (negotiationAuto != null) {
            ifSaosAugBuilder.setNegotiationAuto(negotiationAuto);
        }
        if (speedType != null) {
            ifSaosAugBuilder.setSpeedType(speedType);
        }

        builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        return builder.build();
    }
}