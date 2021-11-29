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

package io.frinx.cli.unit.huawei.system.handler.ntp;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.Server;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpServerConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = "system-view\n"
            + "{% if($prefered == TRUE) %}"
            + "ntp-service unicast-server {$ip_address} preference\n"
            + "{% else %}"
            + "ntp-service unicast-server {$ip_address}\n"
            + "{% endif %}"
            + "return";

    private static final String DELETE_TEMPLATE = "system-view\n"
            + "undo ntp-service unicast-server {$ip_address}\n"
            + "return";

    private final Cli cli;

    public NtpServerConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Host hostAddress = id.firstKeyOf(Server.class).getAddress();
        Boolean preference = config.isPrefer();
        blockingWriteAndRead(cli, id, config,
                fT(WRITE_UPDATE_TEMPLATE, "ip_address", hostAddress.getIpAddress()
                        .getIpv4Address().getValue(), "prefered", preference));
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
        Host hostAddress = id.firstKeyOf(Server.class).getAddress();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "ip_address", hostAddress.getIpAddress()
                .getIpv4Address().getValue()));
    }
}
