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

package io.frinx.cli.unit.sros.ipsec.handler.clientgroup;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.ipsec.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.client.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.client.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private ClientConfigReader target;

    private static final String GROUP_NAME = "Tunnel-Group-001";
    private static final String CLIENT_ID = "100";
    private static final ClientGroupKey CLIENT_GROUP_KEY = new ClientGroupKey(GROUP_NAME);
    private static final ClientKey CLIENT_KEY = new ClientKey(CLIENT_ID);
    private static final InstanceIdentifier<Config> IID =
        IidUtils.createIid(IIDs.IP_CL_CL_CL_CL_CONFIG, CLIENT_GROUP_KEY, CLIENT_KEY);

    private static final String SH_RUN = "/configure\n"
        + "ipsec client-db \"Tunnel-Group-001\" client 100\n"
        + "info | match \"^ {20}[^ ]\" expression\n"
        + "exit all";

    private static final String SH_RUN_ENABLED_OUTPUT = "                    no shutdown\n"
        + "                    private-interface \"PRIVATE-INTERFACE-001-001\"\n"
        + "                    private-service 200\n"
        + "                    tunnel-template 300\n"
        + "                    client-identification\n"
        + "                    exit";

    private static final String SH_RUN_DISABLED_OUTPUT = "                    shutdown\n"
        + "                    private-interface \"PRIVATE-INTERFACE-001-001\"\n"
        + "                    private-service 200\n"
        + "                    tunnel-template 300\n"
        + "                    client-identification\n"
        + "                    exit";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ClientConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributesEnabled() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_ENABLED_OUTPUT).when(target)
        .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getClientId(), CoreMatchers.sameInstance(CLIENT_ID));
        Assert.assertThat(builder.isEnabled(), CoreMatchers.is(Boolean.TRUE));
    }

    @Test
    public void testReadCurrentAttributesDisabled() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_DISABLED_OUTPUT).when(target)
        .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getClientId(), CoreMatchers.sameInstance(CLIENT_ID));
        Assert.assertThat(builder.isEnabled(), CoreMatchers.is(Boolean.FALSE));
    }
}
