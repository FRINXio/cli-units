/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.network.instance;

import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.registry.common.TypedReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.CliOperReader;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.DEFAULTINSTANCE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface L3VrfReader<O extends DataObject, B extends Builder<O>> extends TypedReader<O, B> {

    // Vrf readers are also available for default network instance
    Function<DataObject, Boolean> L3VRF_CHECK = config -> ((Config) config).getType() == L3VRF.class || ((Config) config).getType() == DEFAULTINSTANCE.class;

    @Nullable
    @Override
    default Map.Entry<InstanceIdentifier<? extends DataObject>, Function<DataObject, Boolean>> getParentCheck(InstanceIdentifier<O> id) {
        return new AbstractMap.SimpleEntry<>(
                RWUtils.cutId(id, NetworkInstance.class).child(Config.class),
                L3VRF_CHECK);
    }

    interface L3VrfConfigReader<O extends DataObject, B extends Builder<O>> extends L3VrfReader<O, B>, CliConfigReader<O, B> {}
    interface L3VrfOperReader<O extends DataObject, B extends Builder<O>> extends L3VrfReader<O, B>, CliOperReader<O, B> {}
}
