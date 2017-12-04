/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.mpls;

import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.registry.common.TypedListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliOperListReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;

public interface MplsListReader <O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>> extends TypedListReader<O, K, B> {

    @Nullable
    @Override
    default Map.Entry<InstanceIdentifier<? extends DataObject>, Function<DataObject, Boolean>> getParentCheck(InstanceIdentifier<O> id) {
        return new AbstractMap.SimpleEntry<>(
                RWUtils.cutId(id, NetworkInstance.class).child(Config.class),
                MplsReader.MPLS_CHECK);
    }

    interface MplsConfigListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>> extends MplsListReader<O, K, B>, CliConfigListReader<O, K, B> {
    }

    interface MplsOperListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>> extends MplsListReader<O, K, B>, CliOperListReader<O, K, B> {
    }
}
