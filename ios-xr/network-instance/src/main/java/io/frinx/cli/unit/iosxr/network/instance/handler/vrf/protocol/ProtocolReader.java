/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.ListReaderCustomizer;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolReader implements L3VrfListReader.L3VrfConfigListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    private final ProtocolReaderComposite delegate;

    public ProtocolReader(Cli cli) {
        // Wrapping the composite reader into a typed reader to ensure network instance type first
        delegate = new ProtocolReaderComposite(cli);
    }

    @Override
    public List<ProtocolKey> getAllIdsForType(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        return delegate.getAllIds(instanceIdentifier, readContext);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Protocol> readData) {
        delegate.merge(builder, readData);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier, @Nonnull ProtocolBuilder protocolBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        delegate.readCurrentAttributes(instanceIdentifier, protocolBuilder, readContext);
    }

    @Nonnull
    @Override
    public ProtocolBuilder getBuilder(@Nonnull InstanceIdentifier<Protocol> id) {
        return delegate.getBuilder(id);
    }

    public static class ProtocolReaderComposite extends CompositeListReader<Protocol, ProtocolKey, ProtocolBuilder>
            implements CliConfigListReader<Protocol, ProtocolKey, ProtocolBuilder> {

        public ProtocolReaderComposite(Cli cli) {
            super(new ArrayList<ListReaderCustomizer<Protocol, ProtocolKey, ProtocolBuilder>>() {{

            }});
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
    }
}
