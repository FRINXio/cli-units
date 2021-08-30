/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.ifc.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = "system-view\n"
            + "interface {$ifc_name}\n"
            + "{% if ($data.access_vlan) %}port default vlan {$data.access_vlan.value}\n"
            + "{% else %}undo port default vlan\n{% endif %}"
            + "{% if ($trunk_vlans) %}port trunk allow-pass vlan {$trunk_vlans}\n"
            + "{% else %}undo port trunk allow-pass vlan all\n{% endif %}"
            + "{% if ($data.native_vlan.value) %}port trunk pvid vlan {$data.native_vlan.value}\n"
            + "{% else %}undo port trunk pvid vlan\n{% endif %}"
            + "{% if ($data.interface_mode.name) %}port link-type {$data.interface_mode.name}\n"
            + "{% else %}undo port link-type\n{% endif %}"
            + "return";

    private static final String DELETE_TEMPLATE = "system-view\n"
            + "undo interface {$ifc_name}\n"
            + "return";

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
                fT(WRITE_UPDATE_TEMPLATE,
                        "ifc_name", ifcName,
                        "data", config,
                        "trunk_vlans", convertVlansToString(config)));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(instanceIdentifier, dataAfter, writeContext);
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
            config.getTrunkVlans().forEach(id -> stringBuilder.append(parseVlanRange(id.getVlanRange().getValue())));
            return stringBuilder.toString();
        }
        return null;
    }

    private String parseVlanRange(String vlanRange) {
        String[] ranges = vlanRange.split("\\..");
        return ranges[0] + " to " + ranges[1];
    }
}
