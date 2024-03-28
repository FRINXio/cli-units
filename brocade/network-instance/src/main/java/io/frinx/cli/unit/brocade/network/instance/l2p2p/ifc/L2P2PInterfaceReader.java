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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.ifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PInterfaceReader implements CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder>,
        CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    public static final Check L2P2P_CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P);

    @Override
    public Check getCheck() {
        return L2P2P_CHECK;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext ctx) {
        final NetworkInstanceKey name = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        return ctx.read(IidUtils.createIid(IIDs.NE_NE_CONNECTIONPOINTS, name))
                .stream()
                .flatMap(cps -> cps.getConnectionPoint().stream())
                .map(ConnectionPoint::getEndpoints)
                .flatMap(eps -> eps.getEndpoint().stream())
                .map(this::getIfc)
                .filter(Objects::nonNull)
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Nullable
    private String getIfc(Endpoint ep) {
        if (ep.getLocal() != null && ep.getLocal().getConfig() != null) {
            return ep.getLocal().getConfig().getInterface();
        }

        return null;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext ctx) {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }
}