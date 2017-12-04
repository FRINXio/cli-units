/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.io.frinx.cli.handlers.ospf;

import io.frinx.cli.registry.common.TypedReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.CliReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;

public interface OspfReader<O extends DataObject, B extends Builder<O>> extends TypedReader<O, B>, CliReader<O, B> {

    Class<OSPF> TYPE = OSPF.class;

    @Override
    default Identifier<? extends DataObject> getKey() {
        return new ProtocolKey(TYPE, null);
    }

    /**
     * Union mixin of Ospf reader and Config reader.
     */
    interface OspfConfigReader<O extends DataObject, B extends Builder<O>> extends OspfReader<O, B>, CliConfigReader<O, B> {}

    interface OspfOperReader<O extends DataObject, B extends Builder<O>> extends OspfReader<O, B>, CliOperReader<O, B> {}
}
