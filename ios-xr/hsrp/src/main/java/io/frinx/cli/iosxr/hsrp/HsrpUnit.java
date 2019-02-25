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

package io.frinx.cli.iosxr.hsrp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupConfigReader;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupConfigWriter;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupReader;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceConfigReader;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceConfigWriter;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.hsrp.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.top.HsrpBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class HsrpUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public HsrpUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.$YangModuleInfoImpl
                        .getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
            @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry, @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.HSRP, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.HS_INTERFACES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.HS_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.HS_IN_IN_CONFIG, new HsrpInterfaceConfigWriter(cli)));

        writeRegistry.add(new GenericListWriter<>(IIDs.HS_IN_IN_HSRPGROUP, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.HS_IN_IN_HS_CONFIG, new HsrpGroupConfigWriter(cli)));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.HSRP, HsrpBuilder.class);
        readRegistry.addStructuralReader(IIDs.HS_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.HS_IN_INTERFACE, new HsrpInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.HS_IN_IN_CONFIG, new HsrpInterfaceConfigReader(cli)));

        readRegistry.add(new GenericConfigListReader<>(IIDs.HS_IN_IN_HSRPGROUP, new HsrpGroupReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.HS_IN_IN_HS_CONFIG, new HsrpGroupConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR Hsrp unit";
    }
}
