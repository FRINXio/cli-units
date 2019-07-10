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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.redistribution.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.Af;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisRedistributionConfigWriter implements CliWriter<Config> {

    private Cli cli;

    // redistribute command cannot update only a specific field. (e.g. even if you only want to update metric,
    // you have to put level and route-policy with the current values)
    private static final String WRITE_TEMPLATE = "router isis {$instanceName}\n"
        + " address-family {$afi} {$safi}\n"
        + "  redistribute {$data.protocol} {$data.instance}"
        + "{% if ($level) %} {$level}{% endif %}"
        + "{% if ($data.metric) %} metric {$data.metric}{% endif %}"
        + "{% if ($data.route_policy) %} route-policy {$data.route_policy}{% endif %}"
        + "\n"
        + "root";

    private static final String DELETE_TEMPLATE = "router isis {$instanceName}\n"
        + " address-family {$afi} {$safi}\n"
        + "  no redistribute {$data.protocol} {$data.instance}"
        + "\n"
        + "root";

    public IsisRedistributionConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

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
        InstanceIdentifier<Config> id,
        Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

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
