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

package io.frinx.cli.handlers.ospf;

import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.translate.unit.commons.handler.spi.TypedReader;
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
    interface OspfConfigReader<O extends DataObject, B extends Builder<O>> extends OspfReader<O, B>,
            CliConfigReader<O, B> {
    }

    interface OspfOperReader<O extends DataObject, B extends Builder<O>> extends OspfReader<O, B>, CliOperReader<O, B> {
    }
}
