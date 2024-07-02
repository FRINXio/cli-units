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

package io.frinx.cli.unit.brocade.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;

class EthernetConfigWriterTest {

    @Test
    void negotiationTest() {
        assertEquals("""
                configure terminal
                interface eth 1/1
                no gig-default auto-gig
                end""", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().setAutoNegotiate(true).build(), new ConfigBuilder().build()));

        assertEquals("""
                configure terminal
                interface eth 1/1
                no gig-default neg-off
                end""", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().setAutoNegotiate(false).build(), new ConfigBuilder().build()));

        assertEquals("""
                configure terminal
                interface eth 1/1
                gig-default auto-gig
                end""", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1", null,
                    new ConfigBuilder().setAutoNegotiate(true).build()));

        assertEquals("", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1", null,
                    new ConfigBuilder().build()));

        assertEquals("", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().setAutoNegotiate(true).build(),
                    new ConfigBuilder().setAutoNegotiate(true).build()));

        assertEquals("""
                configure terminal
                interface eth 1/1
                gig-default auto-gig
                end""", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().setAutoNegotiate(false).build(),
                    new ConfigBuilder().setAutoNegotiate(true).build()));

        assertEquals("""
                configure terminal
                interface eth 1/1
                gig-default neg-off
                end""", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().setAutoNegotiate(true).build(),
                    new ConfigBuilder().setAutoNegotiate(false).build()));

        assertEquals("""
                configure terminal
                interface eth 1/1
                gig-default neg-off
                end""", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().build(),
                    new ConfigBuilder().setAutoNegotiate(false).build()));

        assertEquals("", new EthernetConfigWriter(Mockito.mock(Cli.class)).getCommand("eth 1/1",
                    new ConfigBuilder().setAutoNegotiate(false).build(),
                    new ConfigBuilder().setAutoNegotiate(false).build()));
    }
}