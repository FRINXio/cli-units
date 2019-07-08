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

package io.frinx.cli.unit.iosxr.routing.policy.handler.policy;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;

public class PolicyWriterTest implements CliFormatter {

    @Test
    public void testTemplate() throws Exception {
        Assert.assertEquals("route-policy abcd\n"
                        + "end-policy",
                fT(PolicyConfigWriter.TEMPLATE,
                        "config", new PolicyDefinitionBuilder()
                                .setName("abcd")
                                .build()));

        Assert.assertEquals("no route-policy abcd\n",
                fT(PolicyConfigWriter.TEMPLATE,
                        "config", new PolicyDefinitionBuilder()
                                .setName("abcd")
                                .build(),
                        "delete", true));
    }
}