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

package io.frinx.cli.unit.saos8.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos8.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.saos8.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.saos8.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.saos8.network.instance.handler.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.saos8.network.instance.handler.ifc.InterfaceConfigWriter;
import io.frinx.cli.unit.saos8.network.instance.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.VirtualRingConfigReader;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.VirtualRingConfigWriter;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.VirtualRingReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosNetworkInstanceUnit extends AbstractUnit {

    public SaosNetworkInstanceUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return new HashSet<Device>() {
            {
                add(SaosDevices.SAOS_8);
            }
        };
    }

    @Override
    protected String getUnitName() {
        return "Saos-8 Network Instance (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE,
                IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE_TYPES,
                IIDs.FRINX_SAOS_NETWORK_INSTANCE_TYPE_EXTENSION,
                IIDs.FRINX_SAOS_VIRTUAL_RING_EXTENSION,
                IIDs.FRINX_SAOS_NI_IF_EXTENSION,
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
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);
        writeRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_NE_AUG_SAOS8VRAUG_RI_RING);
        writeRegistry.add(IIDs.NE_NE_AUG_SAOS8VRAUG_RI_RI_CONFIG, new VirtualRingConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.addNoop(IIDs.NE_NE_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.NE_NE_IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_IN_IN_CO_AUG_SAOS8NIIFCAUG));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_AUG_SAOS8VRAUG_RI_RING, new VirtualRingReader(cli));
        readRegistry.add(IIDs.NE_NE_AUG_SAOS8VRAUG_RI_RI_CONFIG, new VirtualRingConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_IN_IN_CONFIG, new InterfaceConfigReader(cli));
    }
}