/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.logical.ring;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos.logical.ring.handler.LogicalRingConfigReader;
import io.frinx.cli.unit.saos.logical.ring.handler.LogicalRingConfigWriter;
import io.frinx.cli.unit.saos.logical.ring.handler.LogicalRingReader;
import io.frinx.cli.unit.saos.logical.ring.handler.virtual.VirtualRingConfigReader;
import io.frinx.cli.unit.saos.logical.ring.handler.virtual.VirtualRingConfigWriter;
import io.frinx.cli.unit.saos.logical.ring.handler.virtual.VirtualRingReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.ring.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosLogicalRingUnit extends AbstractUnit {

    public SaosLogicalRingUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return SaosDevices.SAOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Saos Logical ring (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_OPENCONFIG_RING,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.LOGICALRINGS);
        writeRegistry.addNoop(IIDs.LO_LOGICALRING);
        // throw exception
        writeRegistry.add(IIDs.LO_LO_CONFIG, new LogicalRingConfigWriter());

        writeRegistry.addNoop(IIDs.LO_LO_VIRTUALRINGS);
        writeRegistry.addNoop(IIDs.LO_LO_VI_VIRTUALRING);
        // throw exception
        writeRegistry.add(IIDs.LO_LO_VI_VI_CONFIG, new VirtualRingConfigWriter());
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.LO_LOGICALRING, new LogicalRingReader(cli));
        readRegistry.add(IIDs.LO_LO_CONFIG, new LogicalRingConfigReader(cli));
        readRegistry.add(IIDs.LO_LO_VI_VIRTUALRING, new VirtualRingReader(cli));
        readRegistry.add(IIDs.LO_LO_VI_VI_CONFIG, new VirtualRingConfigReader());
    }


}
