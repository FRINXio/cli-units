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

package io.frinx.cli.unit.saos.ifc.handler.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;

class AggregateConfigReaderTest {

    private static final String OUTPUT = """
            aggregation add agg LP01 port 1
            aggregation add agg LM01W port 2
            aggregation add agg LM01E port 3
            aggregation add agg LS01E port 4
            """;

    @Test
    void parseConfigTest() {
        Config1Builder builder = new Config1Builder();
        AggregateConfigReader reader = new AggregateConfigReader(Mockito.mock(Cli.class));

        reader.parseConfig(OUTPUT, builder, "3");

        assertEquals("LM01E", builder.getAggregateId());
    }
}
