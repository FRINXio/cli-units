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
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.Authentication;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.authentication.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.authentication.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAuthenticationConfig.AuthenticationMethod;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.top.AuthenticationBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public final class AuthenticationSchemasConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_AUTH_MODE =
            "display current-configuration | include ^ authentication-scheme |^  authentication";
    private static final Pattern AUTH_MODE =
            Pattern.compile("[\\s\\S]+authentication-mode(?<mode>(\\s\\S+)+)(\\s?.*)+");

    private final Cli cli;

    public AuthenticationSchemasConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        String schemasName = instanceIdentifier.firstKeyOf(Authentication.class).getName();
        parseConfigAttributes(blockingRead(DISPLAY_AUTH_MODE, cli, instanceIdentifier,
                readContext), configBuilder, schemasName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String name) {
        List<AuthenticationMethod> authenticationMethod = Pattern.compile("\\n\\sauthentication-").splitAsStream(output)
                .filter(value -> value.startsWith("scheme " + name + "\n"))
                .map(AUTH_MODE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("mode"))
                .map(String::trim)
                .flatMap(value -> Arrays.stream(value.split(" ")))
                .map(AuthenticationMethod::new)
                .collect(Collectors.toList());

        if (!authenticationMethod.isEmpty()) {
            configBuilder.setAuthentication(new AuthenticationBuilder().setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication
                            .top.authentication.ConfigBuilder().setAuthenticationMethod(authenticationMethod).build())
                    .build());
        }
    }
}
