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

package io.frinx.cli.unit.ios.ifc.handler;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import io.frinx.cli.unit.ios.ifc.Util;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    // FIXME : add shutdown when Boolean vs boolean is fixed
    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{$data|update(mtu,mtu `$data.mtu`\n,no mtu\n)}"
            + "{$data|update(description,description `$data.description`\n,no description\n)}"
            //  + "{$data|update(is_enabled,shutdown\n,no shutdown\n}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($pt) %}{$pt}\n{% endif %}"
            + "{% if ($portType) %}port-type {$portType}\n{% else %}no port-type\n{% endif %}"
            + "{% if ($mode) %}switchport mode {$mode}\n{% else %}no switchport mode\n{% endif %}"
            + "{% if ($snmpTrap) %}no snmp trap link-status\n{% else %}snmp trap link-status\n{% endif %}"
            + "{% if ($switchportAccessVlan) %}switchport access vlan {$switchportAccessVlan}\n"
            + "{% else %}no switchport access vlan\n{% endif %}"
            + "{% if ($switchportTrunkAllowed) %}switchport trunk allowed vlan {$switchportTrunkAllowed}\n"
            + "{% else %}no switchport trunk allowed vlan\n{% endif %}"
            + "{% if ($stormControl) %}{$stormControl}{% endif %}"
            + "end\n";

    private static final String WRITE_TEMPLATE_VLAN = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{$data|update(mtu,mtu `$data.mtu`\n,no mtu\n)}"
            + "{$data|update(description,description `$data.description`\n,no description\n)}"
            //  + "{$data|update(is_enabled,shutdown\n,no shutdown\n}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($pt) %}{$pt}\n{% endif %}"
            + "{% if ($snmpTrap) %}no snmp trap link-status\n{% else %}snmp trap link-status\n{% endif %}"
            + "{% if ($ipRedirects) %}no ip redirects\n{% else %}ip redirects\n{% endif %}"
            + "{% if ($ipUnreachables) %}no ip unreachables\n{% else %}ip unreachables\n{% endif %}"
            + "{% if ($ipProxyArp) %}no ip proxy-arp\n{% else %}ip proxy-arp\n{% endif %}"
            + "end\n";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no interface {$data.name}\n"
            + "end";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        IfCiscoExtAug ciscoExtAug = after.getAugmentation(IfCiscoExtAug.class);
        if (isPhysicalInterface(after)) {
            return fT(WRITE_TEMPLATE,
                "before", before,
                "data", after,
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
                "pt", getPhysicalType(before, after),
                "portType", (ciscoExtAug != null) ? ciscoExtAug.getPortType() : null,
                "mode", (ciscoExtAug != null) ? ciscoExtAug.getSwitchportMode() : null,
                "snmpTrap", (ciscoExtAug != null && ciscoExtAug.isSnmpTrapLinkStatus() != null
                            && !ciscoExtAug.isSnmpTrapLinkStatus()) ? Chunk.TRUE : null,
                "stormControl", getStormControlCommands(after));
        }
        return fT(WRITE_TEMPLATE_VLAN,
            "before", before,
            "data", after,
            "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
            "pt", getPhysicalType(before, after),
            "snmpTrap", (ciscoExtAug != null && ciscoExtAug.isSnmpTrapLinkStatus() != null
                        && !ciscoExtAug.isSnmpTrapLinkStatus()) ? Chunk.TRUE : null,
            "ipRedirects", (ciscoExtAug != null && ciscoExtAug.isIpRedirects() != null
                        && !ciscoExtAug.isIpRedirects()) ? Chunk.TRUE : null,
            "ipUnreachables", (ciscoExtAug != null && ciscoExtAug.isIpUnreachables() != null
                        && !ciscoExtAug.isIpUnreachables()) ? Chunk.TRUE : null,
            "ipProxyArp", (ciscoExtAug != null && ciscoExtAug.isIpProxyArp() != null && !ciscoExtAug.isIpProxyArp())
                        ? Chunk.TRUE : null);
    }

    private String getPhysicalType(Config dataBefore, Config dataAfter) {
        String typeBefore = getPhysicalType(dataBefore);
        String typeAfter = getPhysicalType(dataAfter);
        if (!Objects.equals(typeAfter, typeBefore)) {
            if (typeAfter != null) {
                return "media-type " + typeAfter;
            }
            return "no media-type";
        }
        return null;
    }

    private String getPhysicalType(Config config) {
        if (config != null) {
            IfSaosAug saosAug = config.getAugmentation(IfSaosAug.class);
            if (saosAug != null) {
                PhysicalType physicalType = saosAug.getPhysicalType();
                return physicalType != null ? physicalType.getName() : null;
            }
        }
        return null;
    }

    private String getStormControlCommands(Config after) {
        StringBuilder stringBuilder = new StringBuilder();
        if (after != null && after.getAugmentation(IfCiscoExtAug.class) != null) {
            IfCiscoExtAug aug = after.getAugmentation(IfCiscoExtAug.class);
            if (aug.getStormControl() != null) {
                // create commands for required storm controls
                for (StormControl stormControl : aug.getStormControl()) {
                    stringBuilder.append("storm-control ").append(stormControl.getAddress().getName());
                    stringBuilder.append(" level ").append(stormControl.getLevel().toString()).append("\n");
                }
            }
        }
        // create no-commands for remaining storm controls
        for (StormControl.Address address : StormControl.Address.values()) {
            if (!stringBuilder.toString().contains(address.getName())) {
                stringBuilder.append("no storm-control ").append(address.getName()).append(" level\n");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data);
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }
}
