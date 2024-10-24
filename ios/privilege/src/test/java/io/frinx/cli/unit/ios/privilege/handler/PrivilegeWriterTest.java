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

package io.frinx.cli.unit.ios.privilege.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.LevelId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.Levels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.LevelsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.Level;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.LevelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.LevelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.level.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.top.Privilege;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.top.PrivilegeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class PrivilegeWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            privilege exec level 0 traceroute
            privilege exec level 0 ping
            privilege exec level 1 show port-security
            privilege exec level 1 show running-config
            privilege exec level 1 show
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            privilege exec reset traceroute
            privilege exec reset ping
            privilege exec reset show port-security
            privilege exec reset show running-config
            privilege exec reset show
            end
            """;

    private static final Levels LEVELS = new LevelsBuilder()
            .setLevel(Arrays.asList(
                    getLevel("0", "traceroute", "ping"),
                    getLevel("1", "show port-security", "show running-config", "show")
            ))
            .build();

    private static final Privilege PRIVILEGE = new PrivilegeBuilder()
            .setLevels(LEVELS)
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private PrivilegeWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Levels.class);

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new PrivilegeWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, PRIVILEGE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, PRIVILEGE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    void isConfigValid() {
        final Levels invalidConfig = new LevelsBuilder()
                .setLevel(Collections.singletonList(
                        getLevel("0", "show port-security", "show running-config view", "show")
                ))
                .build();

        assertTrue(PrivilegeWriter.getCommandsWithoutParent(invalidConfig).size() > 0);
        assertFalse(PrivilegeWriter.getCommandsWithoutParent(LEVELS).size() > 0);
    }

    private static Level getLevel(final String id, final String ...keywords) {
        final LevelBuilder levelBuilder = new LevelBuilder();
        levelBuilder.setId(LevelId.getDefaultInstance(id));
        levelBuilder.setKey(new LevelKey(levelBuilder.getId()));

        final ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setId(levelBuilder.getId());
        configBuilder.setCommands(Arrays.asList(keywords));

        levelBuilder.setConfig(configBuilder.build());
        return levelBuilder.build();
    }

}