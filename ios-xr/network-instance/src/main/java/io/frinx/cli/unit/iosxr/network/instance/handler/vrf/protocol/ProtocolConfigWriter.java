/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.BgpProtocolWriter;
import io.frinx.cli.iosxr.ospf.handler.OspfProtocolWriter;
import io.frinx.cli.registry.common.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.INSTALLPROTOCOLTYPE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolConfigWriter extends CompositeWriter {

    public ProtocolConfigWriter(final Cli cli) {
        super(Lists.newArrayList(
            new BgpProtocolWriter(),
            new OspfProtocolWriter(cli)
        ));
    }

    public static boolean checkProtocolType(InstanceIdentifier<?> id, Class<? extends INSTALLPROTOCOLTYPE> type) {
        ProtocolKey protocolKey = id.firstKeyOf(Protocol.class);

        return protocolKey != null && type.equals(protocolKey.getIdentifier());
    }
}
