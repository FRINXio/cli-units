/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.arris.rev220506.IfArrisExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.arris.rev220506.IfArrisExtensionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;

class EthernetConfigReaderTest {

    private static final String SH_INT_CONFIG_ETHERNET = """
            configure
            interface ethernet 6/3\s
             fec auto
             link-aggregate 0
             no shutdown
            exit
            end
            """;

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .addAugmentation(IfArrisExtensionAug.class,
                    new IfArrisExtensionAugBuilder()
                            .setLinkAggregate(Short.valueOf("0"))
                            .build())
            .build();

    @Test
    void testParseLinkAggregate() {
        ConfigBuilder builder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("ethernet 6/3", SH_INT_CONFIG_ETHERNET, builder);
        assertEquals(EXPECTED_CONFIG, builder.build());
    }
}
