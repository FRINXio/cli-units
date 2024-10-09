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

package io.frinx.cli.unit.huawei.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.huawei.snmp.extension.rev211129.huawei.snmp.top.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunityConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            system-view
            {% if ($config.local_engineid) %}snmp-agent local-engineid {$config.local_engineid}
            Y
            {% else %}undo snmp-agent local-engineid
            Y
            {% endif %}{% if ($config.read_community_password) %}snmp-agent community read {$config.read_community_password.plain_string.value} acl 2000
            Y
            {% else if($before.read_community_password) %}undo snmp-agent community {$before.read_community_password.plain_string.value}
            Y
            {% endif %}{% if ($config.write_community_password) %}snmp-agent community write {$config.write_community_password.plain_string.value} acl 2000
            Y
            {% else if($before.write_community_password) %}undo snmp-agent community {$before.write_community_password.plain_string.value}
            Y
            {% endif %}{% if ($config.community_location) %}snmp-agent sys-info location {$config.community_location}
            Y
            {% else %}undo snmp-agent sys-info location
            Y
            {% endif %}return""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String DELETE_TEMPLATE = """
            system-view
            undo snmp-agent local-engineid
            Y
            {% if ($before.read_community_password) %}undo snmp-agent community {$before.read_community_password.plain_string.value}
            Y
            {% endif %}{% if ($before.write_community_password) %}undo snmp-agent community {$before.write_community_password.plain_string.value}
            Y
            {% endif %}undo snmp-agent sys-info location
            Y
            return""";

    private final Cli cli;

    public CommunityConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE, "before", null, "config", config));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(WRITE_TEMPLATE, "before", dataBefore, "config", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "before", config));
    }
}