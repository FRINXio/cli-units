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

package io.frinx.cli.unit.huawei.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ComponentReaderTest {


    static String OUTPUT = "";

    static {
        try {
            OUTPUT = new String(
                    ByteStreams.toByteArray(ComponentStateReader.class.getClassLoader()
                        .getResourceAsStream("elabel.txt")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void testAllIds() throws Exception {
        assertEquals(94, ComponentReader.getComponents(OUTPUT).size());
    }
}