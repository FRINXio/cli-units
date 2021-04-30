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

package io.frinx.cli.unit.iosxe.bfdt.handler;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplateKey;

public class BfdTemplateReaderTest {
    private static final String SH_BFD_TEMPLATE_RUN = "bfd-template single-hop bfd1\n"
            + "bfd-template single-hop test\n"
            + " interval min-tx 900 min-rx 7 multiplier 3\n"
            + "bfd-template single-hop test2\n"
            + " interval min-tx 444 min-rx 555 multiplier 3";

    private static final List<BfdTemplateKey> BFD_TEMPLATES =
            Arrays.asList(new BfdTemplateKey("bfd1"), new BfdTemplateKey("test"),
                    new BfdTemplateKey("test2"));

    @Test
    public void testParseBfdTemplateNames() {
        Assert.assertEquals(BfdTemplateReader.parseBfdTemplateNames(SH_BFD_TEMPLATE_RUN), BFD_TEMPLATES);
    }
}
