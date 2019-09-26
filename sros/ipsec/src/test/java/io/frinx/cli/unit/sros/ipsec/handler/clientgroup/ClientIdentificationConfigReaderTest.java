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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.client.identification.client.identification.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.client.identification.client.identification.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientIdentificationConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private ClientIdentificationConfigReader target;

    private static final String GROUP_NAME = "Tunnel-Group-001";
    private static final String CLIENT_ID = "100";
    private static final ClientGroupKey CLIENT_GROUP_KEY = new ClientGroupKey(GROUP_NAME);
    private static final ClientKey CLIENT_KEY = new ClientKey(CLIENT_ID);
    private static final InstanceIdentifier<Config> IID =
        IidUtils.createIid(IIDs.IP_CL_CL_CL_CL_CL_CONFIG, CLIENT_GROUP_KEY, CLIENT_KEY);

    private static final String SH_RUN = "/configure\n"
        + "ipsec client-db \"Tunnel-Group-001\" client 100 client-identification\n"
        + "info | match \"^ {24}[^ ]\" expression\n"
        + "exit all";

    private static final String SH_RUN_OUTPUT =
        "                        idi string-type fqdn string-value \"example1.com\"\n"
        + "                        peer-ip-prefix 2400:2000:2400:2650::/64";

    private static final String SH_RUN_EMPTY_OUTPUT = "";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ClientIdentificationConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_OUTPUT).when(target)
        .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getIdiHost().getDomainName().getValue(),
            CoreMatchers.equalTo("example1.com"));
        Assert.assertThat(builder.getPeerPrefix().getIpv6Prefix().getValue(),
            CoreMatchers.equalTo("2400:2000:2400:2650::/64"));
    }

    @Test
    public void testReadCurrentAttributesEmpty() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_EMPTY_OUTPUT).when(target)
        .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getIdiHost(), CoreMatchers.nullValue());
        Assert.assertThat(builder.getPeerPrefix(), CoreMatchers.nullValue());
    }
}
