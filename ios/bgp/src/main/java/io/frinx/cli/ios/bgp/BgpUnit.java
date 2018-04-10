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

package io.frinx.cli.ios.bgp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.GlobalAfiSafiConfigReader;
import io.frinx.cli.ios.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.ios.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.ios.bgp.handler.GlobalConfigReader;
import io.frinx.cli.ios.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.ios.bgp.handler.GlobalStateReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborAfiSafiPolicyConfigReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborAfiSafiReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborPolicyConfigReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborRouteReflectorConfigReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborStateReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborTransportConfigReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter;
import io.frinx.cli.ios.bgp.handler.neighbor.PrefixesReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupAfiSafiPolicyConfigReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupAfiSafiReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupConfigReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupPolicyConfigReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupRouteReflectorConfigReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupTransportConfigReader;
import io.frinx.cli.ios.bgp.handler.peergroup.PeerGroupWriter;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.route.reflector.RouteReflectorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.PeerGroupsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class BgpUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public BgpUnit(@Nonnull final TranslationUnitCollector registry) {
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
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigWriter(cli)),
                IIDs.NE_NE_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new NoopCliWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG);

        // Peer group writer, handle also subtrees
        wRegistry.subtreeAddAfter(
                Sets.newHashSet(
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_TRANSPORT, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_TR_CONFIG, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_ROUTEREFLECTOR, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_RO_CONFIG, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_APPLYPOLICY, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_AP_CONFIG, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_AFISAFIS, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AFISAFI, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_CONFIG, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_APPLYPOLICY, InstanceIdentifier.create(PeerGroup.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_AP_CONFIG, InstanceIdentifier.create(PeerGroup.class))),
                new GenericListWriter<>(IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP, new PeerGroupWriter(cli)),
                Sets.newHashSet(IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG));

        // Neighbor writer, handle also subtrees
        wRegistry.subtreeAddAfter(
                Sets.newHashSet(
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_ROUTEREFLECTOR, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_RO_CONFIG, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_APPLYPOLICY, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY, InstanceIdentifier.create(Neighbor.class)),
                        RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, InstanceIdentifier.create(Neighbor.class))),
                new GenericListWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborWriter(cli)),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BGP, BgpBuilder.class);

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_GLOBAL, GlobalBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_BG_GL_STATE, new GlobalStateReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_GL_AFISAFIS, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new GlobalAfiSafiReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigReader(cli)));

        provideNeighborReaders(rRegistry, cli);
        providePeerGroupReaders(rRegistry, cli);
    }

    private void provideNeighborReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NEIGHBORS, NeighborsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli)));

        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_STATE, new NeighborStateReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS, AfiSafisBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(AfiSafi.class).child(Config.class)),
                new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY, ApplyPolicyBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new NeighborAfiSafiPolicyConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT, TransportBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new NeighborTransportConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_APPLYPOLICY, ApplyPolicyBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG, new NeighborPolicyConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_STATE, StateBuilder.class);
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_ST_PREFIXES, new PrefixesReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_ROUTEREFLECTOR, RouteReflectorBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_RO_CONFIG, new NeighborRouteReflectorConfigReader(cli)));
    }

    private void providePeerGroupReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_PEERGROUPS, PeerGroupsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP, new PeerGroupReader(cli)));

        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG, new PeerGroupConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_PE_PE_AFISAFIS, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.AfiSafisBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.afi.safi.Config.class)),
                new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AFISAFI, new PeerGroupAfiSafiReader(cli)));


        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_APPLYPOLICY, ApplyPolicyBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_AP_CONFIG, new PeerGroupAfiSafiPolicyConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_PE_PE_TRANSPORT, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.TransportBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_PE_PE_TR_CONFIG, new PeerGroupTransportConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_PE_PE_APPLYPOLICY, ApplyPolicyBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_PE_PE_AP_CONFIG, new PeerGroupPolicyConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_PE_PE_ROUTEREFLECTOR, RouteReflectorBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_PE_PE_RO_CONFIG, new PeerGroupRouteReflectorConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS BGP (Openconfig) translate unit";
    }
}
