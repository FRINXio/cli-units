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

package io.frinx.cli.unit.ios.bgp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.bgp.handler.GlobalAfiSafiConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.ios.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.ios.bgp.handler.GlobalConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.ios.bgp.handler.GlobalStateReader;
import io.frinx.cli.unit.ios.bgp.handler.local.aggregates.BgpLocalAggregateConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.local.aggregates.BgpLocalAggregateConfigWriter;
import io.frinx.cli.unit.ios.bgp.handler.local.aggregates.BgpLocalAggregateReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborAfiSafiPolicyConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborAfiSafiReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborPolicyConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborRouteReflectorConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborStateReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborTimersConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborTransportConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborWriter;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.PrefixesReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupAfiSafiPolicyConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupAfiSafiReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupPolicyConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupRouteReflectorConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupTransportConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.peergroup.PeerGroupWriter;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
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
        return IosDevices.IOS_ONLY;
    }

    @Override
    protected String getUnitName() {
        return "IOS BGP (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_BGP_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("% Configure the peer-group .+ first", Pattern.DOTALL),
                Pattern.compile("% Cannot configure the local system as neighbor", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)BGP(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% Topology global(?-i).*", Pattern.DOTALL)
        ));
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_BG_GL_CO_AUG_BGPGLOBALCONFIGAUG),
                IIDs.NE_NE_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigWriter(cli),
                Collections.singleton(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CO_AUG_GLOBALAFISAFICONFIGAUG),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG);

        // Peer group writer, handle also subtrees
        writeRegistry.subtreeAddAfter(
                IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP, new PeerGroupWriter(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_TRANSPORT,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_TR_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_ROUTEREFLECTOR,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_RO_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_APPLYPOLICY,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_AP_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_AFISAFIS,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AFISAFI,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_APPLYPOLICY,
                        IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_AP_CONFIG),
                IIDs.NE_NE_CONFIG,
                IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG);

        // Neighbor writer, handle also subtrees
        writeRegistry.subtreeAddAfter(
                IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborWriter(cli),
                Sets.newHashSet(
                        IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_ROUTEREFLECTOR,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_RO_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_APPLYPOLICY,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_APPLYPOLICY,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_TIMERS,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_TI_CONFIG,
                        IIDs.NE_NE_PR_PR_BG_NE_NE_CO_AUG_BGPNEIGHBORCONFIGAUG),
                IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP);

        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new BgpLocalAggregateConfigWriter(cli),
                IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG,
                IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_STATE, new GlobalStateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new GlobalAfiSafiReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigReader(cli));

        provideNeighborReaders(readRegistry, cli);
        providePeerGroupReaders(readRegistry, cli);
    }

    private void provideNeighborReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli));

        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_BG_NE_NE_CO_AUG_BGPNEIGHBORCONFIGAUG));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_STATE, new NeighborStateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_TI_CONFIG, new NeighborTimersConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_AP_CONFIG, new NeighborAfiSafiPolicyConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG, new NeighborTransportConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG, new NeighborPolicyConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_ST_PREFIXES, new PrefixesReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_RO_CONFIG, new NeighborRouteReflectorConfigReader(cli));

        // Local aggregates
        readRegistry.add(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new BgpLocalAggregateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new BgpLocalAggregateConfigReader());

    }

    private void providePeerGroupReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PEERGROUP, new PeerGroupReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_CONFIG, new PeerGroupConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AFISAFI, new PeerGroupAfiSafiReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_CONFIG));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_AF_AF_AP_CONFIG, new PeerGroupAfiSafiPolicyConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_TR_CONFIG, new PeerGroupTransportConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_AP_CONFIG, new PeerGroupPolicyConfigReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_PE_PE_RO_CONFIG, new PeerGroupRouteReflectorConfigReader(cli));
    }
}
