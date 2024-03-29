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

package io.frinx.cli.unit.ios.cdp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.cdp.handler.InterfaceConfigReader;
import io.frinx.cli.unit.ios.cdp.handler.InterfaceReader;
import io.frinx.cli.unit.ios.cdp.handler.NeighborReader;
import io.frinx.cli.unit.ios.cdp.handler.NeighborStateReader;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.cdp.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cdp.rev171024.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosCdpUnit extends AbstractUnit {

    public IosCdpUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS CDP (FRINX) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readeRegistry, Cli cli) {
        // TODO keeping InterfaceReader and InterfaceConfigReader just as Operational readers
        // because we do not yet support writes
        // and also because finding out whether an interface is cdp enabled or not is not possible
        // just from running-config (IOS has default off, XE has default on) the only way to get the
        // info from running config is to use "show run all". But that would not do well with CliReader right now and
        // would
        // slow down reads by a lot
        readeRegistry.add(IIDs.CD_IN_INTERFACE, new InterfaceReader(cli));
        readeRegistry.add(IIDs.CD_IN_IN_CONFIG, new InterfaceConfigReader());
        readeRegistry.add(IIDs.CD_IN_IN_NE_NEIGHBOR, new NeighborReader(cli));
        readeRegistry.add(IIDs.CD_IN_IN_NE_NE_STATE, new NeighborStateReader(cli));
    }
}