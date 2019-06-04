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

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolWriter implements CliWriter<Config>, CompositeWriter.Child<Config> {

    private Cli cli;

    public OspfProtocolWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(id, writeContext, false)) {
            return false;
        }

        String vrfId = id.firstKeyOf(NetworkInstance.class)
                .getName();
        final String processName = id.firstKeyOf(Protocol.class)
                .getName();
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfId)) {
            blockingWriteAndRead(cli, id, data,
                    f("router ospf %s", processName),
                    "root");
        } else {
            blockingWriteAndRead(cli, id, data,
                    f("router ospf %s vrf %s", processName, vrfId),
                    "root");
        }
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(id, writeContext, false)) {
            return false;
        }

        // NOOP
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(id, writeContext, true)) {
            return false;
        }

        final String processName = id.firstKeyOf(Protocol.class)
                .getName();
        final String vrfName = OspfProtocolReader.resolveVrfWithName(id);
        blockingWriteAndRead(cli, id, dataBefore,
                f("no router ospf %s %s", processName, vrfName));
        return true;
    }
}
