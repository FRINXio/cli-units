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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.User;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.UserBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.UserKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AuthenticationUserReader implements CliConfigListReader<User, UserKey, UserBuilder> {

    public static final String SH_USER = "user show";
    public static final Pattern PARSE_USER_ACCOUNT = Pattern.compile(
            "\\| (?<username>(?!Username)\\S+) *\\| (?<accessLevel>\\S+) *.*");

    private Cli cli;

    public AuthenticationUserReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<User> id,
                                      @NotNull UserBuilder userBuilder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        userBuilder.setKey(new UserKey(id.firstKeyOf(User.class).getUsername()));
    }

    @NotNull
    @Override
    public List<UserKey> getAllIds(@NotNull InstanceIdentifier<User> id,
                                   @NotNull ReadContext context) throws ReadFailedException {
        return parseAllUserKeys(blockingRead(SH_USER, cli, id, context));
    }

    public static List<UserKey> parseAllUserKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
                PARSE_USER_ACCOUNT::matcher,
                matcher -> matcher.group("username"),
                UserKey::new);
    }
}