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

package io.frinx.cli.unit.huawei.aaa.handler.radius;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.Template;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class RadiusTemplateConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            radius-server template {$name}
            {% if($config.secret_key_hashed.value) %}radius-server shared-key cipher {$config.secret_key_hashed.value}
            {% endif %}{% if($config.authentication_data) %}{% loop in $config.authentication_data as $auth_data %}{% if($auth_data.vrf_name) %}radius-server authentication {$auth_data.key.source_address.ipv4_address.value} 1812 vpn-instance {$auth_data.vrf_name} source LoopBack 0 weight 80
            {% else %}radius-server authentication {$auth_data.key.source_address.ipv4_address.value} 1812 source LoopBack 0 weight 80
            {% endif %}{% endloop %}{% endif %}{% if($config.retransmit_attempts) %}radius-server retransmit {$config.retransmit_attempts}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo radius-server template {$name}
            return""";

    private final Cli cli;

    public RadiusTemplateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String templateName = id.firstKeyOf(Template.class).getName();
        blockingWriteAndRead(cli, id, config, fT(WRITE_UPDATE_TEMPLATE, "config", config, "name", templateName));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String templateName = id.firstKeyOf(Template.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "config", config, "name", templateName));
    }
}