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

package io.frinx.cli.ios.rib;

import static io.frinx.cli.ios.IosDevices.IOS_ALL;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericOperListReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.rib.handler.AfiSafiReader;
import io.frinx.cli.ios.rib.handler.Ipv4RoutesReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.openconfig.openconfig.rib.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRibBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.Ipv4UnicastBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.LocRibBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.RoutesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.Route;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class RibUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public RibUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        // no writers
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        // FIXME: add ipv6 support?
        readRegistry.addStructuralReader(IIDs.BGPRIB, BgpRibBuilder.class);
        readRegistry.addStructuralReader(IIDs.BG_AFISAFIS, AfiSafisBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(AfiSafi.class)
                        .child(State.class)),
                new GenericOperListReader<>(IIDs.BG_AF_AFISAFI, new AfiSafiReader()));
        readRegistry.addStructuralReader(IIDs.BG_AF_AF_IPV4UNICAST, Ipv4UnicastBuilder.class);
        readRegistry.addStructuralReader(IIDs.BG_AF_AF_IP_LOCRIB, LocRibBuilder.class);
        readRegistry.addStructuralReader(IIDs.BG_AF_AF_IP_LO_ROUTES, RoutesBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(Route.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc
                                .rib.top.loc.rib.routes.route.State.class)),
                new GenericOperListReader<>(IIDs.BG_AF_AF_IP_LO_RO_ROUTE, new Ipv4RoutesReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS BGP RIB (Openconfig) translate unit";
    }
}
