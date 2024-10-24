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

package io.frinx.cli.unit.junos.unit.acl.handler;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class AclSetConfigWriterTest {

    private static final String FILTER_NAME = "FILTER-001";
    private static final Class<? extends ACLTYPE> NET_TYPE = ACLIPV4.class;
    private static final InstanceIdentifier<AclSet> IID = IIDs.ACL
            .child(AclSets.class)
            .child(AclSet.class, new AclSetKey(FILTER_NAME, NET_TYPE));
    private static final AclSet DATA = new AclSetBuilder()
            .setName(FILTER_NAME)
            .setType(NET_TYPE)
            .build();

    private static final Class<? extends ACLTYPE> NET_TYPE6 = ACLIPV6.class;
    private static final InstanceIdentifier<AclSet> IID6 = IIDs.ACL
            .child(AclSets.class)
            .child(AclSet.class, new AclSetKey(FILTER_NAME, NET_TYPE6));
    private static final AclSet DATA6 = new AclSetBuilder()
            .setName(FILTER_NAME)
            .setType(NET_TYPE)
            .build();

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private AclSetConfigWriter target;

    private ArgumentCaptor<Command> commands;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new AclSetConfigWriter(cli);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(Optional.empty()).when(writeContext).readBefore(Mockito.any());
        commands = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    void testWriteCurrentAttributes() throws Exception {
        target.writeCurrentAttributes(IID, DATA, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
                "set firewall family inet filter FILTER-001\n"));
    }

    @Test
    void testWriteCurrentAttributesWithIpv6() throws Exception {
        target.writeCurrentAttributes(IID6, DATA6, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
                "set firewall family inet6 filter FILTER-001\n"));
    }

    @Test
    void testDeleteCurrentAttributes() throws Exception {
        target.deleteCurrentAttributes(IID, DATA, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
                "delete firewall family inet filter FILTER-001\n"));
    }

    @Test
    void testDeleteCurrentAttributesWithIpv6() throws Exception {
        target.deleteCurrentAttributes(IID6, DATA6, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
                "delete firewall family inet6 filter FILTER-001\n"));
    }
}