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

package io.frinx.cli.unit.brocade.network.instance;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.L2P2PReader;
import io.frinx.cli.unit.brocade.network.instance.l2vsi.L2VSIReader;
import io.frinx.cli.unit.brocade.network.instance.vrf.L3VrfReader;
import io.frinx.cli.unit.handlers.def.DefaultReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class NetworkInstanceReader
        extends CompositeListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>
        implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    public NetworkInstanceReader(Cli cli) {
        super(List.of(
            new L2P2PReader(cli),
            new L2VSIReader(cli),
            new L3VrfReader(cli),
            new DefaultReader()
        ));
    }

    @NotNull
    @Override
    public List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        // Caching here to speed up reading
        if (readContext.getModificationCache().get(this) != null) {
            return (List<NetworkInstanceKey>) readContext.getModificationCache().get(this);
        }

        List<NetworkInstanceKey> allIds = super.getAllIds(instanceIdentifier, readContext);
        readContext.getModificationCache().put(this, allIds);
        return allIds;
    }
}