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

package io.frinx.cli.unit.ios.snmp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.ios.snmp.Util;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.Communities;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.Community;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.CommunityBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.CommunityKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.community.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CommunityWriterTest {

    private static final String WRITE = """
            configure terminal
            snmp-server community Bar view SnmpReadAccess ro
            end
            """;

    private static final String UPDATE = """
            configure terminal
            snmp-server community Bar view SnmpWriteAccess rw 70
            end
            """;

    private static final String DELETE = """
            configure terminal
            no snmp-server community Bar
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CommunityWriter writer;
    private Community data;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Communities.class)
            .child(Community.class, new CommunityKey("Bar"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CommunityWriter(cli);
        data = getCommunity("Bar", "SnmpReadAccess", "RO", null);
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        final Community newData = getCommunity("Bar", "SnmpWriteAccess", "RW", "70");
        writer.updateCurrentAttributes(iid, data, newData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE, response.getValue().getContent());
    }

    private Community getCommunity(final String name, final String view, final String access, final String accessList) {
        return new CommunityBuilder()
                .setKey(new CommunityKey(name))
                .setName(name)
                .setConfig(new ConfigBuilder()
                        .setName(name)
                        .setView(view)
                        .setAccess(Util.getAccessType(access))
                        .setAccessList(accessList)
                        .build())
                .build();
    }

}