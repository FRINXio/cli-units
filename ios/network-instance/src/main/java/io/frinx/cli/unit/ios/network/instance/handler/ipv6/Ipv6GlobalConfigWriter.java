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

package io.frinx.cli.unit.ios.network.instance.handler.ipv6;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.honeycomb.translate.write.WriteFailedException.CreateFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.ipvsix.cisco.rev210630.cisco.ipv6.global.config.CiscoIpv6Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Ipv6GlobalConfigWriter implements CliWriter<CiscoIpv6Config> {

    private static final String TEMPLATE = "configure terminal\n"
            + "{% if (($delete==TRUE) || ($ipv6Unicast!=TRUE)) %}no {% endif %}ipv6 unicast-routing\n"
            + "{% if (($delete==TRUE) || ($ipv6Cef!=TRUE)) %}no {% endif %}ipv6 cef\n"
            + "end";

    private final Cli cli;

    public Ipv6GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<CiscoIpv6Config> instanceIdentifier,
                                       @Nonnull CiscoIpv6Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        executeCommandsOrThrowException(instanceIdentifier, config, false);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<CiscoIpv6Config> instanceIdentifier,
                                        @Nonnull CiscoIpv6Config dataBefore,
                                        @Nonnull CiscoIpv6Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        executeCommandsOrThrowException(instanceIdentifier, dataAfter, false);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<CiscoIpv6Config> instanceIdentifier,
                                        @Nonnull CiscoIpv6Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        executeCommandsOrThrowException(instanceIdentifier, config, true);
    }

    private void executeCommandsOrThrowException(InstanceIdentifier<CiscoIpv6Config> id, CiscoIpv6Config config,
                                                 boolean forRemoving) throws CreateFailedException {
        Preconditions.checkArgument(NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class)),
                "Ipv6 global configuration should be configured in default network instance");
        blockingWriteAndRead(cli, id, config, fT(TEMPLATE, "delete", forRemoving,
                "ipv6Unicast", config.isUnicastRoutingEnabled(),
                "ipv6Cef", config.isCefEnabled()));
    }
}
