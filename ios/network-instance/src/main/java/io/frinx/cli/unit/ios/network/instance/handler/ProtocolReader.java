/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import io.fd.honeycomb.translate.spi.read.ListReaderCustomizer;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.BgpProtocolReader;
import io.frinx.cli.ios.local.routing.StaticLocalRoutingProtocolReader;
import io.frinx.cli.ospf.OspfProtocolReader;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolReader extends CompositeReader<Protocol, ProtocolKey, ProtocolBuilder>
        implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    public ProtocolReader(Cli cli) {
        super(new ArrayList<ListReaderCustomizer<Protocol, ProtocolKey, ProtocolBuilder>>() {{
            add(new OspfProtocolReader(cli));
            add(new BgpProtocolReader(cli));
            add(new StaticLocalRoutingProtocolReader());
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
