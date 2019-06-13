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

package io.frinx.cli.unit.iosxr.bfd;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.bfd.handler.ConfigReader;
import io.frinx.cli.unit.iosxr.bfd.handler.ConfigWriter;
import io.frinx.cli.unit.iosxr.bfd.handler.InterfaceReader;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.bfd.IIDs;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.BfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRBfdUnit implements TranslateUnit {

    private static final InstanceIdentifier<Config> CONFIG_INSTANCE_IDENTIFIER_ROOT
            = InstanceIdentifier.create(Config.class);

    private final TranslationUnitCollector translationRegistry;
    private TranslationUnitCollector.Registration reg;

    public IosXRBfdUnit(@Nonnull final TranslationUnitCollector translationRegistry) {
        this.translationRegistry = translationRegistry;
    }

    public void init() {
        reg = translationRegistry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117
                        .$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211
                        .$YangModuleInfoImpl.getInstance());

    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return new HashSet<>();
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.BFD, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.BF_INTERFACES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.BF_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.subtreeAdd(Sets.newHashSet(
                CONFIG_INSTANCE_IDENTIFIER_ROOT.augmentation(IfBfdExtAug.class)
        ), new GenericWriter<>(IIDs.BF_IN_IN_CONFIG, new ConfigWriter(cli)));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.BFD, BfdBuilder.class);
        readRegistry.addStructuralReader(IIDs.BF_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.BF_IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.subtreeAdd(Sets.newHashSet(
                CONFIG_INSTANCE_IDENTIFIER_ROOT.augmentation(IfBfdExtAug.class)
        ), new GenericConfigReader<>(IIDs.BF_IN_IN_CONFIG, new ConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR BFD (Openconfig) translation unit";
    }
}
