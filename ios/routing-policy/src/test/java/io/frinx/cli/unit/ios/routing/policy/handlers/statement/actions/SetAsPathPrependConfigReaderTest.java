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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder;

class SetAsPathPrependConfigReaderTest {

    @Test
    void testZeroRepeats() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SetAsPathPrependConfigReader.parseConfig("BLANK", "10", BgpActionsConfigReaderTest.OUTPUT, configBuilder);
        assertNull(configBuilder.getAsn());
        assertNull(configBuilder.getRepeatN());
    }

    @Test
    void parseTwoRepeat() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SetAsPathPrependConfigReader.parseConfig("ASN", "10", BgpActionsConfigReaderTest.OUTPUT, configBuilder);
        assertEquals("65222", configBuilder.getAsn().getValue().toString());
        assertEquals("2", configBuilder.getRepeatN().toString());
    }

    @Test
    void parseFourRepeats() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SetAsPathPrependConfigReader.parseConfig("ASN", "20", BgpActionsConfigReaderTest.OUTPUT, configBuilder);
        assertEquals("65222", configBuilder.getAsn().getValue().toString());
        assertEquals("4", configBuilder.getRepeatN().toString());
    }

}
