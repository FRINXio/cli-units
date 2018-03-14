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

package io.frinx.cli.iosxr.ospf.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;

public class GlobalConfigWriterTest implements CliFormatter{

    private static final String WRITE_CURR_ATTR = "router ospf procName\n" +
            "exit";

    private static final String WRITE_CURR_ATTR_D = "no router ospf procName\n" +
            "exit";

    @Test
    public void test() {
        Assert.assertEquals(WRITE_CURR_ATTR, fT(GlobalConfigWriter.WRITE_CURR_ATTR,
                "procName", "procName"));

        Assert.assertEquals(WRITE_CURR_ATTR_D, fT(GlobalConfigWriter.WRITE_CURR_ATTR,
                "procName", "procName",
                "delete", true));
    }
}