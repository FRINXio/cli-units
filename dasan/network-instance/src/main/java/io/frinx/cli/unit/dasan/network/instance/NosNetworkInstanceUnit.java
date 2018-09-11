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

package io.frinx.cli.unit.dasan.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.dasan.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.dasan.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.dasan.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.dasan.network.instance.handler.vlan.VlanConfigReader;
import io.frinx.cli.unit.dasan.network.instance.handler.vlan.VlanReader;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.VlansBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class NosNetworkInstanceUnit implements TranslateUnit {

    private static final Device DASAN = new DeviceIdBuilder()
            .setDeviceType("nos")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public NosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(DASAN, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    // vlan config augmentation
    private static final InstanceIdentifier<
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config>
        VLAN_CONFIG_ROOT_ID =
            InstanceIdentifier
                .create(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan
                    .Config.class);

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        // No handling required on the network instance level
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NETWORKINSTANCE, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli)),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli)));

        // VLAN(L3)
        readRegistry.addStructuralReader(IIDs.NE_NE_VLANS, VlansBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_VL_VLAN, new VlanReader(cli)));
        readRegistry.subtreeAddAfter(Sets.newHashSet(
                VLAN_CONFIG_ROOT_ID.augmentation(
                        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1.class)
                ),
                new GenericConfigReader<>(IIDs.NE_NE_VL_VL_CONFIG, new VlanConfigReader(cli)), IIDs.NE_NE_VL_VLAN);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.$YangModuleInfoImpl
                .getInstance()
                );
    }

    @Override
    public String toString() {
        return "Dasan Network Instance (Openconfig) translate unit";
    }
}
