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

package io.frinx.cli.unit.iosxe.ifc.handler.cfm;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.mip.Level;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



public class CfmMipWriter implements CliWriter<Level> {
    private static String WRITE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            {% if ($vlanRemove) %}no ethernet cfm mip level {$level} vlan {$vlanRemove}
            {% endif %}{% if ($vlanAdd) %}ethernet cfm mip level {$level} vlan {$vlanAdd}
            {% endif %}end""";

    private static String DELETE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            {% if ($level) %}no ethernet cfm mip level {$level}
            {% endif %}end""";

    private Cli cli;

    public CfmMipWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Level> id,
                                       @NotNull Level level,
                                       @NotNull WriteContext ctx) throws WriteFailedException {
        final Short levelValue = id.firstKeyOf(Level.class).getLevel();
        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, id, level, updateTemplate(level, null, ifcName, levelValue));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Level> id,
                                        @NotNull Level dataBefore,
                                        @NotNull Level dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final Short levelValue = id.firstKeyOf(Level.class).getLevel();
        String ifcName = id.firstKeyOf(Interface.class).getName();
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataAfter, dataBefore, ifcName, levelValue));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Level> id,
                                        @NotNull Level level,
                                        @NotNull WriteContext ctx) throws WriteFailedException {
        final Short levelValue = id.firstKeyOf(Level.class).getLevel();
        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, id, updateTemplate(null, level, ifcName, levelValue));
    }

    private String updateTemplate(Level after, Level before, String ifcName, Short levelValue) {
        if (after != null) {
            return fT(WRITE_TEMPLATE, "ifc_name", ifcName,
                    "level", levelValue,
                    "vlanRemove", getVlansDiff(before, after),
                    "vlanAdd", getVlansDiff(after, before));
        }
        return fT(DELETE_TEMPLATE, "ifc_name", ifcName, "level", levelValue);
    }

    private Object getVlansDiff(Level first, Level second) {
        if (first != null && first.getVlan() != null) {
            if (second != null && second.getVlan() != null) {
                List<String> vlanIds = first.getVlan().stream()
                    .filter(vlan_id -> !second.getVlan().contains(vlan_id))
                    .collect(Collectors.toList());
                if (vlanIds.isEmpty()) {
                    return null;
                }
                return getVlansString(vlanIds);
            }
            return getVlansString(first.getVlan());
        }
        return null;
    }

    private String getVlansString(List<String> vlanIds) {
        StringBuilder str = new StringBuilder();

        for (String vlan : vlanIds) {
            str.append(vlan).append(",");
        }
        str.deleteCharAt(str.length() - 1);

        return str.toString();
    }
}