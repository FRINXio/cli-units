/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf.table;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.table.BgpTableConnectionWriter;
import io.frinx.cli.ospf.handler.table.OspfTableConnectionWriter;
import io.frinx.cli.registry.common.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;

public class TableConnectionConfigWriter extends CompositeWriter<Config> {

    public TableConnectionConfigWriter(Cli cli) {
        super(Lists.newArrayList(
                new BgpTableConnectionWriter(cli),
                new OspfTableConnectionWriter(cli)
        ));
    }


}
