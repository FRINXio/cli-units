/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.rib;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.rib.handler.AfiSafiReader;
import io.frinx.cli.ios.rib.handler.Ipv4RoutesReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import java.util.Set;
import javax.annotation.Nonnull;

import io.frinx.openconfig.openconfig.rib.IIDs;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRibBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafi;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.Ipv4UnicastBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.LocRibBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.RoutesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.$YangModuleInfoImpl;

public class RibUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

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
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        // no writers
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // FIXME: add ipv6 support?
        rRegistry.addStructuralReader(IIDs.BGPRIB, BgpRibBuilder.class);
        rRegistry.addStructuralReader(IIDs.BG_AFISAFIS, AfiSafisBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(AfiSafi.class).child(State.class)),
                new GenericListReader<>(IIDs.BG_AF_AFISAFI, new AfiSafiReader()));
        rRegistry.addStructuralReader(IIDs.BG_AF_AF_IPV4UNICAST, Ipv4UnicastBuilder.class);
        rRegistry.addStructuralReader(IIDs.BG_AF_AF_IP_LOCRIB, LocRibBuilder.class);
        rRegistry.addStructuralReader(IIDs.BG_AF_AF_IP_LO_ROUTES, RoutesBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(Route.class)
                        .child(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.route.State.class)),
                new GenericListReader<>(IIDs.BG_AF_AF_IP_LO_RO_ROUTE, new Ipv4RoutesReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS BGP RIB (Openconfig) translate unit";
    }
}
