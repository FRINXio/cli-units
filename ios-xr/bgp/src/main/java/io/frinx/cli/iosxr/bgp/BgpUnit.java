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

package io.frinx.cli.iosxr.bgp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiApplyPolicyConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiApplyPolicyConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiIpv4ConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiIpv6ConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiIpvConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiPrefixLimitConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiPrefixLimitConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborEbgpConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborEbgpConfigWriter;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborTransportConfigReader;
import io.frinx.cli.iosxr.bgp.handler.neighbor.NeighborTransportConfigWriter;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.all.afi.safi.common.PrefixLimitBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.ipv4.unicast.group.Ipv4UnicastBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.ipv6.unicast.group.Ipv6UnicastBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.ebgp.multihop.EbgpMultihopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class BgpUnit implements TranslateUnit {

    private static final InstanceIdentifier<Config> CONFIG_IID = InstanceIdentifier.create(Config.class);
    private static final InstanceIdentifier<AfiSafi> NE_AFISAFI_IID = InstanceIdentifier.create(AfiSafi.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx
            .openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi> GL_AFISAFI_IID =
            InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx
                    .openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi.class);

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public BgpUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg
                != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(), org.opendaylight.yang.gen.v1.http.frinx.openconfig
                .net.yang.bgp.extension.rev180323.$YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry, @Nonnull final
        ModifiableWriterRegistryBuilder writeRegistry, @Nonnull final TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AFISAFIS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NEIGHBORS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new
                NeighborTransportConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_EBGPMULTIHOP, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_EB_CONFIG,
                new NeighborEbgpConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NoopCliListWriter<>()));
        writeRegistry.subtreeAddAfter(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG, CONFIG_IID),
            RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG_SOFTRECONFIGURATION, CONFIG_IID)),
                new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG, new
                NeighborAfiSafiConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new
                NeighborAfiSafiApplyPolicyConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IPV4UNICAST, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG, new
                NeighborAfiSafiIpvConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PREFIXLIMIT, new
                NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PRE_CONFIG, new
                NeighborAfiSafiPrefixLimitConfigWriter(cli)), IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IPV6UNICAST, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG, new
                NeighborAfiSafiIpvConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs
                .NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFIXLIMIT, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs
                .NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFI_CONFIG, new
                NeighborAfiSafiPrefixLimitConfigWriter(cli)), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BGP, BgpBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_GLOBAL, GlobalBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_GL_AFISAFIS, org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, GL_AFISAFI_IID)),
                new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new GlobalAfiSafiReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NEIGHBORS, NeighborsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT, TransportBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new
                NeighborTransportConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_EBGPMULTIHOP, EbgpMultihopBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_EB_CONFIG,
                new NeighborEbgpConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS, AfiSafisBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG, NE_AFISAFI_IID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG, NE_AFISAFI_IID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG_SOFTRECONFIGURATION,
                        NE_AFISAFI_IID)), new GenericConfigListReader<>(IIDs
                .NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY, ApplyPolicyBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new
                NeighborAfiSafiApplyPolicyConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IPV4UNICAST, Ipv4UnicastBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG, new
                NeighborAfiSafiIpv4ConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PREFIXLIMIT, PrefixLimitBuilder
                .class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PRE_CONFIG, new
                NeighborAfiSafiPrefixLimitConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IPV6UNICAST, Ipv6UnicastBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG, new
                NeighborAfiSafiIpv6ConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFIXLIMIT,
                PrefixLimitBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs
                .NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFI_CONFIG, new
                NeighborAfiSafiPrefixLimitConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR BGP (Openconfig) translate unit";
    }
}
