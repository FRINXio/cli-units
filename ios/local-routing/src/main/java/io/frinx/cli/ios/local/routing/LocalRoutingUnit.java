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
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.handlers.StaticConfigReader;
import io.frinx.cli.ios.local.routing.handlers.StaticNextHopReader;
import io.frinx.cli.ios.local.routing.handlers.StaticReader;
import io.frinx.cli.ios.local.routing.handlers.StaticStateReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top.StaticRoutes;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top.StaticRoutesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHops;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LocalRoutingUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private static final InstanceIdentifier<NetworkInstances> NETWORK_INSTANCES_ID = InstanceIdentifier
            .create(NetworkInstances.class);
    private static final InstanceIdentifier<NetworkInstance> NETWORK_INSTANCE_ID = NETWORK_INSTANCES_ID
            .child(NetworkInstance.class);
    private static final InstanceIdentifier<Protocols> PROTOCOLS_ID = NETWORK_INSTANCE_ID
            .child(Protocols.class);
    private static final InstanceIdentifier<Protocol> PROTOCOL_ID = PROTOCOLS_ID
            .child(Protocol.class);

    private static final InstanceIdentifier<StaticRoutes> STATIC_ROUTES_ID = PROTOCOL_ID.child(StaticRoutes.class);
    private static final InstanceIdentifier<Static> STATIC_ID = STATIC_ROUTES_ID.child(Static.class);
    private static final InstanceIdentifier<State> STATE_ID = STATIC_ID.child(State.class);
    private static final InstanceIdentifier<Config> CONFIG_ID = STATIC_ID.child(Config.class);
    private static final InstanceIdentifier<NextHops> NEXT_HOPS_ID = STATIC_ID.child(NextHops.class);
    private static final InstanceIdentifier<NextHop> NEXT_HOP_ID = NEXT_HOPS_ID.child(NextHop.class);

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
        return Collections.EMPTY_SET;
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(STATIC_ROUTES_ID, StaticRoutesBuilder.class);
        rRegistry.add(new GenericListReader<>(STATIC_ID, new StaticReader(cli)));
        rRegistry.add(new GenericReader<>(STATE_ID, new StaticStateReader()));
        rRegistry.add(new GenericReader<>(CONFIG_ID, new StaticConfigReader()));
        rRegistry.addStructuralReader(NEXT_HOPS_ID, NextHopsBuilder.class);
        rRegistry.add(new GenericListReader<>(NEXT_HOP_ID, new StaticNextHopReader(cli)));

    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS Local Routing (Openconfig) translate unit";
    }
}
