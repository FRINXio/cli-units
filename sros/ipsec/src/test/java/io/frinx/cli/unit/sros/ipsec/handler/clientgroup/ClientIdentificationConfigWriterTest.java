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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.ipsec.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.client.identification.client.identification.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.client.identification.client.identification.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.DomainName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientIdentificationConfigWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private ClientIdentificationConfigWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String WRITE_INPUT = "/configure\n"
        + "ipsec\n"
        + "client-db \"Tunnel-Group-001\"\n"
        + "client 100\n"
        + "client-identification\n"
        + "idi string-type fqdn string-value \"example.com\"\n"
        + "peer-ip-prefix 2400:2000:2400:2650::/64\n"
        + "exit all\n";
    private static final String UPDATE_INPUT = "/configure\n"
        + "ipsec\n"
        + "client-db \"Tunnel-Group-001\"\n"
        + "client 100\n"
        + "client-identification\n"
        + "no idi\n"
        + "no peer-ip-prefix\n"
        + "exit all\n";
    private static final String DELETE_INPUT = "/configure\n"
        + "ipsec\n"
        + "client-db \"Tunnel-Group-001\"\n"
        + "client 100\n"
        + "client-identification\n"
        + "no idi\n"
        + "no peer-ip-prefix\n"
        + "exit all\n";

    private static final String GROUP_NAME = "Tunnel-Group-001";
    private static final String CLIENT_ID = "100";
    private static final String IDI_FQDN = "example.com";
    private static final String IPV6_PREFIX = "2400:2000:2400:2650::/64";
    private static final ClientGroupKey CLIENT_GROUP_KEY = new ClientGroupKey(GROUP_NAME);
    private static final ClientKey CLIENT_KEY = new ClientKey(CLIENT_ID);
    private static final InstanceIdentifier<Config> IID =
        IidUtils.createIid(IIDs.IP_CL_CL_CL_CL_CL_CONFIG, CLIENT_GROUP_KEY, CLIENT_KEY);

    private static final Config DATA = new ConfigBuilder()
        .setIdiHost(new Host(new DomainName(IDI_FQDN)))
        .setPeerPrefix(new IpPrefix(new Ipv6Prefix(IPV6_PREFIX)))
        .build();

    private static final Config DATA_EMPTY = new ConfigBuilder()
        .build();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new ClientIdentificationConfigWriter(cli);
    }

    @Test
    public void testWriteCurrentAttributes() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testUpdateCurrentAttributes() throws WriteFailedException {
        target.updateCurrentAttributes(IID, DATA, DATA_EMPTY, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes() throws WriteFailedException {
        target.deleteCurrentAttributes(IID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
