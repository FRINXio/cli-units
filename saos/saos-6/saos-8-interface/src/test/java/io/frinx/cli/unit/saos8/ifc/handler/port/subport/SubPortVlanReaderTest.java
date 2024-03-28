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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalElementsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElementKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.element.ConfigBuilder;

class SubPortVlanReaderTest {

    private static final String OUTPUT =
            """
                    sub-port add sub-port LAG=LS02W_100_1 class-element 100 vtag-stack 100
                    sub-port add sub-port LAG=LS02W_100_1 class-element 32 vtag-stack *
                    sub-port add sub-port LAG=LS02W_100_1 class-element 38 vlan-untagged-data
                    """;

    private final List<ClassElement> expecredElements =
                    Arrays.asList(
                        new ClassElementBuilder()
                            .setKey(new ClassElementKey("100"))
                            .setConfig(new ConfigBuilder()
                                .setId("100")
                                .setVtagStack("100")
                                .setVlanUntaggedData(false)
                                .build())
                            .build(),
                        new ClassElementBuilder()
                            .setKey(new ClassElementKey("32"))
                            .setConfig(new ConfigBuilder()
                                .setId("32")
                                .setVtagStack("*")
                                .setVlanUntaggedData(false)
                                .build())
                            .build(),
                        new ClassElementBuilder()
                            .setKey(new ClassElementKey("38"))
                            .setConfig(new ConfigBuilder()
                                 .setId("38")
                                 .setVlanUntaggedData(true)
                                 .build())
                            .build());

    @Test
    void parseVlanTest() {
        var builder = new VlanBuilder();
        SubPortVlanReader.parseVlan(OUTPUT, builder);
        assertEquals(expecredElements, builder.getAugmentation(Saos8VlanLogicalElementsAug.class)
                .getClassElement());
    }
}
