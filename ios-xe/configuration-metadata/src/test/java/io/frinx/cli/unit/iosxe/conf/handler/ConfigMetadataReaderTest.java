/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.conf.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConfigMetadataReaderTest {

    private static final String OUTPUT = "! Last configuration change at 09:16:10 CET Thu Aug 25 2022";

    private static final String EXPECTED = "09:16:10 CET Thu Aug 25 2022";

    @Test
    void test() {
        final Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT);
        assertTrue(date1.isPresent());
        assertEquals(EXPECTED, date1.get());

        final Optional<String> date2 = (ConfigMetadataReader.getLastConfigurationFingerprint(""));
        assertFalse(date2.isPresent());
    }

}