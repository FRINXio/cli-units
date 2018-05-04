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

package io.frinx.cli.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_OSPF_IFC_COST = "show running-config interface %s | include ^ ip ospf cost";

    private final Cli cli;

    public AreaInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getId();

        configBuilder.setId(ifcName);

        String output = blockingRead(String.format(SHOW_OSPF_IFC_COST, ifcName), cli, instanceIdentifier, readContext);

        if (!output.isEmpty()) {
            configBuilder.setMetric(new OspfMetric(Integer.valueOf
                    (output.replaceFirst("ip ospf cost ", "").trim())));
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((InterfaceBuilder) builder).setConfig(config);
    }
}
