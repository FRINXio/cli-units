/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing;

import com.google.common.collect.ImmutableList;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliListReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.policy.types.rev160512.STATIC;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class StaticLocalRoutingProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    public static final Class<STATIC> TYPE = STATIC.class;
    private static final NetworkInstanceKey DEFAULT_NETWORK_INSTANCE_KEY = new NetworkInstanceKey("default");

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> id, @Nonnull ReadContext context) throws ReadFailedException {
        if (!DEFAULT_NETWORK_INSTANCE_KEY.equals(id.firstKeyOf(NetworkInstance.class))) {

            return Collections.emptyList();
        }

        return ImmutableList.of(new ProtocolKey(TYPE, "default"));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Protocol> list) {
        ((ProtocolsBuilder) builder).setProtocol(list);
    }

    @Nonnull
    @Override
    public ProtocolBuilder getBuilder(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier) {

        return new ProtocolBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                      @Nonnull ProtocolBuilder protocolBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        if (key.getIdentifier().equals(TYPE)) {
            protocolBuilder.setName(key.getName());
            protocolBuilder.setIdentifier(key.getIdentifier());
            // FIXME set attributes
        }


    }
}
