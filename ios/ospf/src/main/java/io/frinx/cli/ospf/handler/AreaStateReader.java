/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import javax.annotation.Nonnull;

import io.frinx.cli.handlers.io.frinx.cli.handlers.ospf.OspfReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaStateReader implements OspfReader.OspfOperReader<State, StateBuilder> {

    // FIXME Duplicate code with config

    @Nonnull
    @Override
    public StateBuilder getBuilder(@Nonnull InstanceIdentifier<State> instanceIdentifier) {
        return new StateBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier, @Nonnull StateBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        configBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class).getIdentifier());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State config) {
        ((AreaBuilder) builder).setState(config);
    }
}
