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

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.types.rev181121.CryptPasswordType;

public class UsersListConfigReaderTest {

    private final String outputUserConfig = "undo local-user admin\n"
        + " local-user lab password irreversible-cipher $1a$6~.;C|I6xV$H^7K2h^4O=J*TZR!kFI$NITjYm+)nDL<9M\"E*{Z3$\n"
        + " local-user lab privilege level 15\n"
        + " local-user lab service-type telnet terminal ssh ftp\n"
        + " local-user frinx password irreversible-cipher $1a$|a\"1Q2dt,8$!2;5PG2k@~B*R/LB.HsDaDxX)<dw;O.H.A:sm/j.$\n"
        + " local-user frinx privilege level 15\n"
        + " local-user frinx service-type telnet terminal ssh ftp\n"
        + " local-user ziggocpe password irreversible-cipher "
        +   "%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%^%#\n"
        + " local-user ziggocpe privilege level 15\n"
        + " local-user ziggocpe service-type terminal ssh\n"
        + " local-user client001 privilege level 0\n"
        + " local-user client001 service-type ssh\n"
        + " local-user frinxuser password irreversible-cipher "
        +   "$1a$||4SP*]@\\'$ja):C$c(u1j6ci7y11$+3ab+RNqV}']/X9=bX`_S$\n"
        + " local-user frinxuser privilege level 15\n"
        + " local-user frinxuser service-type terminal ssh\n";

    @Test
    public void testUserConfigIds1() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        UsersListConfigReader.parseConfigAttributes(outputUserConfig, configBuilder, "lab");
        Assert.assertEquals(new ConfigBuilder().setPasswordHashed(
                new CryptPasswordType("$1a$6~.;C|I6xV$H^7K2h^4O=J*TZR!kFI$NITjYm+)nDL<9M\"E*{Z3$"))
                        .addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                                .setPrivilegeLevel((short)15).setServiceType(
                                        Arrays.asList("telnet", "terminal", "ssh", "ftp")).build()).build(),
                configBuilder.build());
    }

    @Test
    public void testUserConfigIds2() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        UsersListConfigReader.parseConfigAttributes(outputUserConfig, configBuilder, "frinx");
        Assert.assertEquals(new ConfigBuilder().setPasswordHashed(
                                new CryptPasswordType("$1a$|a\"1Q2dt,8$!2;5PG2k@~B*R/LB.HsDaDxX)<dw;O.H.A:sm/j.$"))
                        .addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                                .setPrivilegeLevel((short)15).setServiceType(
                                        Arrays.asList("telnet", "terminal", "ssh", "ftp")).build()).build(),
                configBuilder.build());
    }

    @Test
    public void testUserConfigIds3() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        UsersListConfigReader.parseConfigAttributes(outputUserConfig, configBuilder, "ziggocpe");
        Assert.assertEquals(new ConfigBuilder().setPasswordHashed(
                new CryptPasswordType("%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%^%#"))
                        .addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                                .setPrivilegeLevel((short)15).setServiceType(
                                        Arrays.asList("terminal", "ssh")).build()).build(),
                configBuilder.build());
    }

    @Test
    public void testUserConfigIds4() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        UsersListConfigReader.parseConfigAttributes(outputUserConfig, configBuilder, "client001");
        Assert.assertEquals(new ConfigBuilder().addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                .setPrivilegeLevel((short) 0).setServiceType(Arrays.asList("ssh"))
                .build()).build(), configBuilder.build());
    }

    @Test
    public void testUserConfigIds5() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        UsersListConfigReader.parseConfigAttributes(outputUserConfig, configBuilder, "frinxuser");
        Assert.assertEquals(new ConfigBuilder().setPasswordHashed(
                                new CryptPasswordType("$1a$||4SP*]@\\'$ja):C$c(u1j6ci7y11$+3ab+RNqV}']/X9=bX`_S$"))
                        .addAugmentation(AaaHuaweiUserAug.class, new AaaHuaweiUserAugBuilder()
                                .setPrivilegeLevel((short)15).setServiceType(
                                        Arrays.asList("terminal", "ssh")).build()).build(),
                configBuilder.build());
    }

}
