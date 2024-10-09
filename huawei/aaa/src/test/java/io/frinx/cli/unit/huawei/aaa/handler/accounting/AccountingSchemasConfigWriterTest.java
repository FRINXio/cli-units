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

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.aaa.IIDs;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.Account;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.AccountKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.account.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.account.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAccountingMethodsCommon.AccountingMethod;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.accounting.top.AccountingBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


class AccountingSchemasConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private AccountingSchemasConfigWriter writer;

    private static final String WRITE_WITHOUT_DATA = """
            system-view
            aaa
            accounting-scheme test
            undo accounting-mode
            return
            """;

    private static final String WRITE_WITH_DATA = """
            system-view
            aaa
            accounting-scheme test
            accounting-mode radius
            accounting start-fail online
            return
            """;

    private static final String UPDATE_WITH_DATA = """
            system-view
            aaa
            accounting-scheme test
            accounting-mode radius
            accounting start-fail online
            return
            """;

    private static final String UPDATE_WITHOUT_DATA = """
            system-view
            aaa
            accounting-scheme test
            undo accounting-mode
            undo accounting start-fail
            return
            """;

    private static final String UPDATE_WITH_ANOTHER_DATA = """
            system-view
            aaa
            accounting-scheme test
            accounting-mode haca
            undo accounting start-fail
            accounting interim-fail online
            return
            """;


    private static final String DELETE_INPUT = """
            system-view
            aaa
            undo accounting-scheme test
            return
            """;

    private final InstanceIdentifier<Config> iid = IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_ACCOUNTINGSCHEMES
            .child(Account.class, new AccountKey("test"))
            .child(Config.class);

    private final Config configWithoutData = new ConfigBuilder()
            .setName("test")
            .build();

    private final Config configWithData = new ConfigBuilder()
            .setName("test")
            .setFailPolicy("start-fail")
            .setFailPolicyMode("online")
            .setAccounting(new AccountingBuilder().setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.accounting.top
                                .accounting.ConfigBuilder().setAccountingMethod(Arrays.asList(
                                        new AccountingMethod("radius"))).build())
                    .build())
            .build();

    private final Config configWithAnotherData = new ConfigBuilder()
            .setName("test")
            .setFailPolicy("interim-fail")
            .setFailPolicyMode("online")
            .setAccounting(new AccountingBuilder().setConfig(
                            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa
                                    .accounting.top.accounting.ConfigBuilder().setAccountingMethod(Arrays.asList(
                                        new AccountingMethod("haca"))).build())
                    .build())
            .build();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new AccountingSchemasConfigWriter(cli);
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
    void testUpdateWithoutData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithData, configWithoutData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITHOUT_DATA));
    }

    @Test
    void testUpdateWithData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithoutData, configWithData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_DATA));
    }

    @Test
    void testUpdateWithAnotherData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithData, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_ANOTHER_DATA));
    }

    @Test
    void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, configWithoutData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }
}
