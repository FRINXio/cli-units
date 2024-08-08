/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos6.aaa.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.ciena.extension.rev221123.AaaCienaUserAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.ciena.extension.rev221123.AaaCienaUserAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.ciena.extension.rev221123.UserExtension.AccessLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.User;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AuthenticationUserConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_USER = "configuration search string \"user create user %s\"";
    private static final Pattern PARSE_USER = Pattern.compile(
            "user create user (?<username>\\S+) access-level (?<accessLevel>\\S+) secret (?<secret>\\S+) *");

    private Cli cli;

    public AuthenticationUserConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var username = id.firstKeyOf(User.class).getUsername();
        var builder = new AaaCienaUserAugBuilder();
        parseUserConfig(configBuilder, builder,
                blockingRead(f(SH_USER, username), cli, id, readContext));
        configBuilder.addAugmentation(AaaCienaUserAug.class, builder.build());
    }

    public static void parseUserConfig(@NotNull ConfigBuilder configBuilder,
                                      AaaCienaUserAugBuilder builder,
                                      String output) {
        ParsingUtils.parseFields(output, 0,
                PARSE_USER::matcher,
                matcher -> matcher.group("username"),
                configBuilder::setUsername);

        ParsingUtils.parseField(output, 0,
                PARSE_USER::matcher,
                matcher -> matcher.group("accessLevel"),
                accessLevel -> convertStringToAccessLevel(accessLevel, builder));

        ParsingUtils.parseFields(output, 0,
                PARSE_USER::matcher,
                matcher -> matcher.group("secret"),
                configBuilder::setPassword);
    }

    private static void convertStringToAccessLevel(String accessLevelValue, AaaCienaUserAugBuilder builder) {
        AccessLevel accessLevel = switch (accessLevelValue.toLowerCase(Locale.ROOT)) {
            case "limited" -> AccessLevel.LIMITED;
            case "admin" -> AccessLevel.ADMIN;
            case "super" -> AccessLevel.SUPER;
            case "diag" -> AccessLevel.DIAG;
            default -> throw new IllegalArgumentException("Unsupported access level: " + accessLevelValue);
        };

        builder.setAccessLevel(accessLevel);
    }
}