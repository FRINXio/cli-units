/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.protocol;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeWriter;
import io.frinx.cli.unit.huawei.bgp.handler.BgpProtocolWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;

public class ProtocolConfigWriter extends CompositeWriter<Config> {

    public ProtocolConfigWriter(final Cli cli) {
        super(Lists.newArrayList(
                new BgpProtocolWriter()
        ));
    }
}
