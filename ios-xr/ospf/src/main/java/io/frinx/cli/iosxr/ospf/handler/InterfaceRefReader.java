/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.io.frinx.cli.handlers.ospf.OspfReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRefBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222._interface.ref._interface.ref.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222._interface.ref._interface.ref.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class InterfaceRefReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        configBuilder.setInterface(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((InterfaceRefBuilder) parentBuilder).setConfig(readValue);
    }
}
