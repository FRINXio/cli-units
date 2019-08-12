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

package io.frinx.cli.unit.brocade.ifc.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;

public class InterfaceConfigWriterTest {

    @Test
    public void updateTemplate() {
        Config configBefore = getConfig("e 1", 4, true);

        InterfaceConfigWriter writer = new InterfaceConfigWriter(Mockito.mock(Cli.class));

        Config config = getConfig("e 1", 0, true);
        String output = writer.updateTemplate(configBefore, config);
        Assert.assertEquals("configure terminal\n"
                + "interface e 1\n"
                + "enable\n"
                + "priority 0\n"
                + "end", output);

        config = getConfig("e 1", 4, false);
        output = writer.updateTemplate(configBefore, config);
        Assert.assertEquals("configure terminal\n"
                + "interface e 1\n"
                + "enable\n"
                + "no priority force\n"
                + "end", output);

        config = getConfig("e 1", 4, false);
        output = writer.updateTemplate(null, config);
        Assert.assertEquals("configure terminal\n"
                + "interface e 1\n"
                + "enable\n"
                + "priority 4\n"
                + "end", output);

        config = getConfig("e 1", 4, true);
        output = writer.updateTemplate(configBefore, config);
        Assert.assertEquals("configure terminal\n"
                + "interface e 1\n"
                + "enable\n"
                + "end", output);
    }

    private Config getConfig(String name, int priority, boolean force) {
        IfBrocadePriorityAugBuilder cfg;
        Config config;
        cfg = new IfBrocadePriorityAugBuilder()
            .setPriority((short) priority);
        if (force) {
            cfg.setPriorityForce(force);
        }
        config = new ConfigBuilder()
            .setEnabled(true)
            .setName(name)
            .addAugmentation(IfBrocadePriorityAug.class, cfg.build())
            .build();
        return config;
    }
}