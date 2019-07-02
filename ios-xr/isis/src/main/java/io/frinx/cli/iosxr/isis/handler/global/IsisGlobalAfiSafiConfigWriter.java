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

package io.frinx.cli.iosxr.isis.handler.global;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.AFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.MULTICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.SAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.af.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisGlobalAfiSafiConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final String COMMAND_TEMPLATE = "router isis {$instanceName}\n"
        + "{% if ($delete) %}no {% endif %}address-family {$afi} {$safi}\n"
        + "root";

    public IsisGlobalAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        String instanceName = id.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, id, config,
                fT(COMMAND_TEMPLATE,
                    "instanceName", instanceName,
                    "afi", convertAfiTypeToString(config.getAfiName()),
                    "safi", convertSafiTypeToString(config.getSafiName())));
    }

    @Override
    public void updateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        // This writer deals only key attributes of parent container,
        // so there is no modifiable attributes.
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        String instanceName = id.firstKeyOf(Protocol.class).getName();

        blockingDeleteAndRead(cli, id,
            fT(COMMAND_TEMPLATE,
                "instanceName", instanceName,
                "delete", true,
                "afi", convertAfiTypeToString(config.getAfiName()),
                "safi", convertSafiTypeToString(config.getSafiName())));
    }

    private static String convertAfiTypeToString(Class<? extends AFITYPE> afi) {
        if (afi == IPV4.class) {
            return "ipv4";
        } else if (afi == IPV6.class) {
            return "ipv6";
        } else {
            throw new IllegalArgumentException("Unknown AFI type " + afi.getName());
        }
    }

    private static String convertSafiTypeToString(Class<? extends SAFITYPE> safi) {
        if (safi == UNICAST.class) {
            return "unicast";
        } else if (safi == MULTICAST.class) {
            return "multicast";
        } else {
            throw new IllegalArgumentException("Unknown SAFI type " + safi.getName());
        }
    }
}
