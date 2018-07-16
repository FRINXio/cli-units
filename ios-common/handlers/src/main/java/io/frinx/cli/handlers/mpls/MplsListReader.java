/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.handlers.mpls;

import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.registry.common.TypedListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliOperListReader;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface MplsListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends
        Builder<O>> extends TypedListReader<O, K, B> {

    @Nullable
    @Override
    default Map.Entry<InstanceIdentifier<? extends DataObject>, Function<DataObject, Boolean>>
        getParentCheck(InstanceIdentifier<O> id) {
        return new AbstractMap.SimpleEntry<>(
                RWUtils.cutId(id, NetworkInstance.class)
                        .child(Config.class),
                MplsReader.MPLS_CHECK);
    }

    interface MplsConfigListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends
            Builder<O>> extends MplsListReader<O, K, B>, CliConfigListReader<O, K, B> {
    }

    interface MplsOperListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends
            Builder<O>> extends MplsListReader<O, K, B>, CliOperListReader<O, K, B> {
    }
}
