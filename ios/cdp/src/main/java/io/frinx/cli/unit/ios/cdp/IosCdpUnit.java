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

import static io.frinx.cli.ios.IosDevices.IOS_ALL;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericOperListReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.cdp.handler.InterfaceConfigReader;
import io.frinx.cli.unit.ios.cdp.handler.InterfaceReader;
import io.frinx.cli.unit.ios.cdp.handler.NeighborReader;
import io.frinx.cli.unit.ios.cdp.handler.NeighborStateReader;
import io.frinx.openconfig.openconfig.cdp.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cdp.rev171024.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cdp.rev171024.cdp.top.CdpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.NeighborsBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosCdpUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosCdpUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
    }

    private void provideReaders(ModifiableReaderRegistryBuilder readeRegistry, Cli cli) {
        readeRegistry.addStructuralReader(IIDs.CDP, CdpBuilder.class);
        readeRegistry.addStructuralReader(IIDs.CD_INTERFACES, InterfacesBuilder.class);
        // TODO keeping InterfaceReader and InterfaceConfigReader just as Operational readers
        // because we do not yet support writes
        // and also because finding out whether an interface is cdp enabled or not is not possible
        // just from running-config (IOS has default off, XE has default on) the only way to get the
        // info from running config is to use "show run all". But that would not do well with CliReader right now and
        // would
        // slow down reads by a lot
        readeRegistry.add(new GenericOperListReader<>(IIDs.CD_IN_INTERFACE, new InterfaceReader(cli)));
        readeRegistry.add(new GenericOperReader<>(IIDs.CD_IN_IN_CONFIG, new InterfaceConfigReader()));
        readeRegistry.addStructuralReader(IIDs.CD_IN_IN_NEIGHBORS, NeighborsBuilder.class);
        readeRegistry.add(new GenericOperListReader<>(IIDs.CD_IN_IN_NE_NEIGHBOR, new NeighborReader(cli)));
        readeRegistry.add(new GenericOperReader<>(IIDs.CD_IN_IN_NE_NE_STATE, new NeighborStateReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS CDP (FRINX) translate unit";
    }

}
