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

package io.frinx.cli.ios.local.routing.common;

import io.frinx.cli.registry.common.TypedListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.CliOperListReader;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

public interface LrListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
        extends LrReader<O, B>, TypedListReader<O, K, B>, CliListReader<O, K, B> {

    interface LrConfigListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
            extends LrListReader<O, K, B>, CliConfigListReader<O, K, B> {}

    interface LrOperListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
            extends LrListReader<O, K, B>, CliOperListReader<O, K, B> {}
}
