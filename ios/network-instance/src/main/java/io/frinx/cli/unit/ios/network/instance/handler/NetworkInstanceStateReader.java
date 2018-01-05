/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.frinx.cli.handlers.def.DefaultStateReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.L2VSIStateReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.VrfStateReader;
import io.frinx.cli.unit.utils.CliOperReader;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkInstanceStateReader extends CompositeReader<State, StateBuilder>
    implements CliOperReader<State, StateBuilder> {

    private Cli cli;

    public NetworkInstanceStateReader(Cli cli) {
        super(new ArrayList<ReaderCustomizer<State, StateBuilder>>() {{
            add(new VrfStateReader(cli));
            add(new DefaultStateReader());
            add(new L2P2PStateReader(cli));
            add(new L2VSIStateReader(cli));
        }});
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State config) {
        ((NetworkInstanceBuilder) builder).setState(config);
    }
}
