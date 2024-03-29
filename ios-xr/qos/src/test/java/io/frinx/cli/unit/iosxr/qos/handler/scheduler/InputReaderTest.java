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

package io.frinx.cli.unit.iosxr.qos.handler.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InputReaderTest {

    private static final String OUTPUT = """
            Tue Apr  3 08:55:01.362 UTC\r
             class mapAny\r
             class mapAll\r
             class class-default\r
            """;

    @Test
    void testAllIds() {
        assertEquals("mapAny", InputReader.getInputKeys(OUTPUT, 1L)
                .get(0)
                .getId());
        assertEquals("mapAll", InputReader.getInputKeys(OUTPUT, 2L)
                .get(0)
                .getId());
        assertEquals("class-default", InputReader.getInputKeys(OUTPUT, 3L)
                .get(0)
                .getId());
    }
}
