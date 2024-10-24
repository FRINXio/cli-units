/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lr.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.STATIC;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LrProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    public LrProtocolReader(Cli cli) {
    }

    @NotNull
    @Override
    public List<ProtocolKey> getAllIds(@NotNull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @NotNull ReadContext readContext)
            throws ReadFailedException {
        List<ProtocolKey> rtn = new ArrayList<ProtocolKey>();
        String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (vrfId.equals("default")) {
            rtn.add(new ProtocolKey(STATIC.class, "default"));
        }
        return rtn;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Protocol> instanceIdentifier,
                                             @NotNull ProtocolBuilder protocolBuilder,
                                             @NotNull ReadContext readContext) {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        protocolBuilder.setIdentifier(key.getIdentifier());
        protocolBuilder.setName(key.getName());
    }

    @Override
    public Check getCheck() {
        return ChecksMap.PathCheck.Protocol.STATIC_ROUTING;
    }
}