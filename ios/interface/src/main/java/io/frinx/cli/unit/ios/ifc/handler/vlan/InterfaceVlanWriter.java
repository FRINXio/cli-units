/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.ifc.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$ifc_name}\n"
            + "{% if ($config.access_vlan) %}switchport access vlan {$config.access_vlan.value}\n{% endif %}"
            + "{% if ($config.native_vlan) %}switchport trunk native vlan {$config.native_vlan.value}\n{% endif %}"
            + "{% if ($trunk_vlans) %}switchport trunk allowed vlan {$trunk_vlans}\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "interface {$ifc_name}\n"
            + "no switchport access vlan\n"
            + "no switchport trunk native vlan\n"
            + "no switchport trunk allowed vlan\n"
            + "end";

    private final Cli cli;

    public InterfaceVlanWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "ifc_name", ifcName,
                        "config", config,
                        "trunk_vlans", convertVlansToString(config)));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "ifc_name", ifcName));
    }

    private String convertVlansToString(Config config) {
        if (config.getTrunkVlans() != null) {
            final StringBuilder stringBuilder = new StringBuilder();
            config.getTrunkVlans().forEach(id -> stringBuilder.append(id.getVlanId().getValue()).append(","));
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return stringBuilder.toString();
        }
        return null;
    }

}
