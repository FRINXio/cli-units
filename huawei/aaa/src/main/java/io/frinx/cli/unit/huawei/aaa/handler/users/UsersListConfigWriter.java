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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.AaaHuaweiUserAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.User;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.user.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class UsersListConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = "system-view\n"
            + "aaa\n"
            + "{% if($password) %}"
            + "local-user {$username} password irreversible-cipher {$password.value}\n"
            + "{% else %}undo local-user {$username} password irreversible-cipher\n{% endif %}"
            + "{% if($privilege) %}"
            + "local-user {$username} privilege level {$privilege}\n"
            + "{% else %}local-user {$username} privilege level 0\n{% endif %}"
            + "{% if($service_type) %}local-user {$username} service-type"
            + "{% loop in $service_type as $type %} {$type}"
            + "{% endloop %}\n"
            + "{% else %}undo local-user {$username} service-type\n{% endif %}"
            + "return";

    private static final String DELETE_TEMPLATE = "system-view\n"
            + "aaa\n"
            + "undo local-user {$username}\n"
            + "return";

    private final Cli cli;

    public UsersListConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String username = id.firstKeyOf(User.class).getUsername();
        AaaHuaweiUserAug augConfig = config.getAugmentation(AaaHuaweiUserAug.class);
        Short privilegeLevel = null;
        List<String> serviceTypes = null;
        if (augConfig != null) {
            privilegeLevel = augConfig.getPrivilegeLevel();
            serviceTypes = augConfig.getServiceType();
        }
        blockingWriteAndRead(cli, id, config, fT(WRITE_UPDATE_TEMPLATE, "username", username, "password",
                config.getPasswordHashed(), "privilege", privilegeLevel, "service_type", serviceTypes));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataAfter, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String username = id.firstKeyOf(User.class).getUsername();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "username", username));
    }
}
