/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.isis.handler.global;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.redistribution.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.Af;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisRedistributionConfigWriter implements CliWriter<Config> {

    private Cli cli;

    // redistribute command cannot update only a specific field. (e.g. even if you only want to update metric,
    // you have to put level and route-policy with the current values)
    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            router isis {$instanceName}
             address-family {$afi} {$safi}
              redistribute {$data.protocol} {$data.instance}{% if ($level) %} {$level}{% endif %}{% if ($data.metric) %} metric {$data.metric}{% endif %}{% if ($data.route_policy) %} route-policy {$data.route_policy}{% endif %}
            root""";

    private static final String DELETE_TEMPLATE = """
            router isis {$instanceName}
             address-family {$afi} {$safi}
              no redistribute {$data.protocol} {$data.instance}
            root""";

    public IsisRedistributionConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(
            IsisRedistributionReader.SUPPORTED_REDISTRIBUTION_PROTOCOLS.contains(config.getProtocol()),
            "Unsupported redistribute protocol : %s", config.getProtocol());

        String instanceName = id.firstKeyOf(Protocol.class).getName();
        AfKey afKey = id.firstKeyOf(Af.class);

        blockingWriteAndRead(cli, id, config,
                fT(WRITE_TEMPLATE,
                    "instanceName", instanceName,
                    "afi", IsisGlobalAfiSafiReader.convertAfiTypeToString(afKey.getAfiName()),
                    "safi", IsisGlobalAfiSafiReader.convertSafiTypeToString(afKey.getSafiName()),
                    "level", IsisRedistributionConfigReader.convertRedistLevelToString(config.getLevel()),
                    "data", config));
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore, @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String instanceName = id.firstKeyOf(Protocol.class).getName();
        AfKey afKey = id.firstKeyOf(Af.class);

        blockingDeleteAndRead(cli, id,
            fT(DELETE_TEMPLATE,
                "instanceName", instanceName,
                "afi", IsisGlobalAfiSafiReader.convertAfiTypeToString(afKey.getAfiName()),
                "safi", IsisGlobalAfiSafiReader.convertSafiTypeToString(afKey.getSafiName()),
                "data", config));
    }
}