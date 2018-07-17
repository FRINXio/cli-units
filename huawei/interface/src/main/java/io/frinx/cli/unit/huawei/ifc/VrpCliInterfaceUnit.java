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

package io.frinx.cli.unit.huawei.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceStateReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpCliInterfaceUnit implements TranslateUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public VrpCliInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(HUAWEI, this);
    }

    public void close() {
        if (reg
                != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.$YangModuleInfoImpl
                        .getInstance(),
                $YangModuleInfoImpl.getInstance());
    }


    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private static final InstanceIdentifier<Subinterface1> SUBIFC_IPV4_AUG_ID = IIDs.IN_IN_SU_SUBINTERFACE
            .augmentation(Subinterface1.class);
    private static final InstanceIdentifier<Ipv4> SUBIFC_IPV4_ID = SUBIFC_IPV4_AUG_ID.child(Ipv4.class);
    private static final InstanceIdentifier<Addresses> SUBIFC_IPV4_ADDRESSES_ID = SUBIFC_IPV4_ID.child(Addresses.class);
    private static final InstanceIdentifier<Address> SUBIFC_IPV4_ADDRESS_ID = SUBIFC_IPV4_ADDRESSES_ID.child(Address
            .class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
            .rev161222.ipv4.top.ipv4.addresses.address.Config> SUBIFC_IPV4_CFG_ID =
            SUBIFC_IPV4_ADDRESS_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                    .rev161222.ipv4.top.ipv4.addresses.address.Config.class);

    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
            .rev170714.Subinterface1> SUBIFC_VLAN_AUG_ID =
            IIDs.IN_IN_SU_SUBINTERFACE.augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
                    .rev170714.Subinterface1.class);
    private static final InstanceIdentifier<Vlan> SUBIFC_VLAN_ID = SUBIFC_VLAN_AUG_ID.child(Vlan.class);
    private static final InstanceIdentifier<Config> SUBIFC_VLAN_CFG_ID = SUBIFC_VLAN_ID.child(Config.class);

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericListWriter<>(IIDs.IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli)));

        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SUBINTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SU_CONFIG, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(SUBIFC_VLAN_CFG_ID, new NoopCliWriter<>()));

        writeRegistry.add(new GenericWriter<>(SUBIFC_IPV4_ADDRESS_ID, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(SUBIFC_IPV4_CFG_ID, new Ipv4ConfigWriter(cli)),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE);
    }

    private void provideReaders(ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.IN_IN_STATE, new InterfaceStateReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES, SubinterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader()));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader()));
        readRegistry.add(new GenericOperReader<>(IIDs.IN_IN_SU_SU_STATE, new SubinterfaceStateReader()));

        readRegistry.addStructuralReader(SUBIFC_IPV4_AUG_ID, Subinterface1Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV4_ID, Ipv4Builder.class);
        readRegistry.addStructuralReader(SUBIFC_IPV4_ADDRESSES_ID, AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(SUBIFC_IPV4_ADDRESS_ID, new Ipv4AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(SUBIFC_IPV4_CFG_ID, new Ipv4ConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "VRP Interface (Openconfig) translate unit";
    }
}
