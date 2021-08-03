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

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.AccountKey;

public class AccountingSchemasReaderTest {

    private final String outputAccounts = " accounting-scheme default\n"
            + " accounting-scheme ACC-SC\n";

    @Test
    public void testAccountingIds() {
        List<AccountKey> keys = AccountingSchemasReader.getAllIds(outputAccounts);
        Assert.assertEquals(keys, Arrays.asList(new AccountKey("default"), new AccountKey("ACC-SC")));
    }
}
