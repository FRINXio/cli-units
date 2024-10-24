/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.cable;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.cer.cable.handler.cablemac.CableMacReader;
import io.frinx.cli.unit.cer.cable.handler.cablemac.CableMacStateReader;
import io.frinx.cli.unit.cer.cable.handler.cablemodem.CableModemReader;
import io.frinx.cli.unit.cer.cable.handler.cablemodem.CableModemStateReader;
import io.frinx.cli.unit.cer.cable.handler.fibernode.FiberNodeConfigReader;
import io.frinx.cli.unit.cer.cable.handler.fibernode.FiberNodeConfigWriter;
import io.frinx.cli.unit.cer.cable.handler.fibernode.FiberNodeReader;
import io.frinx.cli.unit.cer.init.CerDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.cable.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class CerCableUnit extends AbstractUnit {

    public CerCableUnit(@NotNull final TranslationUnitCollector translationRegistry) {
        super(translationRegistry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return CerDevices.CER_ALL;
    }

    @Override
    protected String getUnitName() {
        return "CER Cable (Openconfig) translation unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_CABLE,
                IIDs.FRINX_CER_CABLE_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.CABLE);
        writeRegistry.addNoop(IIDs.CA_FIBERNODES);
        writeRegistry.addNoop(IIDs.CA_FI_FIBERNODE);
        writeRegistry.subtreeAdd(IIDs.CA_FI_FI_CONFIG, new FiberNodeConfigWriter(cli),
                Sets.newHashSet(IIDs.CA_FI_FI_CO_AUG_FIBERNODECONFIGAUG,
                        IIDs.CA_FI_FI_CO_AUG_FIBERNODECONFIGAUG_RPD));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.CA_FI_FIBERNODE, new FiberNodeReader(cli));
        readRegistry.add(IIDs.CA_FI_FI_CONFIG, new FiberNodeConfigReader(cli));

        readRegistry.add(IIDs.CA_AUG_CABLE1_CA_CABLEMAC, new CableMacReader(cli));
        readRegistry.add(IIDs.CA_AUG_CABLE1_CA_CA_STATE, new CableMacStateReader(cli));
        readRegistry.add(IIDs.CA_AUG_CABLE2_CA_CABLEMODEM, new CableModemReader(cli));
        readRegistry.add(IIDs.CA_AUG_CABLE2_CA_CA_STATE, new CableModemStateReader(cli));
    }
}