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
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalStateReader implements OspfReader.OspfOperReader<State, StateBuilder> {

    private Cli cli;

    public GlobalStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                             @Nonnull StateBuilder stateBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        // FIXME Set config attributes from operational state e.g. use sh ip vrf here instead of sh run vrf
        stateBuilder.fieldsFrom(ctx.read(RWUtils.cutId(instanceIdentifier, IIDs.NE_NE_PR_PR_OS_GLOBAL).child(Config.class)).get());
        // TODO set state attributes
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State config) {
        ((GlobalBuilder) builder).setState(config);
    }
}
