/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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

    public void init()
    {
        reg = registry.registerTranslateUnit(HUAWEI, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.$YangModuleInfoImpl.getInstance(),
                $YangModuleInfoImpl.getInstance());
    }


    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private static final InstanceIdentifier<Subinterface1> SUBIFC_IPV4_AUG_ID = IIDs.IN_IN_SU_SUBINTERFACE.augmentation(Subinterface1.class);
    private static final InstanceIdentifier<Ipv4> SUBIFC_IPV4_ID = SUBIFC_IPV4_AUG_ID.child(Ipv4.class);
    private static final InstanceIdentifier<Addresses> SUBIFC_IPV4_ADDRESSES_ID = SUBIFC_IPV4_ID.child(Addresses.class);
    private static final InstanceIdentifier<Address> SUBIFC_IPV4_ADDRESS_ID = SUBIFC_IPV4_ADDRESSES_ID.child(Address.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config> SUBIFC_IPV4_CFG_ID =
            SUBIFC_IPV4_ADDRESS_ID.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config.class);

    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Subinterface1> SUBIFC_VLAN_AUG_ID =
            IIDs.IN_IN_SU_SUBINTERFACE.augmentation(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Subinterface1.class);
    private static final InstanceIdentifier<Vlan> SUBIFC_VLAN_ID = SUBIFC_VLAN_AUG_ID.child(Vlan.class);
    private static final InstanceIdentifier<Config> SUBIFC_VLAN_CFG_ID = SUBIFC_VLAN_ID.child(Config.class);

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        wRegistry.add(new GenericListWriter<>(IIDs.IN_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli)));

        wRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SUBINTERFACE, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SU_CONFIG, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(SUBIFC_VLAN_CFG_ID, new NoopCliWriter<>()));

        wRegistry.add(new GenericWriter<>(SUBIFC_IPV4_ADDRESS_ID, new NoopCliListWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(SUBIFC_IPV4_CFG_ID, new Ipv4ConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);
    }

    private void provideReaders(ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.IN_IN_STATE, new InterfaceStateReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES, SubinterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader()));
        rRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader()));
        rRegistry.add(new GenericOperReader<>(IIDs.IN_IN_SU_SU_STATE, new SubinterfaceStateReader()));

        rRegistry.addStructuralReader(SUBIFC_IPV4_AUG_ID, Subinterface1Builder.class);
        rRegistry.addStructuralReader(SUBIFC_IPV4_ID, Ipv4Builder.class);
        rRegistry.addStructuralReader(SUBIFC_IPV4_ADDRESSES_ID, AddressesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(SUBIFC_IPV4_ADDRESS_ID, new Ipv4AddressReader(cli)));
        rRegistry.add(new GenericConfigReader<>(SUBIFC_IPV4_CFG_ID, new Ipv4ConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "VRP Interface (Openconfig) translate unit";
    }
}
