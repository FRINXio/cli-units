/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.ios.local.routing.common.LrReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.StateBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticStateReader implements LrReader<State, StateBuilder> {

    @Nonnull
    @Override
    public StateBuilder getBuilder(@Nonnull InstanceIdentifier<State> instanceIdentifier) {
        return new StateBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier, @Nonnull StateBuilder stateBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        IpPrefix ipPrefix = instanceIdentifier.firstKeyOf(Static.class).getPrefix();
        stateBuilder.setPrefix(ipPrefix);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State state) {
        ((StaticBuilder) builder).setState(state);
    }
}
