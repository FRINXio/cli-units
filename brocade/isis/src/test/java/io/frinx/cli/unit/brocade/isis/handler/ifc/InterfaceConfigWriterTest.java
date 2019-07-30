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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis._interface.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis._interface.group.ConfigBuilder;

public class InterfaceConfigWriterTest {

    @Test
    public void writeTest() {
        InterfaceConfigWriter writer = new InterfaceConfigWriter(Mockito.mock(Cli.class));
        Config data = new ConfigBuilder().setPassive(true).build();
        Assert.assertEquals("configure terminal\n"
                + "interface ve 4\n"
                + "ip router isis\n"
                + "isis passive\n"
                + "end", writer.getWriteCommand("ve 4", data));

        data = new ConfigBuilder().build();
        Assert.assertEquals("configure terminal\n"
                + "interface ve 3\n"
                + "ip router isis\n"
                + "no isis passive\n"
                + "end", writer.getWriteCommand("ve 3", data));
    }
}