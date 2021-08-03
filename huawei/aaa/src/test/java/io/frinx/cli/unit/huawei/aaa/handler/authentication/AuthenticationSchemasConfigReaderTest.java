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

package io.frinx.cli.unit.huawei.aaa.handler.authentication;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.authentication.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAuthenticationConfig.AuthenticationMethod;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.top.AuthenticationBuilder;

public class AuthenticationSchemasConfigReaderTest {

    private final String outputAuthenticationConfig = "\n authentication-scheme default\n"
            + " authentication-scheme radius\n"
            + "  authentication-mode radius\n"
            + " authentication-scheme AUT-ZIGGO\n"
            + "  authentication-mode local radius\n"
            + "  authentication-scheme radius\n"
            + "  authentication-scheme AUT-ZIGGO\n";

    @Test
    public void testAuthenticationIdsWithConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AuthenticationSchemasConfigReader.parseConfigAttributes(outputAuthenticationConfig, configBuilder, "radius");
        Assert.assertEquals(new ConfigBuilder().setAuthentication(new AuthenticationBuilder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication
                                .top.authentication.ConfigBuilder().setAuthenticationMethod(Arrays.asList(
                                        new AuthenticationMethod("radius"))).build()).build()).build(),
                configBuilder.build());
    }

    @Test
    public void testAuthenticationIdsWithoutConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AuthenticationSchemasConfigReader.parseConfigAttributes(outputAuthenticationConfig, configBuilder, "default");
        Assert.assertEquals(new ConfigBuilder().build(), configBuilder.build());
    }

    @Test
    public void testAuthenticationIdsWithAnotherConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AuthenticationSchemasConfigReader.parseConfigAttributes(outputAuthenticationConfig, configBuilder, "AUT-ZIGGO");
        Assert.assertEquals(new ConfigBuilder().setAuthentication(new AuthenticationBuilder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication
                                .top.authentication.ConfigBuilder().setAuthenticationMethod(Arrays.asList(
                                new AuthenticationMethod("local"), new AuthenticationMethod("radius")))
                                .build()).build()).build(),
                configBuilder.build());
    }
}
