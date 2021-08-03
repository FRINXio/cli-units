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

package io.frinx.cli.unit.huawei.aaa.handler.authentication;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.Authentication;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.AuthenticationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.AuthenticationKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class AuthenticationSchemasReader implements CliConfigListReader<Authentication,
                                                                            AuthenticationKey, AuthenticationBuilder> {

    public static final String SCHEMAS_LIST =
            "display current-configuration | include authentication-scheme";
    private static final Pattern SCHEMA_LINE = Pattern.compile("authentication-scheme (?<name>\\S+)");
    private final Cli cli;

    public AuthenticationSchemasReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AuthenticationKey> getAllIds(@Nonnull InstanceIdentifier<Authentication> instanceIdentifier,
                                          @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SCHEMAS_LIST, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<AuthenticationKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            SCHEMA_LINE::matcher,
            matcher -> matcher.group("name"),
            AuthenticationKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Authentication> instanceIdentifier,
                                      @Nonnull AuthenticationBuilder builder,
                                      @Nonnull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(Authentication.class));
    }
}
