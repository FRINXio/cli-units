/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.unit.utils.CliReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder>, CliReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, ConfigBuilder configBuilder, ReadContext readContext) {
        configBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class).getIdentifier());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((AreaBuilder) builder).setConfig(config);
    }
}
