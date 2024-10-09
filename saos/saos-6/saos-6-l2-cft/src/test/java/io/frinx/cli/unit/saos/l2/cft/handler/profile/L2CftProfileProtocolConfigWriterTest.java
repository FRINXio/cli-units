/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.l2.cft.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Disposition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.Profile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.ProfileKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.protocol.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class L2CftProfileProtocolConfigWriterTest {

    @Mock
    private Cli cli;

    private final InstanceIdentifier iid = IIDs.L2_PROFILES
            .child(Profile.class, new ProfileKey("TEST"));

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private L2CftProfileProtocolConfigWriter writer;

    private final Config config = new ConfigBuilder()
            .setName(Name.AllBridgesBlock)
            .setDisposition(Disposition.Discard)
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2CftProfileProtocolConfigWriter(cli);
    }

    @Test
    void writeCurrentAttributesTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, config, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals("l2-cft protocol add profile TEST ctrl-protocol all-bridges-block "
                + "untagged-disposition discard", commands.getValue().getContent());
    }

    @Test
    void updateCurrentAttributesTest_01() throws WriteFailedException {
        Config configAfter = new ConfigBuilder()
                .setName(Name.AllBridgesBlock)
                .setDisposition(Disposition.Forward)
                .build();

        writer.updateCurrentAttributes(iid, config, configAfter, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals("l2-cft protocol set profile TEST ctrl-protocol all-bridges-block "
                + "untagged-disposition forward", commands.getValue().getContent());
    }

    @Test
    void updateCurrentAttributesTest_02() throws WriteFailedException {
        Config configAfter = new ConfigBuilder()
                .setName(Name.AllBridgesBlock)
                .setDisposition(Disposition.Discard)
                .build();

        writer.updateCurrentAttributes(iid, config, configAfter, null);

        Mockito.verifyNoInteractions(cli);
    }

    @Test
    void deleteCurrentAttributesTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, config, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals("l2-cft protocol remove profile TEST ctrl-protocol all-bridges-block",
                commands.getValue().getContent());
    }
}
