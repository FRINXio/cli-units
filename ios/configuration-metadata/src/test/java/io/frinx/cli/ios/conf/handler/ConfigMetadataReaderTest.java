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

package io.frinx.cli.ios.conf.handler;

import static org.junit.Assert.assertFalse;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class ConfigMetadataReaderTest {

    private static final String OUTPUT = "**Apr 23 11:20:43.936: %SYS-5-CONFIG_I: Configured from console by cisco on"
            + " vty0 (192.168.1.41)\n"
            + "*Apr 23 11:20:44.089: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:20:44.993: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:20:45.457: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:20:45.903: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:23:34.958: %SYS-5-CONFIG_I: Configured from console by cisco on vty1 (192.168.1.41)\n"
            + "*Apr 23 11:24:16.670: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:24:16.829: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:24:17.717: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:24:18.169: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:24:18.617: %SYS-5-CONFIG_I: Configured from console by cisco on vty0 (192.168.1.41)\n"
            + "*Apr 23 11:33:52.247: %SYS-5-CONFIG_I: Configured from console by cisco on vty1 (192.168.1.41)\n"
            + "*Apr 23 14:00:01.896: %SYS-5-CONFIG_I: Configured from console by cisco on vty1 (192.168.1.43)\n"
            + "CMD: 'show history all | include Configured from' 08:58:03 UTC Tue Apr 24 2018\n"
            + "CMD: 'show history all | include Configured from | LINE 2' 09:03:05 UTC Tue Apr 24 2018\n"
            + "CMD: 'show history all | include Configured from |' 09:05:36 UTC Tue Apr 24 2018\n"
            + "CMD: 'show history all | include \"Configured from\"' 09:06:04 UTC Tue Apr 24 2018\n"
            + "CMD: 'show history all | include Configured from' 09:12:48 UTC Tue Apr 24 2018\n"
            + "CMD: 'show history all | include Configured from' 10:49:48 UTC Tue Apr 24 2018\n";

    private static final String EXPECTED = "Apr 23 14:00:01.896";

    @Test
    public void testParse() {

        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(OUTPUT);
        Assert.assertTrue(date1.isPresent());
        Assert.assertEquals(EXPECTED, date1.get());

        Optional<String> date2 = (ConfigMetadataReader.getLastConfigurationFingerprint(""));
        assertFalse(date2.isPresent());
    }
}