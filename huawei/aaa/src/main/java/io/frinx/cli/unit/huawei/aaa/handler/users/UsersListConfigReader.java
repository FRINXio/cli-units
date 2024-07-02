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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.User;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.types.rev181121.CryptPasswordType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class UsersListConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_USER_DATA = "display current-configuration | include local-user %s ";
    private static final Pattern USER_PASSWORD = Pattern.compile(".*password irreversible-cipher (?<value>.*)");
    private static final Pattern PRIVILEGE_LEVEL = Pattern.compile(".*privilege level (?<value>\\d+)");
    private static final Pattern SERVICE_TYPES = Pattern.compile(".*service-type(?<value>(\\s\\S+)+)");

    private final Cli cli;

    public UsersListConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {

        String userName = instanceIdentifier.firstKeyOf(User.class).getUsername();
        parseConfigAttributes(blockingRead(String.format(DISPLAY_USER_DATA, userName), cli, instanceIdentifier,
                readContext), configBuilder, userName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String name) {
        AaaHuaweiUserAugBuilder augBuilder = new AaaHuaweiUserAugBuilder();
        parsingDomainFields(output, name, USER_PASSWORD,
            value -> configBuilder.setPasswordHashed(new CryptPasswordType(value)));
        parsingDomainFields(output, name, PRIVILEGE_LEVEL, value -> augBuilder.setPrivilegeLevel(Short.valueOf(value)));

        List<String> serviceTypes = ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(value -> value.contains(" " + name + " "))
                .map(SERVICE_TYPES::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("value"))
                .map(String::trim)
                .flatMap(value -> Arrays.stream(value.split(" ")))
                .collect(Collectors.toList());

        if (!serviceTypes.isEmpty()) {
            augBuilder.setServiceType(serviceTypes);
        }

        if (augBuilder.getPrivilegeLevel() != null || augBuilder.getServiceType() != null) {
            configBuilder.addAugmentation(AaaHuaweiUserAug.class, augBuilder.build());
        }
    }

    private static void parsingDomainFields(String output, String name, Pattern pattern, Consumer<String> consumer) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(value -> value.contains(" " + name + " "))
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("value"))
                .map(String::trim)
                .findFirst()
                .ifPresent(consumer);
    }
}