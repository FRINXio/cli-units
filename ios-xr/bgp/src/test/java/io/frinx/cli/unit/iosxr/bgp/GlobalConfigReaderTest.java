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

import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

class GlobalConfigReaderTest {

    private static final String OUTPUT_1 = """
            Thu Feb 22 22:59:47.601 UTC
            router bgp 1 instance inst
            router bgp 65505 instance test
            router bgp 1""";

    private static final String OUTPUT_2 = """
            Thu Feb 22 22:59:47.601 UTC
            router bgp 1.100 instance next
            router bgp 1.169031""";

    @Test
    void testGlobal() {
        ConfigBuilder builder = new ConfigBuilder();
        Optional<AsNumber> op = GlobalConfigReader.parseAs(OUTPUT_1, "inst");
        assertEquals(Long.valueOf(1), op.get().getValue());

        builder = new ConfigBuilder();
        op = GlobalConfigReader.parseDefaultAs(OUTPUT_1);
        assertEquals(Long.valueOf(1), op.get().getValue());

        builder = new ConfigBuilder();
        op = GlobalConfigReader.parseAs(OUTPUT_1, "test");
        assertEquals(Long.valueOf(65505), op.get().getValue());

        builder = new ConfigBuilder();
        op = GlobalConfigReader.parseAs(OUTPUT_2, "next");
        assertEquals(Long.valueOf(65636), op.get().getValue());

        builder = new ConfigBuilder();
        op = GlobalConfigReader.parseDefaultAs(OUTPUT_2);
        assertEquals(Long.valueOf(234567), op.get().getValue());
    }
}
