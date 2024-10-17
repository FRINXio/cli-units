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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.RfChannelsBuilder;

class CableRfChannelReaderTest {

    @Test
    void test() {
        final RfChannelsBuilder builder = new RfChannelsBuilder();
        CableRfChannelReader.parseCable(CableInterfaceConfigReaderTest.OUTPUT, builder);
        assertEquals("8-15", builder.getChannelList());
        assertEquals("1", builder.getBandwidthPercent());
    }
}
