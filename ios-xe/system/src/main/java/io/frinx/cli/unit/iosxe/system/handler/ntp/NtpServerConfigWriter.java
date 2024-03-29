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
package io.frinx.cli.unit.iosxe.system.handler.ntp;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.VrfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpServerConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            {% if ($address) %}ntp server{% if ($vrf) %} vrf {$vrf}{% endif %} {$address}
            {% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            no ntp server{% if ($vrf) %} vrf {$vrf}{% endif %} {$address}
            end""";

    private final Cli cli;

    public NtpServerConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        Host hostAddress = config.getAddress();
        VrfCiscoAug vrfCiscoAug = config.getAugmentation(VrfCiscoAug.class);
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "address", hostAddress.getIpAddress().getIpv4Address().getValue(),
                        "vrf", (vrfCiscoAug != null) ? vrfCiscoAug.getVrf() : null));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(instanceIdentifier, dataBefore, writeContext);
        writeCurrentAttributes(instanceIdentifier, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        Host hostAddress = config.getAddress();
        VrfCiscoAug vrfCiscoAug = config.getAugmentation(VrfCiscoAug.class);
        blockingDeleteAndRead(cli, instanceIdentifier, fT(DELETE_TEMPLATE,
                "address", hostAddress.getIpAddress().getIpv4Address().getValue(),
                "vrf", (vrfCiscoAug != null) ? vrfCiscoAug.getVrf() : null));
    }
}