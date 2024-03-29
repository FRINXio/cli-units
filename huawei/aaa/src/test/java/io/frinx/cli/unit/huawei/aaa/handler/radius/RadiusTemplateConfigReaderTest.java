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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.config.AuthenticationDataBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.types.rev181121.CryptPasswordType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;


class RadiusTemplateConfigReaderTest {

    private final String outputRadiusTemplateConfig = """
            radius-server template default\r
            radius-server template RADIUS-ZIGGO\r
             radius-server shared-key cipher %^%#E@)R&d7(C8x1k9MYf,<Af;|b$}sXO/_L;O-'RRLD%^%#\r
             radius-server authentication 198.18.1.15 1812 source LoopBack 0 weight 80\r
             radius-server authentication 198.18.1.16 1812 source LoopBack 0 weight 80\r
             radius-server retransmit 2 timeout 2\r
            radius-server template Test\r
             radius-server authentication 198.18.1.16 1812 vpn-instance MANAGEMENT source LoopBack 0 weight 80\r
            """;

    @Test
    void testRadiusIdsWithoutConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        RadiusTemplateConfigReader.parseConfigAttributes(outputRadiusTemplateConfig, configBuilder, "default");
        assertEquals(new ConfigBuilder().build(), configBuilder.build());
    }

    @Test
    void testRadiusIdsWithConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        RadiusTemplateConfigReader.parseConfigAttributes(outputRadiusTemplateConfig, configBuilder, "RADIUS-ZIGGO");
        assertEquals(new ConfigBuilder()
                .setSecretKeyHashed(new CryptPasswordType("%^%#E@)R&d7(C8x1k9MYf,<Af;|b$}sXO/_L;O-'RRLD%^%#"))
                .setAuthenticationData(Arrays.asList(
                        new AuthenticationDataBuilder()
                                .setSourceAddress(new IpAddress(new Ipv4Address("198.18.1.15")))
                                .build(),
                        new AuthenticationDataBuilder()
                                .setSourceAddress(new IpAddress(new Ipv4Address("198.18.1.16")))
                                .build()))
                .setRetransmitAttempts(Short.valueOf("2")).build(), configBuilder.build());
    }

    @Test
    void testRadiusIdsWithAuthDataConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        RadiusTemplateConfigReader.parseConfigAttributes(outputRadiusTemplateConfig, configBuilder, "Test");
        assertEquals(new ConfigBuilder()
                .setAuthenticationData(Collections.singletonList(
                        new AuthenticationDataBuilder()
                                .setSourceAddress(new IpAddress(new Ipv4Address("198.18.1.16")))
                                .setVrfName("MANAGEMENT")
                                .build())).build(), configBuilder.build());
    }
}
