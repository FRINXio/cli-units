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
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.Client;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientReaderTest {
    private static final String SH_RUN_LIST = "/configure\n"
        + "ipsec client-db \"Tunnel-Group-001\"\n"
        + "info | match ^[[:space:]]{16}client expression\n"
        + "exit all";
    private static final String SH_RUN_LIST_OUTPUT = "            client 10 create\n"
        + "            client 100 create\n"
        + "            client 1000 create\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private ClientReader target;

    private static final String GROUP_NAME = "Tunnel-Group-001";
    private static final String CLIENT_ID = "100";
    private static final ClientGroupKey CLIENT_GROUP_KEY = new ClientGroupKey(GROUP_NAME);
    private static final ClientKey CLIENT_KEY = new ClientKey(CLIENT_ID);
    private static final InstanceIdentifier<Client> IID_FOR_LIST =
        IidUtils.createIid(IIDs.IP_CL_CL_CLIENTS, CLIENT_GROUP_KEY)
            .child(Client.class);
    private static final InstanceIdentifier<Client> IID =
        IidUtils.createIid(IIDs.IP_CL_CL_CL_CLIENT, CLIENT_GROUP_KEY, CLIENT_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ClientReader(cli));
    }

    @Test
    public void testGetAllIds() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_LIST_OUTPUT).when(target)
            .blockingRead(SH_RUN_LIST, cli, IID_FOR_LIST, ctx);

        List<ClientKey> result = target.getAllIds(IID_FOR_LIST, ctx);

        Assert.assertThat(result, Matchers.containsInAnyOrder(
            new ClientKey("10"),
            new ClientKey("100"),
            new ClientKey("1000")));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        final ClientBuilder builder = new ClientBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getClientId(), CoreMatchers.equalTo(CLIENT_ID));
    }
}
