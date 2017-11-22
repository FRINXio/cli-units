/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.bgp;

import io.frinx.cli.registry.common.TypedListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.CliOperListReader;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

public interface BgpListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
        extends BgpReader<O, B>, TypedListReader<O, K, B>, CliListReader<O, K, B> {

    interface BgpConfigListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
            extends BgpListReader<O, K, B>, CliConfigListReader<O, K, B> {}

    interface BgpOperListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
            extends BgpListReader<O, K, B>, CliOperListReader<O, K, B> {}
}
