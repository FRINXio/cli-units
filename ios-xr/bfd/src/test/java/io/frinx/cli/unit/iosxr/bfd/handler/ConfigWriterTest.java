/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.bfd.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class ConfigWriterTest {

    private static final Config WRITE_DATA_1 = new ConfigBuilder()
            .setEnabled(true)
            .setDesiredMinimumTxInterval(10L)
            .setDetectionMultiplier(66)
            .setId("Bundle-Ether100")
            .setLocalAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .addAugmentation(IfBfdExtAug.class, new IfBfdExtAugBuilder()
                    .setRemoteAddress(new IpAddress(new Ipv4Address("4.3.2.1")))
                    .build())
            .build();

    private static final String EXPECTED_WRITE_OUTPUT = """
            interface Bundle-Ether100
             bfd mode ietf
             bfd address-family ipv4 fast-detect
             bfd address-family ipv4 multiplier 66
             bfd address-family ipv4 minimum-interval 10
             bfd address-family ipv4 destination 4.3.2.1
            root
            """;

    private static final String EXPECTED_DELETE_OUTPUT = """
            interface Bundle-Ether100
             no bfd mode ietf
             no bfd address-family ipv4 fast-detect
             no bfd address-family ipv4 multiplier
             no bfd address-family ipv4 minimum-interval
             no bfd address-family ipv4 destination
            root
            """;

    @Mock
    private Cli cli;

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private ConfigWriter configWriter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        configWriter = new ConfigWriter(cli);
    }

    @Test
    void writeCurrentAttributesTest() throws WriteFailedException {
        configWriter.writeConfig(iid, WRITE_DATA_1, "Bundle-Ether100");
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(EXPECTED_WRITE_OUTPUT, response.getValue().getContent());
    }

    @Test
    void deleteCurrentAttributesTest() throws WriteFailedException {
        configWriter.deleteConfig(iid, "Bundle-Ether100");
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(EXPECTED_DELETE_OUTPUT, response.getValue().getContent());
    }
}