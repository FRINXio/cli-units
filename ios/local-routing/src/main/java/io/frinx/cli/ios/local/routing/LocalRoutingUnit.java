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

package io.frinx.cli.ios.local.routing;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.IosDevices;
import io.frinx.cli.ios.local.routing.handlers.NextHopConfigReader;
import io.frinx.cli.ios.local.routing.handlers.NextHopReader;
import io.frinx.cli.ios.local.routing.handlers.NextHopStateReader;
import io.frinx.cli.ios.local.routing.handlers.StaticConfigReader;
import io.frinx.cli.ios.local.routing.handlers.StaticReader;
import io.frinx.cli.ios.local.routing.handlers.StaticStateReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top.StaticRoutesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LocalRoutingUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public LocalRoutingUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_LOCALAGGREGATES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new NoopCliWriter<>()));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_STATICROUTES, StaticRoutesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_ST_STATIC, new StaticReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_ST_ST_STATE, new StaticStateReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_ST_ST_CONFIG, new StaticConfigReader()));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_ST_ST_NEXTHOPS, NextHopsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP, new NextHopReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CONFIG, new NextHopConfigReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_STATE, new NextHopStateReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS Local Routing (Openconfig) translate unit";
    }
}
