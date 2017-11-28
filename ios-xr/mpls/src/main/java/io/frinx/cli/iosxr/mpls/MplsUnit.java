/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
import io.frinx.cli.iosxr.mpls.handler.*;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang.mpls.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.mpls.top.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.SignalingProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeInterfaceAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.SubscriptionBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev170824.rsvp.global.RsvpTeBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te.InterfaceAttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

import javax.annotation.Nonnull;
import java.util.Set;

public class MplsUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public MplsUnit(@Nonnull final TranslationUnitCollector registry) {
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
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.io.yang.mpls.rev171024.$YangModuleInfoImpl.getInstance());
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
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MPLS, new NoopCliWriter<>()));

        // RSVP
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RSVPTE, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_IN_CONFIG, new RsvpInterfaceConfigWriter(cli)));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG, new NoopCliWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG.augmentation(NiMplsRsvpIfSubscripAug.class), new NiMplsRsvpIfSubscripAugWriter(cli)),
            IIDs.NE_NE_MP_SI_RS_IN_IN_CONFIG);

        // TE
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_TE_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_MP_TE_IN_CONFIG, new TeInterfaceConfigWriter(cli)));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_MPLS, MplsBuilder.class);

        // RSVP
        rRegistry.addStructuralReader(IIDs.NE_NE_MP_SIGNALINGPROTOCOLS, SignalingProtocolsBuilder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RSVPTE, RsvpTeBuilder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RS_INTERFACEATTRIBUTES, InterfaceAttributesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_MP_SI_RS_IN_INTERFACE, new RsvpInterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_SI_RS_IN_IN_CONFIG, new RsvpInterfaceConfigReader()));
        rRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RS_IN_IN_SUBSCRIPTION, SubscriptionBuilder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG, ConfigBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_SI_RS_IN_IN_SU_CONFIG.augmentation(NiMplsRsvpIfSubscripAug.class), new NiMplsRsvpIfSubscripAugReader(cli)));

        // TE
        rRegistry.addStructuralReader(IIDs.NE_NE_MP_TEINTERFACEATTRIBUTES, TeInterfaceAttributesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_MP_TE_INTERFACE, new TeInterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_MP_TE_IN_CONFIG, new TeInterfaceConfigReader()));
    }

    @Override
    public String toString() {
        return "IOS XR MPLS (Openconfig) translate unit";
    }
}
