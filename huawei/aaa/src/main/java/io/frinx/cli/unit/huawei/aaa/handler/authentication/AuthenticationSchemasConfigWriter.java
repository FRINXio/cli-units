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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.Authentication;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.authentication.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.AaaAuthenticationConfig.AuthenticationMethod;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class AuthenticationSchemasConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = "system-view\n"
            + "aaa\n"
            + "authentication-scheme {$auth_name}\n"
            + "{% if($authentication_mode) %}authentication-mode"
            + "{% loop in $authentication_mode as $mode %} {$mode.string}"
            + "{% endloop %}\n"
            + "{% else %}undo authentication-mode\n{% endif %}"
            + "return";

    private static final String DELETE_TEMPLATE = "system-view\n"
            + "aaa\n"
            + "undo authentication-scheme {$auth_name}\n"
            + "return";

    private final Cli cli;

    public AuthenticationSchemasConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String authName = id.firstKeyOf(Authentication.class).getName();
        List<AuthenticationMethod> methods = null;
        if (config.getAuthentication() != null) {
            methods = config.getAuthentication().getConfig().getAuthenticationMethod();
        }
        blockingWriteAndRead(cli, id, config,
                fT(WRITE_UPDATE_TEMPLATE, "auth_name", authName, "authentication_mode", methods));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String authName = id.firstKeyOf(Authentication.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "auth_name", authName));
    }
}
