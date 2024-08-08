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

package io.frinx.cli.unit.brocade.network.instance.l2vsi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

public class L2VSIConfigWriterTest {

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                L2VSIConfigWriter.VPLS_MTU,
                getConfig(21),
                getConfig(22),
                """
                configure terminal
                router mpls
                vpls test 1234
                vpls-mtu 22
                end"""
            },
            {
                L2VSIConfigWriter.VPLS_MTU,
                getConfig(21),
                getConfig(null),
                """
                configure terminal
                router mpls
                vpls test 1234
                no vpls-mtu 21
                end"""
            },
            {
                L2VSIConfigWriter.VPLS_MTU,
                getConfig(null),
                getConfig(22),
                """
                configure terminal
                router mpls
                vpls test 1234
                vpls-mtu 22
                end"""
            },
            {
                L2VSIConfigWriter.DELETE_VPLS_MTU,
                null,
                getConfig(22),
                """
                configure terminal
                router mpls
                vpls test 1234
                no vpls-mtu 22
                end"""
            },
            {
                L2VSIConfigWriter.DELETE_VPLS_MTU,
                null,
                getConfig(null),
                ""
            }
        });
    }

    private static Config getConfig(Integer mtu) {
        return new ConfigBuilder().setMtu(mtu).setName("test").build();
    }

    @MethodSource("data")
    @ParameterizedTest
    void getCommandTest(String template, Config before, Config data, String output) {
        L2VSIConfigWriter writer = new L2VSIConfigWriter(Mockito.mock(Cli.class));
        assertEquals(output, writer.getCommand(template, before, data, 1234L));
    }
}