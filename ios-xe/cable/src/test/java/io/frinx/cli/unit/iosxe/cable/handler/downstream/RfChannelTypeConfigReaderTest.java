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
package io.frinx.cli.unit.iosxe.cable.handler.downstream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.Config.Mode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.Config.RfType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.ConfigBuilder;


class RfChannelTypeConfigReaderTest {


    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    void testCoreInterface() {
        RfChannelTypeConfigReader.parseConfig(CableDownstreamConfigReaderTest.OUTPUT, configBuilder, "32 33");
        assertEquals(Mode.Async, configBuilder.getMode());
        assertEquals(RfType.Video, configBuilder.getRfType());
    }

}