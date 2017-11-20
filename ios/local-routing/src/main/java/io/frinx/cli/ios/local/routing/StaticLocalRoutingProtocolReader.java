/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.ios.local.routing.common.LrReader;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticLocalRoutingProtocolReader implements CliConfigListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        LrReader.LrConfigReader<Protocol, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> id, @Nonnull ReadContext context) throws ReadFailedException {
        String vrf = id.firstKeyOf(NetworkInstance.class).getName();
        return Collections.singletonList(new ProtocolKey(TYPE, vrf));
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                             @Nonnull ProtocolBuilder protocolBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        protocolBuilder.setName(key.getName());
        protocolBuilder.setIdentifier(key.getIdentifier());
        // FIXME set attributes
    }
}
