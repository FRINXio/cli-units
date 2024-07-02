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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.SaosVsExtension.EncapCosPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAug;

class L2VSIConfigReaderTest {

    private static final String OUTPUT = """
            virtual-switch ethernet create vs vsVLAN399918 encap-fixed-dot1dpri 1 vc vc2 description EthernetCFMtest
            virtual-switch ethernet add vs vsVLAN399918 port 1
            l2-cft tagged-pvst-l2pt enable vs vsVLAN399918
            """;

    private static final String OUTPUT_WITH_DESCTIPTION_AND_CONFIG =
            "virtual-switch ethernet create vs vsVLAN399918 encap-fixed-dot1dpri 1 vc vc2 "
                    + "description EthernetCFMtest and something else\n";

    private static final String OUTPUT_WITH_MULTIWORD_DESCRIPTION =
            "virtual-switch ethernet create vs vsVLAN399918 encap-fixed-dot1dpri 1 vc vc2 "
            + "description \"Port EthernetCFMtest\"\n";

    private static final String OUTPUT_WITH_MULTIWORD_DESCRIPTION_AND_CONFIG =
            "virtual-switch ethernet create vs vsVLAN399918 encap-fixed-dot1dpri 1 vc vc2 "
                    + "description \"Port EthernetCFMtest\" and something else\n";

    @Test
    void parseConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();
        L2VSIConfigReader reader = new L2VSIConfigReader(Mockito.mock(Cli.class));

        reader.parseConfig(OUTPUT, builder, "vsVLAN399918");

        assertEquals("vsVLAN399918", builder.getName());
        assertEquals(L2VSI.class, builder.getType());
        assertEquals(true, builder.isEnabled());
        assertEquals("EthernetCFMtest", builder.getDescription());
        assertEquals(new Short("1"), builder.getAugmentation(VsSaosAug.class).getEncapFixedDot1dpri());
        assertEquals(true, builder.getAugmentation(VsSaosAug.class).isTaggedPvstL2pt());
        assertEquals(EncapCosPolicy.Fixed, builder.getAugmentation(VsSaosAug.class).getEncapCosPolicy());
    }

    @Test
    void parseConfigWithMultiwordDescriptionTest() {
        ConfigBuilder builder = new ConfigBuilder();
        L2VSIConfigReader reader = new L2VSIConfigReader(Mockito.mock(Cli.class));

        reader.parseConfig(OUTPUT_WITH_DESCTIPTION_AND_CONFIG, builder, "vsVLAN399918");
        assertEquals("EthernetCFMtest", builder.getDescription());

        reader.parseConfig(OUTPUT_WITH_MULTIWORD_DESCRIPTION, builder, "vsVLAN399918");
        assertEquals("Port EthernetCFMtest", builder.getDescription());

        reader.parseConfig(OUTPUT_WITH_MULTIWORD_DESCRIPTION_AND_CONFIG, builder, "vsVLAN399918");
        assertEquals("Port EthernetCFMtest", builder.getDescription());
    }
}
