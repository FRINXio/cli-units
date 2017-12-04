/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericOperReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.handlers.NextHopConfigReader;
import io.frinx.cli.ios.local.routing.handlers.NextHopReader;
import io.frinx.cli.ios.local.routing.handlers.NextHopStateReader;
import io.frinx.cli.ios.local.routing.handlers.StaticConfigReader;
import io.frinx.cli.ios.local.routing.handlers.StaticReader;
import io.frinx.cli.ios.local.routing.handlers.StaticStateReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top.StaticRoutesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LocalRoutingUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public LocalRoutingUnit(@Nonnull final TranslationUnitCollector registry) {
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
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_STATICROUTES, StaticRoutesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_ST_STATIC, new StaticReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_ST_ST_STATE, new StaticStateReader()));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_ST_ST_CONFIG, new StaticConfigReader()));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_ST_ST_NEXTHOPS, NextHopsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP, new NextHopReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_CONFIG, new NextHopConfigReader(cli)));
        rRegistry.add(new GenericOperReader<>(IIDs.NE_NE_PR_PR_ST_ST_NE_NE_STATE, new NextHopStateReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS Local Routing (Openconfig) translate unit";
    }
}
