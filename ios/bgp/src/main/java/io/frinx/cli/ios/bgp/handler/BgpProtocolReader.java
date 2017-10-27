/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import com.google.common.collect.ImmutableList;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliListReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        io.frinx.cli.ios.bgp.common.BgpReader.BgpConfigReader<Protocol, ProtocolBuilder>,
        CompositeReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    public static final String DEFAULT_BGP_INSTANCE = "default";
    private final Cli cli;

    public BgpProtocolReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> iid,
                                       @Nonnull ReadContext context) throws ReadFailedException {
        String output = blockingRead("show run | sec bgp", cli, iid, context);
        if (output.isEmpty()) {
            return Collections.emptyList();
        }

        // FIXME implement BGP VRF-awareness
        if (!DEFAULT_NETWORK.equals(iid.firstKeyOf(NetworkInstance.class))) {
            LOG.info("BGP VRF-aware is not implemented yet.");
            return Collections.emptyList();
        }
        // IOS does not support multi-instance BGP therefore there is only default instance
        return ImmutableList.of(new ProtocolKey(TYPE, DEFAULT_BGP_INSTANCE));
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> iid,
                                             @Nonnull ProtocolBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        // FIXME implement BGP VRF-awareness
        if (!DEFAULT_NETWORK.equals(iid.firstKeyOf(NetworkInstance.class))) {
            LOG.info("BGP VRF-aware is not implemented yet.");
            return;
        }

        ProtocolKey key = iid.firstKeyOf(Protocol.class);
        builder.setName(key.getName());
        builder.setIdentifier(key.getIdentifier());
    }
}
