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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientGroupReaderTest {
    private static final String SH_RUN_LIST = "/configure\n"
        + "ipsec\n"
        + "info | match ^[[:space:]]+client-db expression\n"
        + "exit all";
    private static final String SH_RUN_LIST_OUTPUT = "        client-db \"IPSEC-CLIENT-DB-001\" create\n"
        + "        client-db \"IPSEC-CLIENT-DB-002\" create";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private ClientGroupReader target;

    private static final String GROUP_NAME = "Tunnel-Group-001";
    private static final ClientGroupKey CLIENT_GROUP_KEY = new ClientGroupKey(GROUP_NAME);
    private static final InstanceIdentifier<ClientGroup> IID_FOR_LIST = IIDs.IP_CL_CLIENTGROUP;
    private static final InstanceIdentifier<ClientGroup> IID =
        IidUtils.createIid(IIDs.IP_CL_CLIENTGROUP, CLIENT_GROUP_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ClientGroupReader(cli));
    }

    @Test
    public void testGetAllIds() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_LIST_OUTPUT).when(target)
            .blockingRead(SH_RUN_LIST, cli, IID_FOR_LIST, ctx);

        List<ClientGroupKey> result = target.getAllIds(IID_FOR_LIST, ctx);

        Assert.assertThat(result, Matchers.containsInAnyOrder(
            new ClientGroupKey("IPSEC-CLIENT-DB-001"),
            new ClientGroupKey("IPSEC-CLIENT-DB-002")));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        final ClientGroupBuilder builder = new ClientGroupBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getGroupName(), CoreMatchers.equalTo(GROUP_NAME));
    }
}
