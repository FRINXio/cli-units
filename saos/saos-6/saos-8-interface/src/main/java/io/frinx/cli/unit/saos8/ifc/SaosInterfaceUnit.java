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

package io.frinx.cli.unit.saos8.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos8.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.saos8.ifc.handler.lag.subifc.SubPortConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.lag.subifc.SubPortReader;
import io.frinx.cli.unit.saos8.ifc.handler.lag.subifc.SubPortVlanConfigReader;
import io.frinx.cli.unit.saos8.ifc.handler.lag.subifc.SubPortVlanReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.HashSet;
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
        return new HashSet<Device>() {
            {
                add(SaosDevices.SAOS_8);
            }
        };
    }

    @Override
    protected String getUnitName() {
        return "SAOS-8 Interface (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_INTERFACES,
                IIDs.FRINX_IF_AGGREGATE_EXTENSION,
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

    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SUBINTERFACE, new SubPortReader(cli));
        readRegistry.add(IIDs.IN_IN_SU_SU_CONFIG, new SubPortConfigReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VLAN,
                new SubPortVlanReader(cli));
        readRegistry.add(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                new SubPortVlanConfigReader(cli));

    }
}