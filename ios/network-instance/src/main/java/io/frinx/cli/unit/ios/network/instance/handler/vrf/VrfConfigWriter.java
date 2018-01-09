/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public VrfConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
            "ip vrf {$config.name}\n" +
            "{.if ($config.description) }description {$config.description}\n{.else}no description\n{/if}" +
            "{.if ($config.route_distinguisher.string) }rd {$config.route_distinguisher.string}\n{.else}no rd\n{/if}" +
            "end";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        if (config.getType().equals(L3VRF.class)) {

            checkUniqueRd(writeContext, config.getName(), config.getRouteDistinguisher());

            blockingWriteAndRead(cli, instanceIdentifier, config,
                    fT(WRITE_TEMPLATE,
                            "vrf", config.getName(),
                            "config", config));
        }
    }

    private static void checkUniqueRd(WriteContext writeContext, String name, RouteDistinguisher routeDistinguisher) {
        if (routeDistinguisher == null) {
            return;
        }

        HashMultiset<String> rds = writeContext.readAfter(IIDs.NETWORKINSTANCES).get()
                .getNetworkInstance()
                .stream()
                .filter(i -> i.getConfig().getType() == L3VRF.class)
                .filter(i -> i.getConfig().getRouteDistinguisher() != null)
                .map(NetworkInstance::getConfig)
                .collect(HashMultiset::<String>create, (set, config) -> set.add(config.getRouteDistinguisher().getString()), (configs, configs2) -> configs.addAll(configs2));

        Preconditions.checkArgument(rds.count(routeDistinguisher.getString()) == 1,
                "Cannot configure VRF: %s, route-distinguisher: %s already in use", name, routeDistinguisher.getString());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        // Not using trivial update. Write template supports update as well
        // The problem with updating VRF by delete+write is that on IOS, VRF deletion happens in background after
        // delete command has been issued. While this is happening a VRF cannot be created. So to avoid that, do not
        // delete VRF during update
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    private static final String DELETE_TEMPLATE = "configure terminal\n" +
            "no ip vrf {$config.name}\n" +
            "end";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {

        if (config.getType().equals(L3VRF.class)) {

            blockingDeleteAndRead(cli, instanceIdentifier,
                    fT(DELETE_TEMPLATE,
                            "config", config));
        }
    }
}
