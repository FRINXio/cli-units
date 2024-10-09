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

package io.frinx.cli.unit.iosxr.snmp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.snmp.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.snmp.handler.SnmpInterfacesReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.snmp.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEvent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SnmpUnit extends AbstractUnit {

    public SnmpUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR SNMP unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                io.frinx.openconfig.openconfig.types.IIDs.FRINX_EVENT_TYPES);
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry, @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.SN_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.SN_IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(InstanceIdentifier.create(Config.class).child(EnabledTrapForEvent.class)));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.subtreeAdd(IIDs.SN_INTERFACES, new SnmpInterfacesReader(cli),
                Sets.newHashSet(IIDs.SN_IN_INTERFACE,
                        IIDs.SN_IN_IN_CONFIG,
                        IIDs.SN_IN_IN_CO_ENABLEDTRAPFOREVENT));
    }
}