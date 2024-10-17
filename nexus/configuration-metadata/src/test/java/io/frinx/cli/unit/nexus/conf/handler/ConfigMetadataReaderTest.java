/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.nexus.conf.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConfigMetadataReaderTest {

    private static final String OUTPUT = """
            2019 Jan 21 15:47:11 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on 192.168.1.43@pts/3
            2019 Jan 21 15:48:25 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on 192.168.1.43@pts/3
            2019 Jan 21 16:18:27 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on 192.168.1.43@pts/3
            2019 Jan 22 01:11:21 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on vsh.bin.19306
            2019 Jan 22 12:50:37 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on 192.168.1.40@pts/0
            """;

    private static final String OUTPUT2 = """
            *Apr 23 11:20:43.936: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)
            2019 Jan 22 01:11:21 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on vsh.bin.19306
            2019 Jan 22 12:50:37 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from console
            Apr 17 11:20:45.457: %SYS-5-CONFIG_I: Configured from console by console
            *Apr 23 11:24:18.169: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)
             CMD: 'show history all | include Configured from | LINE 2' 09:03:05 UTC Tue Apr 24 2018
            CMD: 'show history all | include "Configured from"' 09:06:04 UTC Tue Apr 24 2018
            *Apr 23 14:00:01.896: %SYS-5-CONFIG_I: Configured from console by console
            CMD: 'show history all | include Configured from' 10:49:48 UTC Tue Apr 24 2018
            """;

    private static final String EXPECTED = "2019 Jan 22 12:50:37";

    @Test
    void testParse() {
        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT);
        assertTrue(date1.isPresent());
        assertEquals(EXPECTED, date1.get());

        Optional<String> date2 = ConfigMetadataReader.getLastConfigurationFingerprint("");
        assertFalse(date2.isPresent());
    }

    @Test
    void testParse2() {
        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT2);
        assertTrue(date1.isPresent());
        assertEquals(EXPECTED, date1.get());

        Optional<String> date2 = ConfigMetadataReader.getLastConfigurationFingerprint("");
        assertFalse(date2.isPresent());
    }
}