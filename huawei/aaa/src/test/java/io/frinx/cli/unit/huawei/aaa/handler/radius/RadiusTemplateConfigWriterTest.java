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

package io.frinx.cli.unit.huawei.aaa.handler.radius;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.aaa.IIDs;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.Template;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.TemplateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.config.AuthenticationDataBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.types.rev181121.CryptPasswordType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class RadiusTemplateConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private RadiusTemplateConfigWriter writer;

    private static final String WRITE_WITHOUT_DATA = """
            system-view
            radius-server template RADIUS-ZIGGO
            return
            """;

    private static final String WRITE_WITH_DATA = """
            system-view
            radius-server template RADIUS-ZIGGO
            radius-server shared-key cipher %^%#E@)R&d7(C8x1k9MYf,<Af;|b$}sXO/_L;O-'RRLD%^%#
            radius-server authentication 198.18.1.15 1812 source LoopBack 0 weight 80
            radius-server authentication 198.18.1.16 1812 source LoopBack 0 weight 80
            radius-server retransmit 2
            return
            """;

    private static final String WRITE_WITH_ANOTHER_DATA = """
            system-view
            radius-server template RADIUS-ZIGGO
            radius-server authentication 198.18.1.16 1812 vpn-instance MANAGEMENT source LoopBack 0 weight 80
            radius-server retransmit 5
            return
            """;


    private static final String DELETE_INPUT = """
            system-view
            undo radius-server template RADIUS-ZIGGO
            return
            """;

    private final InstanceIdentifier<Config> iid = IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_RADIUS
            .child(Template.class, new TemplateKey("RADIUS-ZIGGO"))
            .child(Config.class);

    private final Config configWithoutData = new ConfigBuilder()
            .setName("RADIUS-ZIGGO")
            .build();

    private final Config configWithData = new ConfigBuilder()
            .setName("RADIUS-ZIGGO")
            .setSecretKeyHashed(new CryptPasswordType("%^%#E@)R&d7(C8x1k9MYf,<Af;|b$}sXO/_L;O-'RRLD%^%#"))
            .setAuthenticationData(Arrays.asList(
                    new AuthenticationDataBuilder()
                            .setSourceAddress(new IpAddress(new Ipv4Address("198.18.1.15")))
                            .build(),
                    new AuthenticationDataBuilder()
                            .setSourceAddress(new IpAddress(new Ipv4Address("198.18.1.16")))
                            .build()))
            .setRetransmitAttempts(Short.valueOf("2"))
            .build();

    private final Config configWithAnotherData = new ConfigBuilder()
            .setName("RADIUS-ZIGGO")
            .setAuthenticationData(Collections.singletonList(
                    new AuthenticationDataBuilder()
                            .setSourceAddress(new IpAddress(new Ipv4Address("198.18.1.16")))
                            .setVrfName("MANAGEMENT")
                            .build()))
            .setRetransmitAttempts(Short.valueOf("5"))
            .build();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new RadiusTemplateConfigWriter(cli);
    }

    @Test
    void testWriteWithoutData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithoutData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITHOUT_DATA));
    }

    @Test
    void testWriteWithData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITH_DATA));
    }

    @Test
    void testWriteWithAnotherData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITH_ANOTHER_DATA));
    }

    @Test
    void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, configWithoutData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }
}
