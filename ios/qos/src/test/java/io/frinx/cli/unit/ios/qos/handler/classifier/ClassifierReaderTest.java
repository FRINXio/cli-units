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

package io.frinx.cli.unit.ios.qos.handler.classifier;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;

public class ClassifierReaderTest {

    private static final String MAP_KEYS = " Class Map match-all map1 (id 1)\n"
            + " Class Map match-any map2 (id 2)\n";

    private static final String DEFAULT_KEYS = "  Policy Map plmap1\n"
            + "  Policy Map plmap2\n"
            + "  Policy Map plmap3\n";

    @Test
    public void testMapKeys() {
        final List<ClassifierKey> keys = ClassifierReader.getClassifierMapKeys(MAP_KEYS);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("map1", "map2"),
                keys.stream().map(ClassifierKey::getName).collect(Collectors.toList()));
    }

    @Test
    public void testDefaultKeys() {
        final List<ClassifierKey> keys = ClassifierReader.getClassifierDefaultKeys(DEFAULT_KEYS);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("plmap1-default", "plmap2-default", "plmap3-default"),
                keys.stream().map(ClassifierKey::getName).collect(Collectors.toList()));
    }

}