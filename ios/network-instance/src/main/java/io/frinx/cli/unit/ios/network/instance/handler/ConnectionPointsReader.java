/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp.L2VSIConnectionPointsReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConnectionPointsReader extends CompositeReader<ConnectionPoints, ConnectionPointsBuilder>
        implements CliConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    public ConnectionPointsReader(Cli cli) {
        super(new ArrayList<ReaderCustomizer<ConnectionPoints, ConnectionPointsBuilder>>() {{
            add(new L2P2PConnectionPointsReader(cli));
            add(new L2VSIConnectionPointsReader(cli));
        }});
    }

    @Nonnull
    @Override
    public ConnectionPointsBuilder getBuilder(@Nonnull InstanceIdentifier<ConnectionPoints> id) {
        return new ConnectionPointsBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull ConnectionPoints readValue) {
        ((NetworkInstanceBuilder) parentBuilder).setConnectionPoints(readValue);
    }
}
