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

package io.frinx.cli.unit.ios.rib;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.rib.handler.AfiSafiReader;
import io.frinx.cli.unit.ios.rib.handler.Ipv4RoutesReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.rib.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RibUnit extends AbstractUnit {

    public RibUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS BGP RIB (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // FIXME: add ipv6 support?
        readRegistry.subtreeAdd(IIDs.BG_AF_AFISAFI, new AfiSafiReader(), Sets.newHashSet(IIDs.BG_AF_AF_STATE));
        readRegistry.subtreeAdd(IIDs.BG_AF_AF_IP_LO_RO_ROUTE, new Ipv4RoutesReader(cli),
                Sets.newHashSet(IIDs.BG_AF_AF_IP_LO_RO_RO_STATE));
    }
}