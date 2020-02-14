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

package io.frinx.cli.unit.saos.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.saos.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.saos.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.saos.network.instance.handler.cp.ConnectionPointsReader;
import io.frinx.cli.unit.saos.network.instance.handler.cp.ConnectionPointsWriter;
import io.frinx.cli.unit.saos.network.instance.handler.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.saos.network.instance.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.saos.network.instance.handler.ifc.InterfaceWriter;
import io.frinx.cli.unit.saos.network.instance.handler.vlan.VlanConfigReader;
import io.frinx.cli.unit.saos.network.instance.handler.vlan.VlanConfigWriter;
import io.frinx.cli.unit.saos.network.instance.handler.vlan.VlanReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
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
        return SaosDevices.SAOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 Network Instance (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE,
                IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE_TYPES,
                IIDs.FRINX_SAOS_VS_EXTENSION,
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
        // No handling required on the network instance level
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);
        writeRegistry.subtreeAdd(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli),
                Collections.singleton(IIDs.NE_NE_CO_AUG_VSSAOSAUG));

        // vlan
        writeRegistry.addNoop(IIDs.NE_NE_VL_VLAN);
        writeRegistry.addAfter(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigWriter(cli), IIDs.NE_NE_CONFIG);

        // virtual-switch
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_CONNECTIONPOINTS,
                new ConnectionPointsWriter(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_CO_CONNECTIONPOINT,
                        IIDs.NE_NE_CO_CO_CONFIG),
                /*handle after network instance configuration*/ IIDs.NE_NE_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.addAfter(IIDs.NE_NE_IN_INTERFACE, new InterfaceWriter(cli),
                IIDs.NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_IN_IN_CONFIG);

    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_VL_VLAN, new VlanReader(cli));
        readRegistry.add(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigReader(cli));


        // virtual-switch
        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_IN_IN_CONFIG, new InterfaceConfigReader());
        readRegistry.subtreeAdd(IIDs.NE_NE_CONNECTIONPOINTS, new ConnectionPointsReader(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_CO_CONNECTIONPOINT,
                        IIDs.NE_NE_CO_CO_CONFIG));
    }
}