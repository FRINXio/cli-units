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

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.ipsec.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.tunneling.sa.tunneling.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.tunneling.sa.tunneling.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class TunnelingConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private TunnelingConfigReader target;

    private static final String GROUP_NAME = "Tunnel-Group-001";
    private static final String CLIENT_ID = "100";
    private static final ClientGroupKey CLIENT_GROUP_KEY = new ClientGroupKey(GROUP_NAME);
    private static final ClientKey CLIENT_KEY = new ClientKey(CLIENT_ID);
    private static final InstanceIdentifier<Config> IID =
        IidUtils.createIid(IIDs.IP_CL_CL_CL_CL_SE_TU_CONFIG, CLIENT_GROUP_KEY, CLIENT_KEY);

    private static final String SH_RUN = """
            /configure
            ipsec client-db "Tunnel-Group-001" client 100
            info | match "^ {20}[^ ]" expression
            exit all""";

    private static final String SH_RUN_OUTPUT = """
                                shutdown
                                private-interface "PRIVATE-INTERFACE-001"
                                private-service 200
                                tunnel-template 300
                                client-identification
                                exit\
            """;

    private static final String SH_RUN_EMPTY_OUTPUT = "";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new TunnelingConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_OUTPUT).when(target)
        .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        assertThat(builder.getPrivateInterfaceName(),
            CoreMatchers.equalTo("PRIVATE-INTERFACE-001"));

        assertThat(builder.getPrivateServiceId(),
            CoreMatchers.equalTo(Long.valueOf(200L)));

        assertThat(builder.getTunnelTemplateId(),
            CoreMatchers.equalTo(Long.valueOf(300L)));
    }

    @Test
    void testReadCurrentAttributesEmpty() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_EMPTY_OUTPUT).when(target)
        .blockingRead(SH_RUN, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        assertThat(builder.getPrivateInterfaceName(), CoreMatchers.nullValue());
        assertThat(builder.getPrivateServiceId(), CoreMatchers.nullValue());
        assertThat(builder.getTunnelTemplateId(), CoreMatchers.nullValue());
    }
}
