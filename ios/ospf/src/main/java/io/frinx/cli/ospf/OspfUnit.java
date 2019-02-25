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

package io.frinx.cli.ospf;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.IosDevices;
import io.frinx.cli.ospf.handler.AreaConfigReader;
import io.frinx.cli.ospf.handler.AreaInterfaceConfigReader;
import io.frinx.cli.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.ospf.handler.AreaStateReader;
import io.frinx.cli.ospf.handler.GlobalConfigReader;
import io.frinx.cli.ospf.handler.GlobalConfigWriter;
import io.frinx.cli.ospf.handler.GlobalStateReader;
import io.frinx.cli.ospf.handler.OspfAreaReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.AreasBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public OspfUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
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
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GL_TI_MA_CONFIG, new NoopCliWriter<>()),
                IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG,
                        new AreaInterfaceConfigWriter(cli)),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, IIDs.NE_NE_IN_IN_CONFIG, IIDs
                        .NE_NE_PR_PR_OS_GL_CONFIG));

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OSPFV2, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GLOBAL, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_CONFIG);

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG, new NoopCliWriter<>()));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OSPFV2, Ospfv2Builder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_GLOBAL, GlobalBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_OS_GL_STATE, new GlobalStateReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AREAS, AreasBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader()));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_STATE, new AreaStateReader()));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE,
                new AreaInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG,
                new AreaInterfaceConfigReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.$YangModuleInfoImpl
                        .getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS OSPF unit";
    }
}
