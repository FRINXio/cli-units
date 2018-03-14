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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class L3VrfConfigWriter implements CliWriter<Config> {

    static final String WRITE_CURR_ATTR = "system-view\n" +
            "ip vpn-instance {$config.name}\n" +
            "{% if($config.description) %}description {$config.description}\n{% endif %}\n" +
            "ipv4-family\n" +
            "{% if($config.route_distinguisher.string) %}route-distinguisher {$config.route_distinguisher.string}\n{% endif %}" +
            "commit\n" +
            "return";

    static final String DELETE_CURR_ATTR = "system-view\n" +
            "undo ip vpn-instance {$config.name}\n" +
            "commit\n" +
            "return";

    private final Cli cli;

    public L3VrfConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {
        if(config.getType().equals(L3VRF.class)) {
            blockingWriteAndRead(cli, instanceIdentifier, config, fT(WRITE_CURR_ATTR,
                    "config", config));
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        RouteDistinguisher rdBefore = dataBefore.getRouteDistinguisher();
        RouteDistinguisher rdAfter = dataAfter.getRouteDistinguisher();

        Preconditions.checkArgument(Objects.equals(rdBefore, rdAfter),
                "Cannot update route distinguisher once l3vrf already created");

        if(dataAfter.getType().equals(L3VRF.class)) {
            blockingWriteAndRead(cli, id, dataAfter,
                    "system-view",
                    f("ip vpn-instance %s", dataAfter.getName()),
                    dataAfter.getDescription() == null ? "undo description" : f("description %s", dataAfter.getDescription()),
                    "commit",
                    "return");
        }
    }


    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {

        if(config.getType().equals(L3VRF.class)) {

            blockingDeleteAndRead(cli, instanceIdentifier, fT(DELETE_CURR_ATTR,
                    "config", config));
        }
    }
}
