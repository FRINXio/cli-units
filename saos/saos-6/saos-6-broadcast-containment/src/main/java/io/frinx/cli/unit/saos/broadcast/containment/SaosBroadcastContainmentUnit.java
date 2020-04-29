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

package io.frinx.cli.unit.saos.broadcast.containment;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentFilterConfigReader;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentFilterConfigWriter;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentFilterInterfaceConfigReader;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentFilterInterfaceConfigWriter;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentFilterInterfaceReader;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentFilterReader;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentReader;
import io.frinx.cli.unit.saos.broadcast.containment.handler.BroadcastContainmentWriter;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.broadcast.containment.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosBroadcastContainmentUnit extends AbstractUnit {

    public SaosBroadcastContainmentUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return SaosDevices.SAOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 Network Instance (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_OPENCONFIG_BROADCAST_CONTAINMENT,
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
        writeRegistry.add(IIDs.FILTERS, new BroadcastContainmentWriter(cli));
        writeRegistry.addNoop(IIDs.FI_FILTER);
        writeRegistry.add(IIDs.FI_FI_CONFIG, new BroadcastContainmentFilterConfigWriter(cli));
        writeRegistry.addNoop(IIDs.FI_FI_INTERFACES);
        writeRegistry.addNoop(IIDs.FI_FI_IN_INTERFACE);
        writeRegistry.addAfter(IIDs.FI_FI_IN_IN_CONFIG, new BroadcastContainmentFilterInterfaceConfigWriter(cli),
                IIDs.FI_FI_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.FILTERS, new BroadcastContainmentReader(cli));
        readRegistry.add(IIDs.FI_FILTER, new BroadcastContainmentFilterReader(cli));
        readRegistry.add(IIDs.FI_FI_CONFIG, new BroadcastContainmentFilterConfigReader(cli));
        readRegistry.add(IIDs.FI_FI_IN_INTERFACE, new BroadcastContainmentFilterInterfaceReader(cli));
        readRegistry.add(IIDs.FI_FI_IN_IN_CONFIG, new BroadcastContainmentFilterInterfaceConfigReader());
    }
}