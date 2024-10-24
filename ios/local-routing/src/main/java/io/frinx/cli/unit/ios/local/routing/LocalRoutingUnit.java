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

package io.frinx.cli.unit.ios.local.routing;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.local.routing.handlers.NextHopConfigReader;
import io.frinx.cli.unit.ios.local.routing.handlers.NextHopReader;
import io.frinx.cli.unit.ios.local.routing.handlers.NextHopStateReader;
import io.frinx.cli.unit.ios.local.routing.handlers.StaticConfigReader;
import io.frinx.cli.unit.ios.local.routing.handlers.StaticReader;
import io.frinx.cli.unit.ios.local.routing.handlers.StaticStateReader;
import io.frinx.cli.unit.ios.local.routing.handlers.StaticWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LocalRoutingUnit extends AbstractUnit {

    public LocalRoutingUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS Local Routing (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(io.frinx.openconfig.openconfig.local.routing.IIDs.FRINX_OPENCONFIG_LOCAL_ROUTING);
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_LOCALAGGREGATES);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_LO_AGGREGATE);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_STATICROUTES);
        writeRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_ST_STATIC, new StaticWriter(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_PR_PR_ST_ST_CONFIG,
                        IIDs.NE_NE_PR_PR_ST_ST_NEXTHOPS,
                        IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP,
                        IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CONFIG
                )
        );
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_STATIC, new StaticReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_ST_STATE, new StaticStateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_ST_CONFIG, new StaticConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP, new NextHopReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CONFIG, new NextHopConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_STATE, new NextHopStateReader(cli));
    }
}