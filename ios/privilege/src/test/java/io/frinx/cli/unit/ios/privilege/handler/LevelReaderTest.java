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

package io.frinx.cli.unit.ios.privilege.handler;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.LevelKey;

public class LevelReaderTest {

    private static final String OUTPUT = "privilege exec level 0 traceroute\n"
            + "privilege exec level 0 ping\n"
            + "privilege exec level 1 show port-security\n"
            + "privilege exec level 15 show running-config\n"
            + "privilege exec level 1 show\n"
            + "privilege exec level 1 clear\n"
            + "privilege exec level 7 logging";

    @Test
    public void testKeys() {
        final List<LevelKey> keys = LevelReader.getLevelKeys(OUTPUT);
        Assert.assertEquals(4, keys.size());
        Assert.assertEquals(Short.valueOf("0"), keys.get(0).getId().getValue());
        Assert.assertEquals(Short.valueOf("1"), keys.get(1).getId().getValue());
        Assert.assertEquals(Short.valueOf("15"), keys.get(2).getId().getValue());
        Assert.assertEquals(Short.valueOf("7"), keys.get(3).getId().getValue());
    }

}