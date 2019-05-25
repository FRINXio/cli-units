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
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
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
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_GL_AFISAFIS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI);
        writeRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NEIGHBORS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new NeighborTransportConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_EBGPMULTIHOP);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_EB_CONFIG, new NeighborEbgpConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG, new NeighborAfiSafiConfigWriter(cli),
                Sets.newHashSet(
                    RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG, CONFIG_IID),
                    RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG_SOFTRECONFIGURATION,
                        CONFIG_IID)),
                IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new
                NeighborAfiSafiApplyPolicyConfigWriter(cli), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IPV4UNICAST);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG, new NeighborAfiSafiIpvConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.addNoop(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PREFIXLIMIT);
        writeRegistry.addAfter(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PRE_CONFIG, new
                NeighborAfiSafiPrefixLimitConfigWriter(cli), IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IPV6UNICAST);
        writeRegistry.addAfter(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG, new
                NeighborAfiSafiIpvConfigWriter(cli), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.addNoop(IIDs.NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFIXLIMIT);
        writeRegistry.addAfter(IIDs.NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFI_CONFIG, new
                NeighborAfiSafiPrefixLimitConfigWriter(cli), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new GlobalAfiSafiReader(cli),
                Sets.newHashSet(RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, GL_AFISAFI_IID)));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new NeighborTransportConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_EB_CONFIG,new NeighborEbgpConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli),
            Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG, NE_AFISAFI_IID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG, NE_AFISAFI_IID),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG_SOFTRECONFIGURATION,
                    NE_AFISAFI_IID)
            )
        );
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new NeighborAfiSafiApplyPolicyConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG, new NeighborAfiSafiIpv4ConfigReader(cli));
        readRegistry.add(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PRE_CONFIG,
                new NeighborAfiSafiPrefixLimitConfigReader(cli));
        readRegistry.add(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG, new NeighborAfiSafiIpv6ConfigReader(cli));
        readRegistry.add(IIDs.NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFI_CONFIG,
                new NeighborAfiSafiPrefixLimitConfigReader(cli));
    }

    @Override
    public String toString() {
        return "IOS XR BGP (Openconfig) translate unit";
    }
}
