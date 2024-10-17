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

package io.frinx.cli.unit.brocade.isis.handler.ifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis._interface.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis._interface.group.ConfigBuilder;

class InterfaceConfigWriterTest {

    @Test
    void writeTest() {
        InterfaceConfigWriter writer = new InterfaceConfigWriter(Mockito.mock(Cli.class));
        Config data = new ConfigBuilder().setPassive(true).build();
        assertEquals("""
                configure terminal
                interface ve 4
                ip router isis
                isis passive
                end""", writer.getWriteCommand("ve 4", data));

        data = new ConfigBuilder().build();
        assertEquals("""
                configure terminal
                interface ve 3
                ip router isis
                no isis passive
                end""", writer.getWriteCommand("ve 3", data));
    }
}