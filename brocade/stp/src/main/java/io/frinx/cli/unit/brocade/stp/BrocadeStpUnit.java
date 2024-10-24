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

package io.frinx.cli.unit.brocade.stp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.brocade.init.BrocadeDevices;
import io.frinx.cli.unit.brocade.stp.handler.StpInterfaceConfigReader;
import io.frinx.cli.unit.brocade.stp.handler.StpInterfaceConfigWriter;
import io.frinx.cli.unit.brocade.stp.handler.StpInterfaceReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.stp.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class BrocadeStpUnit extends AbstractUnit {

    public BrocadeStpUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return BrocadeDevices.BROCADE_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Ironware STP (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_STP,
                IIDs.FRINX_OPENCONFIG_LLDP
        );
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writerRegistry, Cli cli) {
        writerRegistry.addNoop(IIDs.ST_IN_INTERFACE);
        writerRegistry.addAfter(IIDs.ST_IN_IN_CONFIG, new StpInterfaceConfigWriter(cli),
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.ST_IN_INTERFACE, new StpInterfaceReader(cli));
        readRegistry.add(IIDs.ST_IN_IN_CONFIG, new StpInterfaceConfigReader());
    }
}