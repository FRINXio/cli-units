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

package io.frinx.cli.unit.iosxr.bgp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.local.aggregates.BgpLocalAggregateConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.local.aggregates.BgpLocalAggregateReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiApplyPolicyConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiApplyPolicyConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiIpv4ConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiIpv6ConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiIpvConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiPrefixLimitConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiPrefixLimitConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborEbgpConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborEbgpConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborTransportConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborTransportConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupAfiSafiApplyPolicyConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupAfiSafiApplyPolicyConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupAfiSafiConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupAfiSafiListReader;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupConfigReader;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupConfigWriter;
import io.frinx.cli.unit.iosxr.bgp.handler.peergroup.PeerGroupListReader;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class BgpUnit extends AbstractUnit {

    public BgpUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR BGP (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                IIDs.FRINX_BGP_EXTENSION);
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
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_PEERGROUPS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG, new PeerGroupConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_PE_PE_AFISAFIS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AFISAFI);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_CONFIG,new PeerGroupAfiSafiConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG,
                IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG);
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
                    IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG,
                    IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG_SOFTRECONFIGURATION),
                IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new
                NeighborAfiSafiApplyPolicyConfigWriter(cli), IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_APPLYPOLICY);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_AP_CONFIG, new
                PeerGroupAfiSafiApplyPolicyConfigWriter(cli),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG,
                IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG,
                IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_CONFIG);
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
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_LOCALAGGREGATES);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_LO_AGGREGATE);

        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new BgpLocalAggregateConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_LO_AG_CO_AUG_NIPROTAGGAUG),
                IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG,
                IIDs.NE_NE_PR_PR_OS_GL_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new GlobalAfiSafiReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP, new PeerGroupListReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG, new PeerGroupConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AFISAFI, new PeerGroupAfiSafiListReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new NeighborTransportConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_EB_CONFIG,new NeighborEbgpConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli),
            Sets.newHashSet(
                IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG,
                IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG,
                IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CO_AUG_BGPNEAFAUG_SOFTRECONFIGURATION
            )
        );
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new NeighborAfiSafiApplyPolicyConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_AP_CONFIG, new PeerGroupAfiSafiApplyPolicyConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_IP_CONFIG, new NeighborAfiSafiIpv4ConfigReader(cli));
        readRegistry.add(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_PRE_CONFIG,
                new NeighborAfiSafiPrefixLimitConfigReader(cli));
        readRegistry.add(IIDs.NET_NET_PRO_PRO_BGP_NEI_NEI_AFI_AFI_IPV_CONFIG, new NeighborAfiSafiIpv6ConfigReader(cli));
        readRegistry.add(IIDs.NETWO_NETWO_PROTO_PROTO_BGP_NEIGH_NEIGH_AFISA_AFISA_IPV6U_PREFI_CONFIG,
                new NeighborAfiSafiPrefixLimitConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new BgpLocalAggregateReader(cli),
                Sets.newHashSet(
                    IIDs.NE_NE_PR_PR_LO_AG_CONFIG,
                    IIDs.NE_NE_PR_PR_LO_AG_CO_AUG_NIPROTAGGAUG));
    }

}
