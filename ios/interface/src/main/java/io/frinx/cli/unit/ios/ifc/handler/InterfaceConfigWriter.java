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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
            + "{% if ($portSecurity) %}{$portSecurity}\n{% endif %}"
            + "{% if ($portSecurityMaximum) %}{$portSecurityMaximum}\n{% endif %}"
            + "{% if ($portSecurityViolation) %}{$portSecurityViolation}\n{% endif %}"
            + "{% if ($portSecurityAgingType) %}{$portSecurityAgingType}\n{% endif %}"
            + "{% if ($portSecurityAgingTime) %}{$portSecurityAgingTime}\n{% endif %}"
            + "{% if ($portSecurityAgingStatic) %}{$portSecurityAgingStatic}\n{% endif %}"
            + "{% if ($l2protocols) %}{$l2protocols}{% endif %}"
            + "{% if ($lldpTransmit) %}{$lldpTransmit}\n{% endif %}"
            + "{% if ($lldpReceive) %}{$lldpReceive}\n{% endif %}"
            + "{% if ($cdpEnable) %}{$cdpEnable}\n{% endif %}"
            + "end\n";

    private static final String WRITE_TEMPLATE_VLAN = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{$data|update(mtu,mtu `$data.mtu`\n,no mtu\n)}"
            + "{$data|update(description,description `$data.description`\n,no description\n)}"
            //  + "{$data|update(is_enabled,shutdown\n,no shutdown\n}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($pt) %}{$pt}\n{% endif %}"
            + "{% if ($snmpTrap) %}{$snmpTrap}\n{% endif %}"
            + "{% if ($ipRedirects) %}{$ipRedirects}\n{% endif %}"
            + "{% if ($ipUnreachables) %}{$ipUnreachables}\n{% endif %}"
            + "{% if ($ipProxyArp) %}{$ipProxyArp}\n{% endif %}"
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
                "stormControl", getStormControlCommands(after),
                "portSecurity", getPortSecurityEnableCommand(before, after),
                "portSecurityMaximum", getPortSecurityMaximumCommand(before, after),
                "portSecurityViolation", getSwitchportPortSecurityViolation(before, after),
                "portSecurityAgingType", getSwitchportPortSecurityAgingType(before, after),
                "portSecurityAgingTime", getSwitchportPortSecurityAgingTime(before, after),
                "portSecurityAgingStatic", getSwitchportPortSecurityAgingStatic(before, after),
                "l2protocols", getl2Protocols(before, after),
                "lldpTransmit", getLldpTransmit(ciscoExtAug),
                "lldpReceive", getLldpReceive(ciscoExtAug),
                "cdpEnable", getCdpEnable(ciscoExtAug));
        }
        return fT(WRITE_TEMPLATE_VLAN,
            "before", before,
            "data", after,
            "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
            "pt", getPhysicalType(before, after),
            "snmpTrap", getSnmpTrap(before, after),
            "ipRedirects", getIpRedirect(before, after),
            "ipUnreachables", getIpUnreachables(before, after),
            "ipProxyArp", getIpProxyArp(before, after));
    }

    private String getSnmpTrap(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSnmpTrapLinkStatus() != null) {
                return ciscoExtAugAfter.isSnmpTrapLinkStatus() ? "snmp trap link-status" : "no snmp trap link-status";
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSnmpTrapLinkStatus() != null) {
            return ciscoExtAugAfter.isSnmpTrapLinkStatus() ? "snmp trap link-status" : "no snmp trap link-status";
        } else if (ciscoExtAugBefore.isSnmpTrapLinkStatus() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSnmpTrapLinkStatus() != null) {
                return ciscoExtAugAfter.isSnmpTrapLinkStatus() ? "snmp trap link-status" : "no snmp trap link-status";
            } else {
                return "no snmp trap link-status";
            }
        }
        return null;
    }

    private String getIpRedirect(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpRedirects() != null) {
                return ciscoExtAugAfter.isIpRedirects() ? "ip redirects" : "no ip redirects";
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpRedirects() != null) {
            return ciscoExtAugAfter.isIpRedirects() ? "ip redirects" : "no ip redirects";
        } else if (ciscoExtAugBefore.isIpRedirects() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpRedirects() != null) {
                return ciscoExtAugAfter.isIpRedirects() ? "ip redirects" : "no ip redirects";
            } else {
                return "no ip redirects";
            }
        }
        return null;
    }

    private Object getIpUnreachables(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpUnreachables() != null) {
                return ciscoExtAugAfter.isIpUnreachables() ? "ip unreachables" : "no ip unreachables";
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpUnreachables() != null) {
            return ciscoExtAugAfter.isIpUnreachables() ? "ip unreachables" : "no ip unreachables";
        } else if (ciscoExtAugBefore.isIpUnreachables() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpUnreachables() != null) {
                return ciscoExtAugAfter.isIpUnreachables() ? "ip unreachables" : "no ip unreachables";
            } else {
                return "no ip unreachables";
            }
        }
        return null;
    }

    private String getIpProxyArp(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpProxyArp() != null) {
                return ciscoExtAugAfter.isIpProxyArp() ? "ip proxy-arp" : "no ip proxy-arp";
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpProxyArp() != null) {
            return ciscoExtAugAfter.isIpProxyArp() ? "ip proxy-arp" : "no ip proxy-arp";
        } else if (ciscoExtAugBefore.isIpProxyArp() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isIpProxyArp() != null) {
                return ciscoExtAugAfter.isIpProxyArp() ? "ip proxy-arp" : "no ip proxy-arp";
            } else {
                return "no ip proxy-arp";
            }
        }
        return null;
    }

    private Object getCdpEnable(IfCiscoExtAug after) {
        String command = "cdp enable";
        return after != null ? getCommandHelper(after.isCdpEnable(), command) : command;
    }

    private String getLldpReceive(IfCiscoExtAug after) {
        String command = "lldp receive";
        return after != null ? getCommandHelper(after.isLldpReceive(), command) : command;
    }

    private String getLldpTransmit(IfCiscoExtAug after) {
        String command = "lldp transmit";
        return after != null ? getCommandHelper(after.isLldpTransmit(), command) : command;
    }

    private String getCommandHelper(Boolean value, String command) {
        if (value != null) {
            if (!value) {
                return "no ".concat(command);
            }
        }
        return command;
    }

    private String getl2Protocols(Config before, Config after) {
        IfCiscoExtAug beforeAug = before.getAugmentation(IfCiscoExtAug.class);
        IfCiscoExtAug afterAug = after.getAugmentation(IfCiscoExtAug.class);

        StringBuilder str = new StringBuilder();
        // remove protocols
        str.append(getL2ProtocolsDiff(beforeAug, afterAug, true));
        // create protocols
        str.append(getL2ProtocolsDiff(afterAug, beforeAug, false));
        return str.toString().equals("") ? null : str.toString();
    }

    private String getL2ProtocolsDiff(IfCiscoExtAug first, IfCiscoExtAug second, boolean delete) {
        if (first != null && first.getL2Protocols() != null) {
            if (second != null && second.getL2Protocols() != null) {
                List<String> entries = first.getL2Protocols().stream()
                        .filter(entry -> !second.getL2Protocols().contains(entry))
                        .collect(Collectors.toList());
                if (entries.isEmpty()) {
                    return "";
                }
                return getL2ProtocolsStringString(entries, delete);
            }
            return getL2ProtocolsStringString(first.getL2Protocols(), delete);
        }
        return "";
    }

    private String getL2ProtocolsStringString(List<String> entries, boolean delete) {
        StringBuilder str = new StringBuilder();
        for (String entry : entries) {
            if (delete) {
                str.append("no ");
            }
            str.append("l2protocol-tunnel ").append(entry).append("\n");
        }
        return str.toString();
    }

    private String getSwitchportPortSecurityAgingStatic(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSwitchportPortSecurityAgingStatic() != null) {
                return ciscoExtAugAfter.isSwitchportPortSecurityAgingStatic()
                        ? "switchport port-security aging static" : "no switchport port-security aging static";
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSwitchportPortSecurityAgingStatic() != null) {
            return ciscoExtAugAfter.isSwitchportPortSecurityAgingStatic()
                    ? "switchport port-security aging static" : "no switchport port-security aging static";
        } else if (ciscoExtAugBefore.isSwitchportPortSecurityAgingStatic() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSwitchportPortSecurityAgingStatic() != null) {
                return ciscoExtAugAfter.isSwitchportPortSecurityAgingStatic()
                        ? "switchport port-security aging static" : "no switchport port-security aging static";
            } else {
                return "no switchport port-security aging static";
            }
        }
        return null;
    }

    private String getSwitchportPortSecurityAgingType(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityAgingType() != null) {
                return "switchport port-security aging type "
                        + ciscoExtAugAfter.getSwitchportPortSecurityAgingType().getName();
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityAgingType() != null) {
            return "switchport port-security aging type "
                    + ciscoExtAugAfter.getSwitchportPortSecurityAgingType().getName();
        } else if (ciscoExtAugBefore.getSwitchportPortSecurityAgingType() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityAgingType() != null) {
                return "switchport port-security aging type "
                        + ciscoExtAugAfter.getSwitchportPortSecurityAgingType().getName();
            } else {
                return "no switchport port-security aging type";
            }
        }
        return null;
    }

    private Object getSwitchportPortSecurityAgingTime(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityAgingTime() != null) {
                return "switchport port-security aging time " + ciscoExtAugAfter.getSwitchportPortSecurityAgingTime();
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityAgingTime() != null) {
            return "switchport port-security aging time " + ciscoExtAugAfter.getSwitchportPortSecurityAgingTime();
        } else if (ciscoExtAugBefore.getSwitchportPortSecurityAgingTime() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityAgingTime() != null) {
                return "switchport port-security aging time " + ciscoExtAugAfter.getSwitchportPortSecurityAgingTime();
            } else {
                return "no switchport port-security aging time";
            }
        }
        return null;
    }

    private Object getSwitchportPortSecurityViolation(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityViolation() != null) {
                return "switchport port-security violation "
                        + ciscoExtAugAfter.getSwitchportPortSecurityViolation().getName();
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityViolation() != null) {
            return "switchport port-security violation "
                    + ciscoExtAugAfter.getSwitchportPortSecurityViolation().getName();
        } else if (ciscoExtAugBefore.getSwitchportPortSecurityViolation() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityViolation() != null) {
                return "switchport port-security violation "
                        + ciscoExtAugAfter.getSwitchportPortSecurityViolation().getName();
            } else {
                return "no switchport port-security violation";
            }
        }
        return null;
    }

    private Object getPortSecurityMaximumCommand(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityMaximum() != null) {
                return "switchport port-security maximum " + ciscoExtAugAfter.getSwitchportPortSecurityMaximum();
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityMaximum() != null) {
            return "switchport port-security maximum " + ciscoExtAugAfter.getSwitchportPortSecurityMaximum();
        } else if (ciscoExtAugBefore.getSwitchportPortSecurityMaximum() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getSwitchportPortSecurityMaximum() != null) {
                return "switchport port-security maximum " + ciscoExtAugAfter.getSwitchportPortSecurityMaximum();
            } else {
                return "no switchport port-security maximum";
            }
        }
        return null;
    }

    private String getPortSecurityEnableCommand(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSwitchportPortSecurityEnable() != null) {
                return ciscoExtAugAfter.isSwitchportPortSecurityEnable()
                        ? "switchport port-security" : "no switchport port-security";
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSwitchportPortSecurityEnable() != null) {
            return ciscoExtAugAfter.isSwitchportPortSecurityEnable()
                    ? "switchport port-security" : "no switchport port-security";
        } else if (ciscoExtAugBefore.isSwitchportPortSecurityEnable() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.isSwitchportPortSecurityEnable() != null) {
                return ciscoExtAugAfter.isSwitchportPortSecurityEnable()
                        ? "switchport port-security" : "no switchport port-security";
            } else {
                return "no switchport port-security";
            }
        }
        return null;
    }

    private IfCiscoExtAug getIfCiscoExtAug(Config config) {
        if (config != null) {
            IfCiscoExtAug ciscoExtAug = config.getAugmentation(IfCiscoExtAug.class);
            if (ciscoExtAug != null) {
                return ciscoExtAug;
            }
        }
        return null;
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
