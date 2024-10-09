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
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.Account;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.account.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.accounting.schemes.account.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAccountingMethodsCommon.AccountingMethod;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.accounting.top.AccountingBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class AccountingSchemasConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_ACC_MODE =
            "display current-configuration | include ^ accounting-scheme |^  accounting|^  accounting-mode |^ domain";
    private static final Pattern ACC_MODE = Pattern.compile("[\\s\\S]+accounting-mode(?<mode>(\\s\\S+)+)(\\s?.*)+");
    private static final Pattern ACC_FAIL_POLICY =
            Pattern.compile("[\\s\\S]+accounting (?<type>\\S+) (?<mode>\\S+)(\\s?.*)+");

    private final Cli cli;

    public AccountingSchemasConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {

        String accountName = instanceIdentifier.firstKeyOf(Account.class).getName();
        parseConfigAttributes(blockingRead(DISPLAY_ACC_MODE, cli, instanceIdentifier,
                readContext), configBuilder, accountName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String name) {
        List<AccountingMethod> accountingMethods = Pattern.compile("\\n\\saccounting-").splitAsStream(output)
                .filter(value -> value.startsWith("scheme " + name + "\n"))
                .map(ACC_MODE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("mode"))
                .map(String::trim)
                .flatMap(value -> Arrays.stream(value.split(" ")))
                .map(AccountingMethod::new)
                .collect(Collectors.toList());

        parsingFailPolicyFields(output, name, "type", configBuilder::setFailPolicy);
        parsingFailPolicyFields(output, name, "mode", configBuilder::setFailPolicyMode);

        if (!accountingMethods.isEmpty()) {
            configBuilder.setAccounting(new AccountingBuilder().setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.accounting.top
                            .accounting.ConfigBuilder().setAccountingMethod(accountingMethods).build()).build())
                    .build();
        }
    }

    private static void parsingFailPolicyFields(String output, String name, String field, Consumer<String> consumer) {
        Pattern.compile("\\n\\s\\S").splitAsStream(output)
                .filter(value -> value.contains(" " + name + "\n"))
                .map(ACC_FAIL_POLICY::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(field))
                .map(String::trim)
                .findFirst()
                .ifPresent(consumer);
    }
}