/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package io.frinx.cli.ios.bgp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.BgpReader;
import io.frinx.cli.ios.bgp.handler.RibReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BgpUnit implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(BgpUnit.class);

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
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl.getInstance(),
            org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.$YangModuleInfoImpl.getInstance());
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
        LOG.trace("readers {}", rRegistry);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        // no writers
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // FIXME BGP reader reads also subtree nodes, it needs to be split into multiple readers. Because this
        // way it is only possible to perform read of the root node: bgp and also, it will not be possible to extend
        // from other units
        rRegistry.add(new GenericReader<>(IIDs.NE_NE_PR_PR_BGP, new BgpReader(cli)));
        // FIXME same for RIB
        rRegistry.add(new GenericReader<>(io.frinx.openconfig.openconfig.rib.IIDs.BGPRIB, new RibReader(cli)));
    }

    @Override
    public String toString() {
        return "ios-bgp-unit";
    }
}
