/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.ospf;

import io.frinx.cli.registry.common.TypedWriter;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;

public interface OspfWriter <O extends DataObject> extends TypedWriter<O>, CliWriter<O> {

    @Override
    default Identifier<? extends DataObject> getKey() {
        // TODO We can define this as constant or extract this to some
        // super interface common for all OSPF typed handlers
        return new ProtocolKey(OspfReader.TYPE, null);
    }
}
