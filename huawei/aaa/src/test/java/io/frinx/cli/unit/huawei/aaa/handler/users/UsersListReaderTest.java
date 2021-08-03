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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.UserKey;

public class UsersListReaderTest {

    private final String outputUsers = "undo local-user admin\n"
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
    public void testUsersIds() {
        List<UserKey> keys = UsersListReader.getAllIds(outputUsers);
        Assert.assertEquals(keys, Arrays.asList(new UserKey("lab"), new UserKey("frinx"),
                new UserKey("ziggocpe"), new UserKey("client001"), new UserKey("frinxuser")));
    }
}