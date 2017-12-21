/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeWriter;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsWriter;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp.L2VSIConnectionPointsWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;

public class ConnectionPointsWriter extends CompositeWriter<ConnectionPoints> {

    public ConnectionPointsWriter(Cli cli) {
        super(Lists.newArrayList(
                new L2P2PConnectionPointsWriter(cli),
                new L2VSIConnectionPointsWriter(cli)));
    }
}
