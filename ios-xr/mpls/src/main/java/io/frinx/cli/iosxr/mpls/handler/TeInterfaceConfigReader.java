/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top._interface.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top._interface.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class TeInterfaceConfigReader implements MplsReader.MplsConfigReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        configBuilder.setInterfaceId(instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId());
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((InterfaceBuilder) parentBuilder).setConfig(readValue);
    }
}
