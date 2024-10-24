/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.isis;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.brocade.init.BrocadeDevices;
import io.frinx.cli.unit.brocade.isis.handler.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.brocade.isis.handler.ifc.InterfaceConfigWriter;
import io.frinx.cli.unit.brocade.isis.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class BrocadeIsisUnit extends AbstractUnit {

    public BrocadeIsisUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return BrocadeDevices.BROCADE_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Ironware IS-IS unit";
    }

    @Override
    public void provideHandlers(
        @NotNull CustomizerAwareReadRegistryBuilder readRegistry,
        @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
        @NotNull Context context) {

        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder rreg, Cli cli) {
        rreg.addAfter(IIDs.NE_NE_PR_PR_IS_IN_INTERFACE, new InterfaceReader(cli), IIDs.NE_NE_PR_PR_CONFIG);
        rreg.addAfter(IIDs.NE_NE_PR_PR_IS_IN_IN_CONFIG, new InterfaceConfigReader(cli), IIDs.NE_NE_PR_PR_CONFIG);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder wreg, Cli cli) {
        wreg.addNoop(IIDs.NE_NE_PR_PR_ISIS);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_IN_INTERFACE);
        wreg.addAfter(IIDs.NE_NE_PR_PR_IS_IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
            io.frinx.openconfig.openconfig.isis.IIDs.FRINX_OPENCONFIG_ISIS,
            io.frinx.openconfig.openconfig.isis.IIDs.FRINX_OPENCONFIG_ISIS_TYPES,
            IIDs.FRINX_ISIS_EXTENSION);
    }
}