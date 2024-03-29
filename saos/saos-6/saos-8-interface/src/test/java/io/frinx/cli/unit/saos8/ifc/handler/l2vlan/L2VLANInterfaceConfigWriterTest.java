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

package io.frinx.cli.unit.saos8.ifc.handler.l2vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class L2VLANInterfaceConfigWriterTest {

    private static final String WRITE_COMMAND = "cpu-interface sub-interface create cpu-subinterface cpuMGMT\n";

    private static final String DELETE_COMMAND = "cpu-interface sub-interface delete cpu-subinterface cpuMGMT\n";

    @Mock
    private Cli cli;

    private final InstanceIdentifier<Config> iid = IIDs.IN_IN_CONFIG;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private L2VLANInterfaceConfigWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2VLANInterfaceConfigWriter(cli);
    }

    @Test
    void writeCurrentAttributesWResultTest() throws WriteFailedException {
        writer.writeCurrentAttributesWResult(iid, createConfig("cpu_subintf_cpuMGMT", L2vlan.class), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(WRITE_COMMAND, commands.getValue().getContent());
    }

    @Test
    void writeCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        assertFalse(writer
                .writeCurrentAttributesWResult(iid, createConfig("cpuMGMT", Ieee8023adLag.class), null));
    }

    @Test
    void updateCurrentAttributesWResultTest() throws WriteFailedException {
        assertThrows(WriteFailedException.class, () -> {
            writer.updateCurrentAttributesWResult(iid, createConfig("cpu_subintf_cpuMGMT", L2vlan.class),
                    createConfig("cpu_subintf_FRINX_TEST", L2vlan.class), null);
        });
    }

    @Test
    void updateCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        assertFalse(writer.updateCurrentAttributesWResult(iid,
                createConfig("cpuMGMT", Ieee8023adLag.class),
                createConfig("cpu_subintf_FRINX_TEST", L2vlan.class), null));
    }

    @Test
    void deleteCurrentAttributesWResultTest() throws WriteFailedException {
        writer.deleteCurrentAttributesWResult(iid, createConfig("cpu_subintf_cpuMGMT", L2vlan.class), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(DELETE_COMMAND, commands.getValue().getContent());
    }

    @Test
    void deleteCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        assertFalse(writer.deleteCurrentAttributesWResult(iid,
                createConfig("cpuMGMT", Ieee8023adLag.class), null));

    }

    private Config createConfig(String name, Class<? extends InterfaceType> type) {
        return new ConfigBuilder().setName(name).setType(type).build();
    }
}
