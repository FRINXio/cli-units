/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliOperReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.StateBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfStateReader implements CliOperReader<State, StateBuilder>,
        CompositeReader.Child<State, StateBuilder> {

    private Cli cli;

    public VrfStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder stateBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isVrf(instanceIdentifier, readContext)) {
            stateBuilder.setName(instanceIdentifier.firstKeyOf(NetworkInstance.class).getName());
            stateBuilder.setType(L3VRF.class);

            // TODO set other attributes i.e. description
        }
    }

    private boolean isVrf(InstanceIdentifier<State> id, ReadContext readContext) throws ReadFailedException {
        return VrfReader.getAllIds(id, readContext, cli, this).contains(id.firstKeyOf(NetworkInstance.class));
    }
}
