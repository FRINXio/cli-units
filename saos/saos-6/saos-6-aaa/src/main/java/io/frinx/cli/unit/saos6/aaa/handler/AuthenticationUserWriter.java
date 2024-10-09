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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.ciena.extension.rev221123.AaaCienaUserAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.User;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AuthenticationUserWriter implements CliWriter<User> {

    private static final String WRITE_TEMPLATE = "user create user {$username} access-level {$accessLevel} "
            + "secret {$encryptedSecretString}";
    private static final String DELETE_TEMPLATE = "user delete user {$username}";

    private Cli cli;

    public AuthenticationUserWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<User> instanceIdentifier,
                                       @NotNull User dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(writeTemplate(dataAfter.getUsername(),
                dataAfter.getConfig().getAugmentation(AaaCienaUserAug.class).getAccessLevel().getName(),
                dataAfter.getConfig().getPassword()), cli, instanceIdentifier, dataAfter);
    }

    @VisibleForTesting
    String writeTemplate(String username, String accessLevel, String encryptedSecretString) {
        return fT(WRITE_TEMPLATE, "username", username,
                "accessLevel", accessLevel,
                "encryptedSecretString", encryptedSecretString);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<User> instanceIdentifier,
                                        @NotNull User dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingDelete(deleteTemplate(dataBefore.getUsername()), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String deleteTemplate(String username) {
        return fT(DELETE_TEMPLATE, "username", username);
    }
}