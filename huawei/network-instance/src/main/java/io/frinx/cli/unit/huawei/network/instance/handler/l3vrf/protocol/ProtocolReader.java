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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.protocol;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.ListReaderCustomizer;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
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
    public List<ProtocolKey> getAllIdsForType(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier, @Nonnull
            ReadContext readContext) throws ReadFailedException {
        return delegate.getAllIds(instanceIdentifier, readContext);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Protocol> readData) {
        delegate.merge(builder, readData);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier, @Nonnull
            ProtocolBuilder protocolBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        delegate.readCurrentAttributes(instanceIdentifier, protocolBuilder, readContext);
    }

    public static class ProtocolReaderComposite extends CompositeListReader<Protocol, ProtocolKey, ProtocolBuilder>
            implements CliConfigListReader<Protocol, ProtocolKey, ProtocolBuilder> {

        ProtocolReaderComposite(Cli cli) {
                    super(new ArrayList<ListReaderCustomizer<Protocol, ProtocolKey, ProtocolBuilder>>() {{
                            add(new BgpProtocolReader(cli));
                        }
                    });
        }

        @Override
        public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Protocol> list) {
            ((ProtocolsBuilder) builder).setProtocol(list);
        }
    }
}
