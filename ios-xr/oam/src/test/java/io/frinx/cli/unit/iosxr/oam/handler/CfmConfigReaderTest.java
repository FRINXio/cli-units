/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.oam.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.oam.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmConfigReaderTest {
    private static final String SH_RUN = "show running-config ethernet cfm";
    private static final String SH_RUN_OUTPUT = "ethernet cfm\n"
        + " domain NBA level 3\n"
        + "  service 38-013S0101 down-meps\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private CfmConfigReader target;

    private static final InstanceIdentifier<Config> IID = IIDs.OA_CF_CONFIG;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new CfmConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_OUTPUT).when(target)
            .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.isEnabled(), CoreMatchers.is(Boolean.TRUE));
    }
}
