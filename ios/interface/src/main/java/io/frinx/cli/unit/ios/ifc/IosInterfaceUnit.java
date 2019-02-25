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

package io.frinx.cli.unit.ios.ifc;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.IosDevices;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceStateReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceConfigReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceConfigWriter;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceStateReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceVlanConfigReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip4.Ipv4ConfigWriter;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6ConfigWriter;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.VlanBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosInterfaceUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosInterfaceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosDevices.IOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet
                        .rev161222.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.$YangModuleInfoImpl
                        .getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericListWriter<>(IIDs.IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_CONFIG, new InterfaceConfigWriter(cli)));

        writeRegistry.add(new GenericWriter<>(IIDs.IN_IN_SU_SUBINTERFACE, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigWriter(cli)),
                IIDs.IN_IN_CONFIG);
        writeRegistry.addAfter(
                new GenericWriter<>(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                        new SubinterfaceVlanConfigWriter(cli)), IIDs.IN_IN_SU_SU_CONFIG);

        writeRegistry.add(
                new GenericWriter<>(
                        io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new NoopCliListWriter<>()));
        writeRegistry.addAfter(
                new GenericWriter<>(
                        io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                        new Ipv4ConfigWriter(cli)), Sets.newHashSet(IIDs.IN_IN_CONFIG,
                        io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE));

        writeRegistry.add(
                new GenericWriter<>(
                        io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                        new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(
                        io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                        new Ipv6ConfigWriter(cli)),
                Sets.newHashSet(IIDs.IN_IN_CONFIG,
                        io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_IN_INTERFACE));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.IN_IN_STATE, new InterfaceStateReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_CONFIG, new InterfaceConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.IN_IN_SUBINTERFACES, SubinterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.IN_IN_SU_SUBINTERFACE, new SubinterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.IN_IN_SU_SU_CONFIG, new SubinterfaceConfigReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.IN_IN_SU_SU_STATE, new SubinterfaceStateReader(cli)));

        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1,
                Subinterface1Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IPV4,
                Ipv4Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_ADDRESSES,
                AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
                new Ipv4AddressReader(cli)));
        readRegistry.add(
                new GenericConfigReader<>(
                        io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_AD_CONFIG,
                        new Ipv4ConfigReader(cli)));

        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2,
                Subinterface2Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IPV6,
                Ipv6Builder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_ADDRESSES,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top
                        .ipv6.AddressesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_ADDRESS,
                new Ipv6AddressReader(cli)));
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig._if.ip.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE2_IP_AD_AD_CONFIG,
                new Ipv6ConfigReader(cli)));

        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Subinterface1Builder.class);
        readRegistry.addStructuralReader(io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VLAN,
                VlanBuilder.class);
        readRegistry.add(
                new GenericConfigReader<>(
                        io.frinx.openconfig.openconfig.vlan.IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_VL_CONFIG,
                        new SubinterfaceVlanConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS Interface (Openconfig) translate unit";
    }

}
