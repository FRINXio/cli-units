/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance.handler.def;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliOperReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.StateBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.DEFAULTINSTANCE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class DefaultStateReader implements CliOperReader<State, StateBuilder>,
        CompositeReader.Child<State, StateBuilder> {

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (DefaultConfigReader.isDefault(instanceIdentifier)) {
            configBuilder.setName(instanceIdentifier.firstKeyOf(NetworkInstance.class).getName());
            configBuilder.setType(DEFAULTINSTANCE.class);
        }
    }

}
