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

package io.frinx.cli.unit.iosxr.mpls.handler;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class TeConfigReaderTest {

    private static final String OUTPUT = """
            Fri Jan 19 11:52:35.794 UTC
            mpls traffic-eng
            interface GigabitEthernet0/0/0/1
            !
            """;

    private static final String NO_OUTPUT = """
            Fri Jan 19 11:52:35.794 UTC
            interface GigabitEthernet0/0/0/1
            !
            """;

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;
    @Mock
    private InstanceIdentifier<Config> iid;

    private TeConfigReader reader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(OUTPUT));
        reader = Mockito.spy(new TeConfigReader(cli));
    }

    @Test
    void testMplsEnabled() throws Exception {
        Mockito.doReturn(OUTPUT).when(reader).blockingRead(Mockito.eq(TeConfigReader.SHOW_RUN_MPLS), Mockito.eq(cli),
                Mockito.eq(iid), Mockito.eq(readContext));
        ConfigBuilder builder = new ConfigBuilder();

        reader.readCurrentAttributes(iid, builder, readContext);
        assertTrue(builder.isEnabled());
    }

    @Test
    void testMplsDisabled() throws Exception {
        Mockito.doReturn(NO_OUTPUT).when(reader).blockingRead(Mockito.eq(TeConfigReader.SHOW_RUN_MPLS), Mockito.eq(cli),
                Mockito.eq(iid), Mockito.eq(readContext));
        ConfigBuilder builder = new ConfigBuilder();

        reader.readCurrentAttributes(iid, builder, readContext);
        assertNull(builder.isEnabled());
    }
}
