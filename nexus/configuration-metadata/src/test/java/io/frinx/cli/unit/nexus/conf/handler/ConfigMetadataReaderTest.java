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

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class ConfigMetadataReaderTest {

    private static final String OUTPUT =
            "2019 Jan 21 15:47:11 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on "
                   + "192.168.1.43@pts/3\n"
            + "2019 Jan 21 15:48:25 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on "
                   + "192.168.1.43@pts/3\n"
            + "2019 Jan 21 16:18:27 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on "
                   + "192.168.1.43@pts/3\n"
            + "2019 Jan 22 01:11:21 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on vsh.bin.19306\n"
            + "2019 Jan 22 12:50:37 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on "
                    + "192.168.1.40@pts/0\n";

    private static final String OUTPUT2 = "*Apr 23 11:20:43.936: %SYS-5-CONFIG_I: Configured from console by cisco on"
            + " vty0 (192.168.1.41)\n"
            + "2019 Jan 22 01:11:21 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from vty by admin on vsh.bin.19306\n"
            + "2019 Jan 22 12:50:37 nxos2 %VSHD-5-VSHD_SYSLOG_CONFIG_I: Configured from console\n"
            + "Apr 17 11:20:45.457: %SYS-5-CONFIG_I: Configured from console by console\n"
            + "*Apr 23 11:24:18.169: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + " CMD: 'show history all | include Configured from | LINE 2' 09:03:05 UTC Tue Apr 24 2018\n"
            + "CMD: 'show history all | include \"Configured from\"' 09:06:04 UTC Tue Apr 24 2018\n"
            + "*Apr 23 14:00:01.896: %SYS-5-CONFIG_I: Configured from console by console\n"
            + "CMD: 'show history all | include Configured from' 10:49:48 UTC Tue Apr 24 2018\n";

    private static final String EXPECTED = "2019 Jan 22 12:50:37";

    @Test
    public void testParse() {
        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT);
        Assert.assertTrue(date1.isPresent());
        Assert.assertEquals(EXPECTED, date1.get());

        Optional<String> date2 = ConfigMetadataReader.getLastConfigurationFingerprint("");
        Assert.assertFalse(date2.isPresent());
    }

    @Test
    public void testParse2() {
        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT2);
        Assert.assertTrue(date1.isPresent());
        Assert.assertEquals(EXPECTED, date1.get());

        Optional<String> date2 = ConfigMetadataReader.getLastConfigurationFingerprint("");
        Assert.assertFalse(date2.isPresent());
    }
}