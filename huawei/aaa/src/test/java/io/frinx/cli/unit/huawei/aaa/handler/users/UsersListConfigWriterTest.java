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

package io.frinx.cli.unit.huawei.aaa.handler.users;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.aaa.IIDs;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.User;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.UserKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.types.rev181121.CryptPasswordType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class UsersListConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private UsersListConfigWriter writer;

    private static final String WRITE_WITHOUT_DATA = "system-view\n"
            + "aaa\n"
            + "local-user test password irreversible-cipher "
            + "%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%^%#\n"
            + "local-user test privilege level 0\n"
            + "undo local-user test service-type\n"
            + "return\n";

    private static final String WRITE_WITH_DATA = "system-view\n"
            + "aaa\n"
            + "local-user test password irreversible-cipher "
            + "%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%^%#\n"
            + "local-user test privilege level 5\n"
            + "local-user test service-type terminal ssh\n"
            + "return\n";

    private static final String UPDATE_WITH_ANOTHER_DATA = "system-view\n"
            + "aaa\n"
            + "local-user test password irreversible-cipher "
            + "$1a$||4SP*]@\\'$ja):C$c(u1j6ci7y11$+3ab+RNqV}']/X9=bX`_S$\n"
            + "local-user test privilege level 10\n"
            + "local-user test service-type telnet terminal ssh ftp\n"
            + "return\n";


    private static final String DELETE_INPUT = "system-view\n"
            + "aaa\n"
            + "undo local-user test\n"
            + "return\n";

    private final InstanceIdentifier<Config> iid = IIDs.AA_AU_USERS
            .child(User.class, new UserKey("test"))
            .child(Config.class);

    private final Config configWithoutAug = new ConfigBuilder()
            .setUsername("test")
            .setPasswordHashed(
                    new CryptPasswordType("%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%^%#"))
            .build();

    private final Config configWithAug = new ConfigBuilder()
            .setUsername("test")
            .setPasswordHashed(
                    new CryptPasswordType("%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%^%#"))
            .addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                    .setPrivilegeLevel((short)5).setServiceType(
                            Arrays.asList("terminal", "ssh")).build())
            .build();

    private final Config configWithAnotherData = new ConfigBuilder()
            .setUsername("test")
            .setPasswordHashed(
                    new CryptPasswordType("$1a$||4SP*]@\\'$ja):C$c(u1j6ci7y11$+3ab+RNqV}']/X9=bX`_S$"))
            .addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                    .setPrivilegeLevel((short)10).setServiceType(
                            Arrays.asList("telnet", "terminal", "ssh", "ftp")).build())
            .build();


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new UsersListConfigWriter(cli);
    }

    @Test
    public void testWriteWithoutData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithoutAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITHOUT_DATA));
    }

    @Test
    public void testWriteWithData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITH_DATA));
    }

    @Test
    public void testUpdateWithAnotherData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithAug, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_ANOTHER_DATA));
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, configWithoutAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }
}