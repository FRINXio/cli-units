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

package io.frinx.cli.unit.iosxr.bgp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborConfigReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;

class NeighborConfigReaderTest {

    private static final String OUTPUT = """
            Fri Feb 23 06:18:50.457 UTC
             neighbor 5.5.5.5
              remote-as 5000
              use neighbor-group nbrgroup1
             neighbor 6.6.6.6
              remote-as 5000
              shutdown
             neighbor 8.8.8.8
              remote-as 65000
              use neighbor-group nbrgroup1
             neighbor 7.7.7.7
              remote-as 65000
              use neighbor-group nbrgroup1
              description test desc
              password encrypted GCHKNJDJSADNKLSAND
              send-community-ebgp
              remove-private-AS
             neighbor 1.2.3.4
              remote-as 1.111
            """;

    @Test
    void test() {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "8.8.8.8");
        assertEquals(65000, builder.getPeerAs().getValue().intValue());
        assertTrue(builder.isEnabled());
        assertEquals("nbrgroup1", builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "5.5.5.5");
        assertEquals(5000, builder.getPeerAs().getValue().intValue());
        assertTrue(builder.isEnabled());
        assertEquals("nbrgroup1", builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "6.6.6.6");
        assertEquals(5000, builder.getPeerAs().getValue().intValue());
        assertFalse(builder.isEnabled());
        assertNull(builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "7.7.7.7");
        assertEquals(65000, builder.getPeerAs().getValue().intValue());
        assertTrue(builder.isEnabled());
        assertEquals("nbrgroup1", builder.getPeerGroup());
        assertEquals("test desc", builder.getDescription());
        assertEquals("Encrypted[GCHKNJDJSADNKLSAND]",
                builder.getAuthPassword().getEncryptedString().getValue());
        assertNotNull(builder.getSendCommunity());
        assertNotNull(builder.getRemovePrivateAs());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "1.2.3.4");
        assertEquals(65647, builder.getPeerAs().getValue().longValue());
    }
}
