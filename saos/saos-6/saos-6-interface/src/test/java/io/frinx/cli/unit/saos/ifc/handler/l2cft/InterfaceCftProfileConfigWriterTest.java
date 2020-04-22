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

package io.frinx.cli.unit.saos.ifc.handler.l2cft;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.l2.cft.cft.profile.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.l2.cft.cft.profile.ConfigBuilder;

public class InterfaceCftProfileConfigWriterTest {

    private static final String SET_PROFILE = "l2-cft set port 1 profile TEST\n";
    private static final String UNSET_PROFILE = "l2-cft unset port 1 profile\n";
    private static final String ENABLE = "l2-cft enable port 1\n";
    private static final String DISABLE = "l2-cft disable port 1\n";
    private static final String COMMIT = "configuration save";

    private InterfaceCftProfileConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new InterfaceCftProfileConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        createTemplateAndTest(SET_PROFILE + ENABLE + COMMIT, createConfig("TEST", true), null);
        createTemplateAndTest(SET_PROFILE + COMMIT, createConfig("TEST", false), null);
        createTemplateAndTest(SET_PROFILE + COMMIT, createConfig("TEST", null), null);
    }

    @Test
    public void updateTemplateTest() {
        createTemplateAndTest(SET_PROFILE + COMMIT,
                createConfig("Test", true), createConfig("TEST", true));

        createTemplateAndTest(COMMIT, createConfig("HH", true), createConfig("HH", true));
        createTemplateAndTest(DISABLE + COMMIT, createConfig("Test", true), createConfig("Test", false));
        createTemplateAndTest(DISABLE + COMMIT, createConfig("Test", true), createConfig("Test", null));

        createTemplateAndTest(COMMIT, createConfig("Test", false), createConfig("Test", false));
        createTemplateAndTest(ENABLE + COMMIT, createConfig("Test", false), createConfig("Test", true));
        createTemplateAndTest(COMMIT, createConfig("Test", false), createConfig("Test", null));

        createTemplateAndTest(COMMIT, createConfig("Test", null), createConfig("Test", null));
        createTemplateAndTest(ENABLE + COMMIT, createConfig("Test", null), createConfig("Test", true));
        createTemplateAndTest(COMMIT, createConfig("Test", null), createConfig("Test", false));
    }

    @Test
    public void deleteTemplateTest() {
        Assert.assertEquals(DISABLE + UNSET_PROFILE + COMMIT,
                writer.deleteTemplate(createConfig("Test", true), "1"));
        Assert.assertEquals(UNSET_PROFILE + COMMIT,
                writer.deleteTemplate(createConfig("Test", false), "1"));
        Assert.assertEquals(UNSET_PROFILE + COMMIT,
                writer.deleteTemplate(createConfig("Test", null), "1"));
    }

    private void createTemplateAndTest(String expected, Config before, Config after) {
        if (after == null) {
            Assert.assertEquals(expected, writer.writeTemplate(before, "1"));
        } else {
            Assert.assertEquals(expected, writer.updateTemplate(before, after, "1"));
        }
    }

    private Config createConfig(String name, Boolean enabled) {
        ConfigBuilder builder = new ConfigBuilder();
        if (enabled != null) {
            return builder.setName(name).setEnabled(enabled).build();
        }
        return new ConfigBuilder().setName(name).build();
    }
}
