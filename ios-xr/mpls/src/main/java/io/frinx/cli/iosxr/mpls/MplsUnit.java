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

package io.frinx.cli.iosxr.mpls;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.iosxr.mpls.handler.LdpInterfaceConfigReader;
import io.frinx.cli.iosxr.mpls.handler.LdpInterfaceConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.LdpInterfaceReader;
import io.frinx.cli.iosxr.mpls.handler.LoadShareConfigReader;
import io.frinx.cli.iosxr.mpls.handler.LoadShareConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.NiMplsLdpGlobalAugReader;
import io.frinx.cli.iosxr.mpls.handler.NiMplsLdpGlobalAugWriter;
import io.frinx.cli.iosxr.mpls.handler.NiMplsRsvpIfSubscripAugReader;
import io.frinx.cli.iosxr.mpls.handler.NiMplsRsvpIfSubscripAugWriter;
import io.frinx.cli.iosxr.mpls.handler.P2pAttributesConfigReader;
import io.frinx.cli.iosxr.mpls.handler.P2pAttributesConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.RsvpInterfaceConfigReader;
import io.frinx.cli.iosxr.mpls.handler.RsvpInterfaceConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.RsvpInterfaceReader;
import io.frinx.cli.iosxr.mpls.handler.TeConfigReader;
import io.frinx.cli.iosxr.mpls.handler.TeConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.TeInterfaceConfigReader;
import io.frinx.cli.iosxr.mpls.handler.TeInterfaceConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.TeInterfaceReader;
import io.frinx.cli.iosxr.mpls.handler.TunnelConfigReader;
import io.frinx.cli.iosxr.mpls.handler.TunnelConfigWriter;
import io.frinx.cli.iosxr.mpls.handler.TunnelReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.LdpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.ldp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.NiMplsTeTunnelCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.NiMplsTeTunnelCiscoAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.TeGlobalAttributes1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.TeGlobalAttributes1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.CiscoMplsTeExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.CiscoMplsTeExtensionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.LspsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.SignalingProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeGlobalAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeInterfaceAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.lsps.ConstrainedPathBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.P2pTunnelAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.TunnelsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.SubscriptionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.RsvpTeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te.InterfaceAttributesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class MplsUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public MplsUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension
                        .rev171024.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco
                        .rev171024.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp
                        .rev180702.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension
                        .rev180822.$YangModuleInfoImpl.getInstance());
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
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MPLS, new NoopCliWriter<>()));

        // RSVP
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RSVPTE, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_IN_CONFIG, new RsvpInterfaceConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG
                        .augmentation(NiMplsRsvpIfSubscripAug.class), new NiMplsRsvpIfSubscripAugWriter(cli)),
                IIDs.NE_NE_MP_SI_RS_IN_IN_CONFIG);

        // LDP
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LDP, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_GLOBAL, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_GL_CONFIG, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_GL_CONFIG.augmentation(NiMplsLdpGlobalAug.class),
                new NiMplsLdpGlobalAugWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_INTERFACEATTRIBUTES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_IN_INTERFACES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_IN_IN_INTERFACE, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_MP_SI_LD_IN_IN_IN_CONFIG,
                new LdpInterfaceConfigWriter(cli)),IIDs.NE_NE_MP_SI_LD_GL_CONFIG
                .augmentation(NiMplsLdpGlobalAug.class));

        // TE
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_TEGLOBALATTRIBUTES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_TEGLOBALATTRIBUTES.augmentation(TeGlobalAttributes1.class),
                new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(TeConfigWriter.TE_CONFIG_IID, new TeConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_TE_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_MP_TE_IN_CONFIG, new TeInterfaceConfigWriter(cli)),
                TeConfigWriter.TE_CONFIG_IID);

        // Tunnel
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_LSPS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_LS_CONSTRAINEDPATH, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_LS_CO_TU_TUNNEL, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_LS_CO_TU_TU_CONFIG, new TunnelConfigWriter(cli)));
        writeRegistry.addAfter(new GenericWriter<>(CONFIG_IID, new LoadShareConfigWriter(cli)), IIDs
                .NE_NE_MP_LS_CO_TU_TU_CONFIG);
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_LS_CO_TU_TU_P2PTUNNELATTRIBUTES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_LS_CO_TU_TU_P2_CONFIG, new P2pAttributesConfigWriter(cli)));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.NE_NE_MPLS, MplsBuilder.class);

        // RSVP
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SIGNALINGPROTOCOLS, SignalingProtocolsBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RSVPTE, RsvpTeBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RS_INTERFACEATTRIBUTES, InterfaceAttributesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_MP_SI_RS_IN_INTERFACE, new RsvpInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_SI_RS_IN_IN_CONFIG, new RsvpInterfaceConfigReader()));
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RS_IN_IN_SUBSCRIPTION, SubscriptionBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG, ConfigBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG
                .augmentation(NiMplsRsvpIfSubscripAug.class), new NiMplsRsvpIfSubscripAugReader(cli)));

        // LDP
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_LDP, LdpBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_LD_GLOBAL, GlobalBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_LD_GL_CONFIG,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp.global
                .ConfigBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_SI_LD_GL_CONFIG
                .augmentation(NiMplsLdpGlobalAug.class), new NiMplsLdpGlobalAugReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_LD_INTERFACEATTRIBUTES,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface
                .attributes.top.InterfaceAttributesBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_LD_IN_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_MP_SI_LD_IN_IN_INTERFACE,
                new LdpInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_SI_LD_IN_IN_IN_CONFIG,
                new LdpInterfaceConfigReader()));

        // TE
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_TEGLOBALATTRIBUTES, TeGlobalAttributesBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_TEGLOBALATTRIBUTES.augmentation(TeGlobalAttributes1.class),
                TeGlobalAttributes1Builder.class);
        readRegistry.add(new GenericConfigReader<>(TeConfigReader.TE_CONFIG_IID, new TeConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_TEINTERFACEATTRIBUTES, TeInterfaceAttributesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_MP_TE_INTERFACE, new TeInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_TE_IN_CONFIG, new TeInterfaceConfigReader()));

        // Tunnel
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_LSPS, LspsBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_LS_CONSTRAINEDPATH, ConstrainedPathBuilder.class);
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_LS_CO_TUNNELS, TunnelsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_MP_LS_CO_TU_TUNNEL, new TunnelReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_LS_CO_TU_TU_CONFIG, new TunnelConfigReader(cli)));
        readRegistry.addStructuralReader(TE_EXT_IID, NiMplsTeTunnelCiscoAugBuilder.class);
        readRegistry.addStructuralReader(MPLS_EXT_IID, CiscoMplsTeExtensionBuilder.class);
        readRegistry.add(new GenericConfigReader<>(CONFIG_IID, new LoadShareConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_MP_LS_CO_TU_TU_P2PTUNNELATTRIBUTES, P2pTunnelAttributesBuilder
                .class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_LS_CO_TU_TU_P2_CONFIG,
                new P2pAttributesConfigReader(cli)));
    }

    private static final InstanceIdentifier<NiMplsTeTunnelCiscoAug> TE_EXT_IID = IIDs.NE_NE_MP_LS_CO_TU_TUNNEL
            .augmentation(NiMplsTeTunnelCiscoAug.class);
    private static final InstanceIdentifier<CiscoMplsTeExtension> MPLS_EXT_IID
            = TE_EXT_IID.child(CiscoMplsTeExtension.class);
    private static final InstanceIdentifier<Config> CONFIG_IID = MPLS_EXT_IID.child(Config.class);

    @Override
    public String toString() {
        return "IOS XR MPLS (Openconfig) translate unit";
    }
}
