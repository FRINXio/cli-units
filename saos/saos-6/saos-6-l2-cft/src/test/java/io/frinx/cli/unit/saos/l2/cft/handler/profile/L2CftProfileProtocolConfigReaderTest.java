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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Disposition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.protocol.ConfigBuilder;

class L2CftProfileProtocolConfigReaderTest {

    @Test
    void parseConfig() {
        ConfigBuilder builder = new ConfigBuilder();
        L2CftProfileProtocolConfigReader reader = new L2CftProfileProtocolConfigReader(Mockito.mock(Cli.class));

        reader.parseConfig(
                "l2-cft protocol add profile CTB-TUNNEL ctrl-protocol cisco-cdp untagged-disposition discard",
                "cisco-cdp", builder);

        assertEquals(Name.CiscoCdp, builder.getName());
        assertEquals(Disposition.Discard, builder.getDisposition());
    }
}
