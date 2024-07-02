/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolWriter implements CliWriter<Config>, CompositeWriter.Child<Config> {

    private Cli cli;

    public OspfProtocolWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = """
            configure terminal
            router ospf {$ospf}{.if ($vrf) } vrf {$vrf}{/if}
            end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            no router ospf {$ospf}{.if ($vrf) } vrf {$vrf}{/if}
            end""";

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                 @NotNull Config config,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "ospf", protocolName,
                        "vrf", vrfName.equals(NetworInstance.DEFAULT_NETWORK_NAME) ? null : vrfName));
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(iid, writeContext, false)) {
            return false;
        }

        // NOOP
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                  @NotNull Config config,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(instanceIdentifier, writeContext, true)) {
            return false;
        }

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE,
                        "ospf", protocolName,
                        "vrf", vrfName.equals(NetworInstance.DEFAULT_NETWORK_NAME) ? null : vrfName));
        return true;
    }
}