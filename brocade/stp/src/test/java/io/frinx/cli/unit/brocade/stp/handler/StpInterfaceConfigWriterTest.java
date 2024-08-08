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

package io.frinx.cli.unit.brocade.stp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class StpInterfaceConfigWriterTest {

    @Test
    void writeStpInterfaces() {
        Config config = new ConfigBuilder().setName("ethernet 1/2").build();

        StpInterfaceConfigWriter writer = new StpInterfaceConfigWriter(Mockito.mock(Cli.class));
        assertEquals("""
                configure terminal
                interface ethernet 1/2
                spanning-tree
                end""", writer.getCommand(config, false));
    }

    @Test
    void writeWithException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Config config = new ConfigBuilder().setName("ve 12").build();
            try {
                StpInterfaceConfigWriter writer = new StpInterfaceConfigWriter(Mockito.mock(Cli.class));
                writer.writeCurrentAttributes(Mockito.mock(InstanceIdentifier.class),
                    config, Mockito.mock(WriteContext.class));
            } catch (WriteFailedException e) {
                // Ignore
            }
        });
    }
}