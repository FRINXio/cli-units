/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.system.handler.ntp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.cisco.system.ntp.access.group.access.group.ConfigBuilder;

public class NtpAccessGroupConfigReaderTest {

    static final String OUTPUT = "ntp access-group peer 99\n"
            + "ntp access-group serve 66\n"
            + "ntp access-group serve-only 66\n"
            + "ntp access-group query-only 66";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testNtpAcl() {
        NtpAccessGroupConfigReader.parseConfig(OUTPUT,  configBuilder);
        Assert.assertEquals("99", configBuilder.getPeer());
        Assert.assertEquals("66", configBuilder.getServe());
        Assert.assertEquals("66", configBuilder.getQueryOnly());
        Assert.assertEquals("66", configBuilder.getServeOnly());
    }
}
