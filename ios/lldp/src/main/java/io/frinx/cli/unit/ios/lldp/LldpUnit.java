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

package io.frinx.cli.unit.ios.lldp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericOperListReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.IosDevices;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.lldp.handler.InterfaceConfigReader;
import io.frinx.cli.unit.ios.lldp.handler.InterfaceReader;
import io.frinx.cli.unit.ios.lldp.handler.LldpConfigReader;
import io.frinx.cli.unit.ios.lldp.handler.NeighborReader;
import io.frinx.cli.unit.ios.lldp.handler.NeighborStateReader;
import io.frinx.openconfig.openconfig.lldp.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.LldpBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LldpUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public LldpUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder writeRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
    }

    private void provideReaders(ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        // TODO CDP and LLDP are almost identical, reuse code, DRY
        readRegistry.addStructuralReader(IIDs.LLDP, LldpBuilder.class);
        readRegistry.add(new GenericOperReader<>(IIDs.LL_CONFIG, new LldpConfigReader(cli, getShowHostnameCommand())));
        readRegistry.addStructuralReader(IIDs.LL_INTERFACES, InterfacesBuilder.class);
        // TODO see IosCdpUnit why interface and config readers are registered as operational
        readRegistry.add(new GenericOperListReader<>(IIDs.LL_IN_INTERFACE, new InterfaceReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.LL_IN_IN_CONFIG, new InterfaceConfigReader()));
        readRegistry.addStructuralReader(IIDs.LL_IN_IN_NEIGHBORS, NeighborsBuilder.class);
        readRegistry.add(new GenericOperListReader<>(IIDs.LL_IN_IN_NE_NEIGHBOR, new NeighborReader(cli)));
        readRegistry.add(new GenericOperReader<>(IIDs.LL_IN_IN_NE_NE_STATE, new NeighborStateReader(cli)));
    }

    protected String getShowHostnameCommand() {
        return LldpConfigReader.SHOW_HOSTNAME;
    }

    @Override
    public String toString() {
        return "IOS LLDP (Openconfig) translate unit";
    }
}
