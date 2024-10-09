/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos8.ifc.handler.InterfaceListConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.InterfaceListConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.InterfaceListReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.portqueuegroup.PortQueueGroupPmInstanceConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.portqueuegroup.PortQueueGroupPmInstanceConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.portqueuegroup.PortQueueGroupPmInstanceReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortPmInstanceConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortPmInstanceReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortRelayAgentConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortRelayAgentConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortVlanElementsConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortVlanReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosInterfaceUnit extends AbstractUnit {

    public SaosInterfaceUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Sets.newHashSet(SaosDevices.SAOS_8);
    }

    @Override
    protected String getUnitName() {
        return "SAOS-8 Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_IF_AGGREGATE_EXTENSION,
                IIDs.FRINX_SAOS_IF_EXTENSION,
                io.frinx.openconfig.openconfig.vlan.IIDs.FRINX_OPENCONFIG_VLAN,
                io.frinx.openconfig.openconfig.network.instance.IIDs.FRINX_SAOS_VLAN_EXTENSION,
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
        writeRegistry.addNoop(IIDs.INTERFACES);
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_CONFIG, new InterfaceListConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_CO_AUG_IFSAOSAUG),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.IN_IN_ET_CO_AUG_CONFIG1);
        // sub-port of the lag interface
        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.IN_IN_SU_SU_CONFIG, new SubPortConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_SU_SU_CO_AUG_SAOSSUBIFCONFIGAUG),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_AUG_SAOS8VRAUG_VI_VI_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig.network.instance.IIDs
                .IN_IN_SU_SU_VL_AUG_SAOS8VLANLOGICALELEMENTSAUG_CLASSELEMENT);
        writeRegistry.addAfter(io.frinx.openconfig.openconfig.network.instance.IIDs
                        .IN_IN_SU_SU_VL_AUG_SAOS8VLANLOGICALELEMENTSAUG_CL_CONFIG,
                new SubPortVlanElementsConfigWriter(cli), IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.addNoop(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PMINSTANCES);
        writeRegistry.addNoop(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PMINSTANCE);
        writeRegistry.add(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PM_CONFIG,
                new SubPortPmInstanceConfigWriter(cli));

        writeRegistry.addNoop(IIDs.IN_IN_AUG_SAOS8PMINSTANCEIFAUG_PMINSTANCES);
        writeRegistry.addNoop(IIDs.IN_IN_AUG_SAOS8PMINSTANCEIFAUG_PM_PMINSTANCE);
        writeRegistry.add(IIDs.IN_IN_AUG_SAOS8PMINSTANCEIFAUG_PM_PM_CONFIG,
                new PortQueueGroupPmInstanceConfigWriter(cli));

        writeRegistry.addAfter(IIDs.IN_IN_SU_SU_AUG_SAOS8RELAYAGENTAUG_RE_CONFIG,
                new SubPortRelayAgentConfigWriter(cli),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_IN_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceListReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceListConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_ET_CO_AUG_CONFIG1, new AggregateConfigReader(cli));

        readRegistry.add(IIDs.IN_IN_AUG_SAOS8PMINSTANCEIFAUG_PM_PMINSTANCE,
                new PortQueueGroupPmInstanceReader(cli));
        readRegistry.add(IIDs.IN_IN_AUG_SAOS8PMINSTANCEIFAUG_PM_PM_CONFIG,
                new PortQueueGroupPmInstanceConfigReader(cli));

        // sub-port of the lag interface
        readRegistry.subtreeAdd(IIDs.IN_IN_SU_SUBINTERFACE, new SubPortReader(cli),
                Sets.newHashSet(IIDs.IN_IN_SU_SU_CONFIG, IIDs.IN_IN_SU_SU_CO_AUG_SAOSSUBIFCONFIGAUG));
        readRegistry.subtreeAdd(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VLAN,
                new SubPortVlanReader(cli), Sets.newHashSet(
                        io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG));
        readRegistry.subtreeAdd(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PMINSTANCE,
                new SubPortPmInstanceReader(cli), Sets.newHashSet(
                        IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PM_CONFIG));

        readRegistry.add(IIDs.IN_IN_SU_SU_AUG_SAOS8RELAYAGENTAUG_RE_CONFIG, new SubPortRelayAgentConfigReader(cli));
    }
}