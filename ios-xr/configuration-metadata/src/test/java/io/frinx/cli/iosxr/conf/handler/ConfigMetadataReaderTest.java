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

package io.frinx.cli.iosxr.conf.handler;

import static org.junit.Assert.assertFalse;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class ConfigMetadataReaderTest {

    @Test
    public void testParse() throws Exception {

        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint("Tue Apr  3 14:03:27.250 UTC\n1"
                + "    1000007068            cisco     vty1:node0_0_CPU0   CLI         Tue Apr  3 13:03:39 2018\n");
        Assert.assertEquals(date1.get()
                .toString(), "Tue Apr  3 13:03:39 2018");

        Optional<String> date2 = (ConfigMetadataReader.getLastConfigurationFingerprint(""));
        assertFalse(date2.isPresent());
    }
}