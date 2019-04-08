/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.ospf;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.junos.ospf.handler.AreaConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceBfdConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceBfdConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceTimersConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceTimersConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.OspfAreaReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.OspfAreaIfBfdConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.BfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRefBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.TimersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.AreasBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public OspfUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(JunosDevices.JUNOS_ALL, this);
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        CheckRegistry checkRegistry = ChecksMap.getOpenconfigCheckRegistry();
        readRegistry.setCheckRegistry(checkRegistry);
        provideReaders(readRegistry, cli);
        writeRegistry.setCheckRegistry(checkRegistry);
        provideWriters(writeRegistry, cli);
    }

    private static final InstanceIdentifier<Config> IN_IN_CONFIG_SUBTREE_ROOT =
            InstanceIdentifier.create(Config.class);

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.subtreeAddAfter(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CO_AUG_OSPFAREAIFCONFAUG,
                        IN_IN_CONFIG_SUBTREE_ROOT)),
                new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG);

        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BFD,
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BF_CONFIG,
                new AreaInterfaceBfdConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs
                .NE_NE_PR_PR_OS_AR_AR_IN_IN_TI_CONFIG, new AreaInterfaceTimersConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG, new NoopCliWriter<>()));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OSPFV2, Ospfv2Builder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AREAS, AreasBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader()));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE,
                new AreaInterfaceReader(cli)));
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CO_AUG_OSPFAREAIFCONFAUG,
                        IN_IN_CONFIG_SUBTREE_ROOT)),
                new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG,
                new AreaInterfaceConfigReader(cli)));
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG,
                OspfAreaIfBfdConfAugBuilder.class);
        readRegistry.addStructuralReader(
                io.frinx.openconfig.openconfig.bfd
                        .IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BFD,
                BfdBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                io.frinx.openconfig.openconfig.bfd
                        .IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BF_CONFIG,
                new AreaInterfaceBfdConfigReader(cli)));
        readRegistry.addStructuralReader(
               IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_TIMERS,
               TimersBuilder.class);
        readRegistry.add(new GenericConfigReader<>(
                        IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_TI_CONFIG,
                new AreaInterfaceTimersConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF, InterfaceRefBuilder.class);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.$YangModuleInfoImpl
                        .getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.$YangModuleInfoImpl
                        .getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "Junos OSPF unit";
    }
}
