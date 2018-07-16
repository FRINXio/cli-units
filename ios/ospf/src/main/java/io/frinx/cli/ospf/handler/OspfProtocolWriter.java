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

package io.frinx.cli.ospf.handler;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolWriter implements OspfWriter<Config> {

    private Cli cli;

    public OspfProtocolWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "router ospf {$ospf}"
            + "{.if ($vrf) } vrf {$vrf}{/if}"
            + "\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no router ospf {$ospf}"
            + "{.if ($vrf) } vrf {$vrf}{/if}"
            + "\n"
            + "end";

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                              WriteContext writeContext) throws WriteFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "ospf", protocolName,
                        "vrf", vrfName.equals(DEFAULT_NETWORK_NAME) ? null : vrfName));
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                               WriteContext writeContext) throws WriteFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE,
                        "ospf", protocolName,
                        "vrf", vrfName.equals(DEFAULT_NETWORK_NAME) ? null : vrfName));
    }
}
