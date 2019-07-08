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

package io.frinx.cli.unit.iosxr.qos.handler.classifier;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermKey;

public class TermReaderTest {

    private static String OUTPUT_ANY = "Mon Mar 12 11:46:44.799 UTC\n"
            + "class-map match-any map1\n"
            + " match access-group ipv4 inacl222 \n"
            + " match access-group ipv6 ahojgroup \n"
            + " match precedence ipv4 1 5 \n"
            + " match precedence 5 3 0 1 2 4 6 7 \n"
            + " match precedence ipv6 1 \n"
            + " match qos-group 10 \n";

    private static String OUTPUT_ALL = "Mon Mar 12 11:46:44.799 UTC\n"
            + "class-map match-all map1\n"
            + " match access-group ipv4 inacl222 \n"
            + " match access-group ipv6 ahojgroup \n";

    @Test
    public void testIds() {
        List<TermKey> keys = TermReader.getTermKeys(OUTPUT_ANY);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("1", "2", "3", "4", "5", "6"),
                keys.stream().map(TermKey::getId).collect(Collectors.toList()));

        List<TermKey> keysAll = TermReader.getTermKeys(OUTPUT_ALL);
        Assert.assertFalse(keysAll.isEmpty());
        Assert.assertEquals(Lists.newArrayList("all"),
                keysAll.stream().map(TermKey::getId).collect(Collectors.toList()));
    }
}
