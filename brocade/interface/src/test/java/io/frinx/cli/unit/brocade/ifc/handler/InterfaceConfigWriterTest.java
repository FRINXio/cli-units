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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;

class InterfaceConfigWriterTest {

    @Test
    void updateTemplate() {
        Config configBefore = getConfig("e 1", 4, true);

        InterfaceConfigWriter writer = new InterfaceConfigWriter(Mockito.mock(Cli.class));

        Config config = getConfig("e 1", 0, true);
        String output = writer.updateTemplate(configBefore, config);
        assertEquals("""
                configure terminal
                interface e 1
                enable
                priority 0
                end""", output);

        config = getConfig("e 1", 4, false);
        output = writer.updateTemplate(configBefore, config);
        assertEquals("""
                configure terminal
                interface e 1
                enable
                no priority force
                end""", output);

        config = getConfig("e 1", 4, false);
        output = writer.updateTemplate(null, config);
        assertEquals("""
                configure terminal
                interface e 1
                enable
                priority 4
                end""", output);

        config = getConfig("e 1", 4, true);
        output = writer.updateTemplate(configBefore, config);
        assertEquals("""
                configure terminal
                interface e 1
                enable
                end""", output);

        config = new ConfigBuilder()
                .setEnabled(true)
                .setName("e 1")
                .build();
        output = writer.updateTemplate(configBefore, config);
        assertEquals("""
                configure terminal
                interface e 1
                enable
                priority 0
                no priority force
                end""", output);
    }

    private IfBrocadePriorityAug getPriorityConfig(int priority, boolean force) {
        IfBrocadePriorityAugBuilder cfg;
        cfg = new IfBrocadePriorityAugBuilder()
            .setPriority((short) priority);
        if (force) {
            cfg.setPriorityForce(force);
        }
        return cfg.build();
    }

    private Config getConfig(String name, int priority, boolean force) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (priority > 0 || force) {
            configBuilder.addAugmentation(IfBrocadePriorityAug.class, getPriorityConfig(priority, force));
        }
        return configBuilder
            .setEnabled(true)
            .setName(name)
            .build();
    }
}