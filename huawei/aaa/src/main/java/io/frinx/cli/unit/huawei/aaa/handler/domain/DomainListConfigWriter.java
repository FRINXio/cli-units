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

package io.frinx.cli.unit.huawei.aaa.handler.domain;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.domain.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DomainListConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            aaa
            domain {$domain_name}
            {% if($authentication_scheme) %}authentication-scheme {$authentication_scheme}
            {% endif %}{% if($accounting_scheme) %}accounting-scheme {$accounting_scheme}
            {% endif %}{% if($radius_server) %}radius-server {$radius_server}
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            aaa
            undo domain {$domain_name}
            return""";

    private final Cli cli;

    public DomainListConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String domainName = id.firstKeyOf(Domain.class).getName();
        //TODO add preconditions if names exist for authentication schema, accounting schema and radius server
        blockingWriteAndRead(cli, id, config,
                fT(WRITE_UPDATE_TEMPLATE, "domain_name", domainName, "authentication_scheme",
                        config.getAuthenticationScheme(), "accounting_scheme", config.getAccountingScheme(),
                        "radius_server", config.getRadiusServer()));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataAfter, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String domainName = id.firstKeyOf(Domain.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "domain_name", domainName));
    }
}