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

package io.frinx.cli.iosxr.routing.policy.handler.aspath;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.utils.CliFormatter;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.as.path.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.as.path.set.ConfigBuilder;

public class AsPathSetConfigWriterTest implements CliFormatter {

    static final Config CFG_1 = new ConfigBuilder()
            .setAsPathSetName("test")
            .setAsPathSetMember(Lists.newArrayList(
                    "neighbor-is '1.1'",
                    "passes-through '54'",
                    "originates-from '3243'",
                    "length eq 444",
                    "unique-length eq 44",
                    "ios-regex '*'"))
            .build();

    static final String OUTPUT_1 = "as-path-set test\n"
            + "neighbor-is '1.1',\n"
            + "passes-through '54',\n"
            + "originates-from '3243',\n"
            + "length eq 444,\n"
            + "unique-length eq 44,\n"
            + "ios-regex '*'\n"
            + "end-set";

    static final Config CFG_2 = new ConfigBuilder()
            .setAsPathSetName("test")
            .setAsPathSetMember(Collections.emptyList())
            .build();

    static final String OUTPUT_2 = "as-path-set test\n"
            + "\n"
            + "end-set";

    @Test
    public void testWrite() throws Exception {
        String output = fT(AsPathSetConfigWriter.TEMPLATE, "config", CFG_1);
        assertEquals(OUTPUT_1, output);

        output = fT(AsPathSetConfigWriter.TEMPLATE, "config", CFG_2);
        assertEquals(OUTPUT_2, output);
    }
}