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

package io.frinx.cli.unit.iosxr.lr;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.lr.handler.statics.StaticConfigReader;
import io.frinx.cli.unit.iosxr.lr.handler.statics.StaticConfigWriter;
import io.frinx.cli.unit.iosxr.lr.handler.statics.StaticListReader;
import io.frinx.cli.unit.iosxr.lr.handler.statics.nexthop.NextHopListReader;
import io.frinx.cli.unit.iosxr.lr.handler.statics.nexthop.NextHopListWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LrUnit extends AbstractUnit {

    public LrUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR LOCAL-ROUTING unit";
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
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_STATICROUTES);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_ST_STATIC);
        writeRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_ST_ST_CONFIG, new StaticConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_ST_ST_CO_AUG_AFISAFIAUG));
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_ST_ST_NEXTHOPS);
        writeRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP,
                new NextHopListWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CONFIG,
                        IIDs.NE_NE_PR_PR_ST_ST_NE_NE_INTERFACEREF,
                        IIDs.NE_NE_PR_PR_ST_ST_NE_NE_IN_CONFIG,
                        IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CO_AUG_SETTAGAUG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_STATIC, new StaticListReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_ST_ST_CONFIG, new StaticConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP,
                new NextHopListReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CONFIG, IIDs.NE_NE_PR_PR_ST_ST_NE_NE_INTERFACEREF));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                $YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610
                .$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610
                .$YangModuleInfoImpl.getInstance());
    }
}
