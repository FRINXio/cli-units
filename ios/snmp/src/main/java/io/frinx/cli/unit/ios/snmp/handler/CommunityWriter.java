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

package io.frinx.cli.unit.ios.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.Community;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunityWriter implements CliWriter<Community> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "snmp-server community {$name}"
            + "{% if ($config.view) %} view {$config.view}{% endif %}"
            + " {$config.access.name}"
            + "{% if ($config.access_list) %} {$config.access_list}{% endif %}"
            + "\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no snmp-server community {$name}\n"
            + "end";

    private final Cli cli;

    public CommunityWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Community> instanceIdentifier,
                                       @Nonnull Community community,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, community,
                fT(WRITE_TEMPLATE,
                        "name", community.getName(),
                        "config", community.getConfig()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Community> id,
                                        @Nonnull Community dataBefore,
                                        @Nonnull Community dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Community> instanceIdentifier,
                                        @Nonnull Community community,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, community,
                fT(DELETE_TEMPLATE, "name", community.getName()));
    }

}