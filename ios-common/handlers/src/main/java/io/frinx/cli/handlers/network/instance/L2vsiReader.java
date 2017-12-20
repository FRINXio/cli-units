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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface L2vsiReader<O extends DataObject, B extends Builder<O>> extends TypedReader<O, B> {

    Function<DataObject, Boolean> L2VSI_CHECK = config -> ((Config) config).getType() == L2VSI.class;

    @Nullable
    @Override
    default Map.Entry<InstanceIdentifier<? extends DataObject>, Function<DataObject, Boolean>> getParentCheck(InstanceIdentifier<O> id) {
        return new AbstractMap.SimpleEntry<>(
                RWUtils.cutId(id, NetworkInstance.class).child(Config.class),
                L2VSI_CHECK);
    }

    interface L2vsiConfigReader<O extends DataObject, B extends Builder<O>> extends L2vsiReader<O, B>, CliConfigReader<O, B> {}
    interface L2vsiVrfOperReader<O extends DataObject, B extends Builder<O>> extends L2vsiReader<O, B>, CliOperReader<O, B> {}
}
