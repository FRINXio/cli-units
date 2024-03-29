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
import io.frinx.cli.unit.saos8.network.instance.handler.ifc.InterfaceConfigWriter;
import io.frinx.cli.unit.saos8.network.instance.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.relayagent.RelayAgentVirtualSwitchConfigReader;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.relayagent.RelayAgentVirtualSwitchConfigWriter;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.VirtualRingConfigReader;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.VirtualRingConfigWriter;
import io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.VirtualRingReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosNetworkInstanceUnit extends AbstractUnit {

    public SaosNetworkInstanceUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Set.of(SaosDevices.SAOS_8);
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
                IIDs.FRINX_SAOS_RA_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);
        writeRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_NE_AUG_SAOS8VRAUG_VI_VIRTUALRING);
        writeRegistry.add(IIDs.NE_NE_AUG_SAOS8VRAUG_VI_VI_CONFIG, new VirtualRingConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.addNoop(IIDs.NE_NE_IN_INTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_IN_IN_CO_AUG_SAOS8NIIFCAUG), IIDs.NE_NE_AUG_SAOS8VRAUG_VI_VI_CONFIG,
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG,
                io.frinx.openconfig.openconfig.network.instance.IIDs
                        .IN_IN_SU_SU_VL_AUG_SAOS8VLANLOGICALELEMENTSAUG_CL_CONFIG,
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_AUG_RASAOSAUG_RELAYAGENT);
        writeRegistry.add(IIDs.NE_NE_AUG_RASAOSAUG_RE_CONFIG, new RelayAgentVirtualSwitchConfigWriter(cli));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_AUG_SAOS8VRAUG_VI_VIRTUALRING, new VirtualRingReader(cli));
        readRegistry.add(IIDs.NE_NE_AUG_SAOS8VRAUG_VI_VI_CONFIG, new VirtualRingConfigReader());

        readRegistry.subtreeAdd(IIDs.NE_NE_IN_INTERFACE, new InterfaceReader(cli),
                Sets.newHashSet(IIDs.NE_NE_IN_IN_CONFIG, IIDs.NE_NE_IN_IN_CO_AUG_SAOS8NIIFCAUG));

        readRegistry.add(IIDs.NE_NE_AUG_RASAOSAUG_RE_CONFIG, new RelayAgentVirtualSwitchConfigReader(cli));
    }
}