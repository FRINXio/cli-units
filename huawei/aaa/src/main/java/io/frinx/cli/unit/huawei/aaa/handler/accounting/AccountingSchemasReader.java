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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.Account;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.AccountBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.AccountKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class AccountingSchemasReader implements CliConfigListReader<Account, AccountKey, AccountBuilder> {

    public static final String SCHEMAS_LIST =
            "display current-configuration | include ^ accounting-scheme";
    private static final Pattern SCHEMA_LINE = Pattern.compile("accounting-scheme (?<name>\\S+)");
    private final Cli cli;

    public AccountingSchemasReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AccountKey> getAllIds(@Nonnull InstanceIdentifier<Account> instanceIdentifier,
                                     @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SCHEMAS_LIST, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<AccountKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            SCHEMA_LINE::matcher,
            matcher -> matcher.group("name"),
            AccountKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Account> instanceIdentifier,
                                      @Nonnull AccountBuilder builder,
                                      @Nonnull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(Account.class));
    }
}
