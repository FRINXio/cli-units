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

package io.frinx.cli.unit.huawei.aaa.handler.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.account.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAccountingMethodsCommon.AccountingMethod;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.accounting.top.AccountingBuilder;

class AccountingSchemasConfigReaderTest {

    private final String outputAccountsConfig = """

             accounting-scheme default
             accounting-scheme ACC-SC
              accounting-mode radius
              accounting start-fail online
             domain default
             domain default_admin
              accounting-scheme ACC-SC
            """;

    @Test
    void testAccountingIdsWithConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AccountingSchemasConfigReader.parseConfigAttributes(outputAccountsConfig, configBuilder, "ACC-SC");
        assertEquals(new ConfigBuilder()
                .setAccounting(new AccountingBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa
                        .rev200730.aaa.accounting.top.accounting.ConfigBuilder().setAccountingMethod(
                                Arrays.asList(new AccountingMethod("radius"))).build()).build())
                .setFailPolicy("start-fail")
                .setFailPolicyMode("online").build(), configBuilder.build());
    }

    @Test
    void testAccountingIdsWithoutConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AccountingSchemasConfigReader.parseConfigAttributes(outputAccountsConfig, configBuilder, "default");
        assertEquals(new ConfigBuilder().build(), configBuilder.build());
    }
}
