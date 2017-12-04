/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.common.CompositeWriter;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.NetworkInstanceStateReader;
import io.frinx.cli.handlers.def.DefaultConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol.ProtocolStateReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRNetworkInstanceUnit implements TranslateUnit {
    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosXRNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // VRFs
        rRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader()));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_STATE, new NetworkInstanceStateReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PROTOCOLS, ProtocolsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader()));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader()));
    }

    private void provideWriters(@Nonnull ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        // No handling required on the network instance level
        wRegistry.add(new GenericWriter<>(IIDs.NE_NETWORKINSTANCE, new NoopCliWriter<>()));

        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_CONFIG,
                        new CompositeWriter<>(Lists.newArrayList(
                                new DefaultConfigWriter()))),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PROTOCOL, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR Network Instance (Openconfig) translate unit";
    }
}
