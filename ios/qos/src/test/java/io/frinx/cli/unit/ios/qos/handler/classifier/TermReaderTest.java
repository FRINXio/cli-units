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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermKey;

public class TermReaderTest {

    private static String OUTPUT_ANY = " Class Map match-any map1 (id 4)\n"
            + "   Match qos-group 6\n"
            + "   Match cos  5 \n";

    private static String OUTPUT_ALL = " Class Map match-all map2 (id 6)\n"
            + "   Match ip  dscp ef (46)\n"
            + "   Match ip  dscp af31 (26)\n"
            + "   Match ip  dscp cs3 (24)\n";

    @Test
    public void testAny() {
        List<TermKey> keys = TermReader.getTermKeys(OUTPUT_ANY);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("1", "2"),
                keys.stream().map(TermKey::getId).collect(Collectors.toList()));
    }

    @Test
    public void testAll() {
        List<TermKey> keysAll = TermReader.getTermKeys(OUTPUT_ALL);
        Assert.assertFalse(keysAll.isEmpty());
        Assert.assertEquals(Lists.newArrayList("all"),
                keysAll.stream().map(TermKey::getId).collect(Collectors.toList()));
    }

}