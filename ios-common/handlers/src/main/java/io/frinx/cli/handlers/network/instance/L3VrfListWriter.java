/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.network.instance;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.registry.common.TypedWriter;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.cli.unit.utils.CliWriter;

public interface L3VrfListWriter<O extends DataObject & Identifiable<K>, K extends Identifier<O>> extends TypedWriter<O>, CliWriter<O> {

    @Nullable
    @Override
    default Map.Entry<InstanceIdentifier<? extends DataObject>, Function<DataObject, Boolean>> getParentCheck(InstanceIdentifier<O> id) {
        return new AbstractMap.SimpleEntry<>(
            RWUtils.cutId(id, NetworkInstance.class).child(Config.class),
            L3VrfReader.L3VRF_CHECK);
    }
}
