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

package io.frinx.cli.unit.iosxr.lacp;

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
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.lacp.handler.BundleConfigReader;
import io.frinx.cli.unit.iosxr.lacp.handler.BundleConfigWriter;
import io.frinx.cli.unit.iosxr.lacp.handler.BundleReader;
import io.frinx.cli.unit.iosxr.lacp.handler.MemberConfigReader;
import io.frinx.cli.unit.iosxr.lacp.handler.MemberConfigWriter;
import io.frinx.cli.unit.iosxr.lacp.handler.MemberReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.lacp.IIDs;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.MembersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.top.LacpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.InterfacesBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRLacpUnit implements TranslateUnit {

    private final TranslationUnitCollector translationRegistry;
    private TranslationUnitCollector.Registration reg;

    public IosXRLacpUnit(@Nonnull final TranslationUnitCollector translationRegistry) {
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
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505
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
       // LACP root
        writeRegistry.add(new GenericWriter<>(IIDs.LACP, new NoopCliWriter<>()));

        // bundle interface
        writeRegistry.add(new GenericWriter<>(IIDs.LA_INTERFACES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.LA_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.LA_IN_IN_CONFIG, new BundleConfigWriter(cli)));

        // member's interface
        writeRegistry.add(new GenericWriter<>(IIDs.LA_IN_IN_MEMBERS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.LA_IN_IN_ME_MEMBER, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.LA_IN_IN_ME_ME_CONFIG, new MemberConfigWriter(cli)));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // LACP root
        readRegistry.addStructuralReader(IIDs.LACP, LacpBuilder.class);

        // bundle interface
        readRegistry.addStructuralReader(IIDs.LA_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.LA_IN_INTERFACE, new BundleReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.LA_IN_IN_CONFIG, new BundleConfigReader(cli)));

        // member's interface
        readRegistry.addStructuralReader(IIDs.LA_IN_IN_MEMBERS, MembersBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.LA_IN_IN_ME_MEMBER, new MemberReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.LA_IN_IN_ME_ME_CONFIG, new MemberConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR LACP (Openconfig) translation unit";
    }
}
