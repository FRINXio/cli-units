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

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ni.base.handler.vrf.AbstractL3VrfConfigWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L3VrfConfigWriter extends AbstractL3VrfConfigWriter {

    public L3VrfConfigWriter(Cli cli) {
        super(cli);
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "ip vrf {$data.name}\n"
            + "{$data|update(description,description `$data.description`\n,no description\n)}"
            + "{$data|update(route_distinguisher,rd `$data.route_distinguisher.string`\n,no rd\n)}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no ip vrf {$data.name}\n"
            + "end";

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        checkUniqueRd(writeContext, config.getName(), config.getRouteDistinguisher());
        return super.writeCurrentAttributesWResult(instanceIdentifier, config, writeContext);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        return fT(WRITE_TEMPLATE, "data", after, "before", before);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore, @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext)
            throws WriteFailedException.UpdateFailedException {
        checkUniqueRd(writeContext, dataAfter.getName(), dataAfter.getRouteDistinguisher());
        return super.updateCurrentAttributesWResult(id, dataBefore, dataAfter, writeContext);
    }

    @Override
    protected String deleteTemplate(Config config) {
        return fT(DELETE_TEMPLATE, "data", config);
    }

    private static void checkUniqueRd(WriteContext writeContext, String name, RouteDistinguisher routeDistinguisher) {
        if (routeDistinguisher == null) {
            return;
        }

        HashMultiset<String> rds = writeContext.readAfter(IIDs.NETWORKINSTANCES)
                .get()
                .getNetworkInstance()
                .stream()
                .filter(i -> i.getConfig()
                        .getType() == L3VRF.class)
                .filter(i -> i.getConfig()
                        .getRouteDistinguisher() != null)
                .map(NetworkInstance::getConfig)
                .collect(HashMultiset::create, (set, config) -> set.add(config.getRouteDistinguisher()
                        .getString()), (configs, configs2) -> configs.addAll(configs2));

        Preconditions.checkArgument(rds.count(routeDistinguisher.getString()) == 1,
                "Cannot configure VRF: %s, route-distinguisher: %s already in use", name, routeDistinguisher
                        .getString());
    }
}
