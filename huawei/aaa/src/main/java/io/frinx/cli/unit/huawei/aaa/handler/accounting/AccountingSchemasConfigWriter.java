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
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.Account;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.account.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAccountingMethodsCommon.AccountingMethod;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class AccountingSchemasConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            aaa
            accounting-scheme {$acc_name}
            {% if($accounting_mode) %}accounting-mode{% loop in $accounting_mode as $mode %} {$mode.string}{% endloop %}
            {% else %}undo accounting-mode
            {% endif %}{% if($fail_policy_before) %}undo accounting {$fail_policy_before}
            {% endif %}{% if($fail_policy) %}accounting {$fail_policy} {$fail_policy_mode}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            aaa
            undo accounting-scheme {$acc_name}
            return""";

    private final Cli cli;

    public AccountingSchemasConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String accName = id.firstKeyOf(Account.class).getName();
        List<AccountingMethod> methods = null;
        if (config.getAccounting() != null) {
            methods = config.getAccounting().getConfig().getAccountingMethod();
        }
        blockingWriteAndRead(cli, id, config,
                fT(WRITE_UPDATE_TEMPLATE, "acc_name", accName, "fail_policy", config.getFailPolicy(),
                        "fail_policy_mode", config.getFailPolicyMode(), "accounting_mode", methods));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String accName = id.firstKeyOf(Account.class).getName();
        List<AccountingMethod> methods = null;
        if (dataAfter.getAccounting() != null) {
            methods = dataAfter.getAccounting().getConfig().getAccountingMethod();
        }
        blockingWriteAndRead(cli, id, dataAfter,
                fT(WRITE_UPDATE_TEMPLATE, "acc_name", accName, "fail_policy_before", dataBefore.getFailPolicy(),
                        "fail_policy", dataAfter.getFailPolicy(), "fail_policy_mode", dataAfter.getFailPolicyMode(),
                        "accounting_mode", methods));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String accName = id.firstKeyOf(Account.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "acc_name", accName));
    }
}