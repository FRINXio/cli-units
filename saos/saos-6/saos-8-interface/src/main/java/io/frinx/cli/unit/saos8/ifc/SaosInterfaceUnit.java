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
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortPmInstanceConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortPmInstanceConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortPmInstanceReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortVlanConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortVlanConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortVlanElementConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.subport.SubPortVlanReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosInterfaceUnit extends AbstractUnit {

    public SaosInterfaceUnit(@Nonnull TranslationUnitCollector registry) {
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.INTERFACES);
        writeRegistry.addNoop(IIDs.IN_INTERFACE);
        writeRegistry.addAfter(IIDs.IN_IN_CONFIG, new InterfaceListConfigWriter(cli),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.IN_IN_ET_CO_AUG_CONFIG1);
        // sub-port of the lag interface
        writeRegistry.addNoop(IIDs.IN_IN_SU_SUBINTERFACE);
        writeRegistry.subtreeAdd(IIDs.IN_IN_SU_SU_CONFIG, new SubPortConfigWriter(cli),
                Sets.newHashSet(IIDs.IN_IN_SU_SU_CO_AUG_SAOS8SUBIFNAMEAUG));

        writeRegistry.subtreeAddAfter(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubPortVlanConfigWriter(cli), Sets.newHashSet(
                        io.frinx.openconfig.openconfig.network.instance.IIDs
                                .IN_IN_SU_SU_VL_CO_AUG_SAOS8VLANLOGICALAUG), IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.addNoop(io.frinx.openconfig.openconfig.network.instance.IIDs
                .IN_IN_SU_SU_VL_AUG_SAOS8VLANLOGICALELEMENTSAUG_CLASSELEMENTS);
        writeRegistry.addNoop(io.frinx.openconfig.openconfig.network.instance.IIDs
                .IN_IN_SU_SU_VL_AUG_SAOS8VLANLOGICALELEMENTSAUG_CL_CLASSELEMENT);
        writeRegistry.add(io.frinx.openconfig.openconfig.network.instance.IIDs
                        .IN_IN_SU_SU_VL_AUG_SAOS8VLANLOGICALELEMENTSAUG_CL_CL_CONFIG,
                new SubPortVlanElementConfigWriter(cli));

        writeRegistry.addNoop(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PMINSTANCES);
        writeRegistry.addNoop(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PMINSTANCE);
        writeRegistry.add(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PM_CONFIG,
                new SubPortPmInstanceConfigWriter(cli));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceListReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceListConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_ET_CO_AUG_CONFIG1, new AggregateConfigReader(cli));

        // sub-port of the lag interface
        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubPortReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubPortConfigReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VLAN,
                new SubPortVlanReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubPortVlanConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PMINSTANCE, new SubPortPmInstanceReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_AUG_SAOS8PMINSTANCEAUG_PM_PM_CONFIG, new SubPortPmInstanceConfigReader(cli));
    }
}