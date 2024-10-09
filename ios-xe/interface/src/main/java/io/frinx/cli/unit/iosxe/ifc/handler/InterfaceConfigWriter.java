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

package io.frinx.cli.unit.iosxe.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.Util;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$data.name}
            {% if ($mtu) %}{$mtu}
            {% endif %}{% if ($description) %}{$description}
            {% endif %}{% if ($enabled) %}{$enabled}
            {% endif %}{% if ($ipRedirects) %}{$ipRedirects}
            {% endif %}{% if ($pt) %}{$pt}{% endif %}{% if ($snmpTrap) %}{$snmpTrap}
            {% endif %}{% if ($stormControl) %}{$stormControl}{% endif %}{% if ($lldpTransmit) %}{$lldpTransmit}{% endif %}{% if ($lldpReceive) %}{$lldpReceive}{% endif %}{% if ($negotiationAuto) %}{$negotiationAuto}{% endif %}{% if ($fhrpMinimum) %}{$fhrpMinimum}{% endif %}{% if ($fhrpReload) %}{$fhrpReload}{% endif %}{% if ($holdQueueIn) %}{$holdQueueIn}
            {% endif %}{% if ($holdQueueOut) %}{$holdQueueOut}
            {% endif %}end""";

    private static final String WRITE_TEMPLATE_VLAN = """
            configure terminal
            interface {$data.name}
            {% if ($mtu) %}{$mtu}
            {% endif %}{% if ($description) %}{$description}
            {% endif %}{% if ($enabled) %}{$enabled}
            {% endif %}{% if ($snmpTrap) %}{$snmpTrap}
            {% endif %}{% if ($ipProxyArp) %}{$ipProxyArp}
            {% endif %}{% if ($ipv6NdRaSuppress) %}{$ipv6NdRaSuppress}
            {% endif %}{% if ($ipRedirects) %}{$ipRedirects}
            {% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            no interface {$data.name}
            end""";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        final IfCiscoExtAug ciscoExtAug = getIfCiscoExtAug(after);
        final IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        if (isPhysicalInterface(after)) {
            return fT(WRITE_TEMPLATE,
                    "before", before,
                    "data", after,
                    "mtu", getMtu(before, after),
                    "description", getDescription(before, after),
                    "enabled", getEnabled(before, after),
                    "pt", getPhysicalType(before, after),
                    "snmpTrap", getSnmp(ciscoExtAugBefore, ciscoExtAug),
                    "stormControl", getStormControlCommands(ciscoExtAugBefore, ciscoExtAug),
                    "lldpTransmit", getLldpTransmit(ciscoExtAugBefore, ciscoExtAug),
                    "lldpReceive", getLldpReceive(ciscoExtAugBefore, ciscoExtAug),
                    "negotiationAuto", getNegotiationAuto(ciscoExtAugBefore, ciscoExtAug),
                    "fhrpMinimum", getFhrpMinimumDelay(ciscoExtAugBefore, ciscoExtAug),
                    "fhrpReload", getFhrpReloadDelay(ciscoExtAugBefore, ciscoExtAug),
                    "ipRedirects", getIpRedirect(ciscoExtAugBefore, ciscoExtAug),
                    "holdQueueIn", getHoldQueueIn(ciscoExtAugBefore, ciscoExtAug),
                    "holdQueueOut", getHoldQueueOut(ciscoExtAugBefore, ciscoExtAug));
        }
        return fT(WRITE_TEMPLATE_VLAN,
                "before", before,
                "data", after,
                "ipRedirects", getIpRedirect(ciscoExtAugBefore, ciscoExtAug),
                "ipProxyArp", getIpProxyArp(ciscoExtAugBefore, ciscoExtAug),
                "snmpTrap", getSnmp(ciscoExtAugBefore, ciscoExtAug),
                "ipv6NdRaSuppress", getIpv6NdRaSuppress(ciscoExtAugBefore, ciscoExtAug),
                "enabled", getEnabled(before, after),
                "mtu", getMtu(before, after),
                "description", getDescription(before, after));
    }

    private String getMtu(Config dataBefore, Config dataAfter) {
        Integer beforeMtu = null;
        if (dataBefore != null && dataBefore.getMtu() != null) {
            beforeMtu = dataBefore.getMtu();
        }

        Integer afterMtu = null;
        if (dataAfter != null && dataAfter.getMtu() != null) {
            afterMtu = dataAfter.getMtu();
        }

        if (!Objects.equals(beforeMtu, afterMtu)) {
            if (afterMtu != null) {
                return "mtu " + afterMtu;
            } else {
                return "no mtu";
            }
        }
        return null;
    }

    private String getDescription(Config dataBefore, Config dataAfter) {
        String beforeDescription = null;
        if (dataBefore != null && dataBefore.getDescription() != null) {
            beforeDescription = dataBefore.getDescription();
        }

        String afterDescription = null;
        if (dataAfter != null && dataAfter.getDescription() != null) {
            afterDescription = dataAfter.getDescription();
        }

        if (!Objects.equals(beforeDescription, afterDescription)) {
            if (afterDescription != null) {
                return "description " + afterDescription;
            } else {
                return "no description";
            }
        }
        return null;
    }

    private String getEnabled(Config dataBefore, Config dataAfter) {
        Boolean beforeEnabled = null;
        Boolean afterEnabled = null;
        if (dataBefore != null && dataBefore.isEnabled() != null) {
            beforeEnabled = dataBefore.isEnabled();
        }
        if (dataAfter != null && dataAfter.isEnabled() != null) {
            afterEnabled = dataAfter.isEnabled();
        }

        if (!Objects.equals(beforeEnabled, afterEnabled)) {
            if (afterEnabled != null) {
                return afterEnabled ? "no shutdown" : "shutdown";
            } else {
                return "shutdown";
            }
        }

        if (beforeEnabled == null && afterEnabled == null) {
            return "shutdown";
        }
        return null;
    }

    private IfCiscoExtAug getIfCiscoExtAug(final Config config) {
        if (config != null) {
            return config.getAugmentation(IfCiscoExtAug.class);
        }
        return null;
    }

    private String getCommandHelper(final Boolean before, final Boolean after, final String command) {
        if (!before && after) {
            return command;
        } else if (before && !after) {
            return "no " + command;
        }
        return null;
    }

    private String getLldpTransmit(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeLldpTransmit = true;
        Boolean afterLldpTransmit = true;
        if (before != null && before.isLldpTransmit() != null) {
            beforeLldpTransmit = before.isLldpTransmit();
        }
        if (after != null && after.isLldpTransmit() != null) {
            afterLldpTransmit = after.isLldpTransmit();
        }
        return getCommandHelper(beforeLldpTransmit, afterLldpTransmit, "lldp transmit\n");
    }

    private String getLldpReceive(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeLldpReceive = true;
        Boolean afterLldpReceive = true;
        if (before != null && before.isLldpReceive() != null) {
            beforeLldpReceive = before.isLldpReceive();
        }
        if (after != null && after.isLldpReceive() != null) {
            afterLldpReceive = after.isLldpReceive();
        }
        return getCommandHelper(beforeLldpReceive, afterLldpReceive, "lldp receive\n");
    }

    private String getNegotiationAuto(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeNegotiationAuto;
        Boolean afterNegotiationAuto;
        if (before != null && before.isNegotiationAuto() != null) {
            beforeNegotiationAuto = before.isNegotiationAuto();
        } else {
            beforeNegotiationAuto = false;
        }
        if (after != null && after.isNegotiationAuto() != null) {
            afterNegotiationAuto = after.isNegotiationAuto();
        } else {
            afterNegotiationAuto = false;
        }
        return getCommandHelper(beforeNegotiationAuto, afterNegotiationAuto, "negotiation auto\n");
    }

    private String getPhysicalType(final Config dataBefore, final Config dataAfter) {
        final String typeBefore = getPhysicalType(dataBefore);
        final String typeAfter = getPhysicalType(dataAfter);
        if (!Objects.equals(typeAfter, typeBefore)) {
            if (typeAfter != null) {
                return "media-type " + typeAfter + "\n";
            }
            return "no media-type\n";
        }
        return null;
    }

    private String getPhysicalType(final Config config) {
        if (config != null) {
            final IfSaosAug saosAug = config.getAugmentation(IfSaosAug.class);
            if (saosAug != null) {
                final PhysicalType physicalType = saosAug.getPhysicalType();
                return physicalType != null ? physicalType.getName() : null;
            }
        }
        return null;
    }

    private String getSnmp(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeSnmp = null;
        Boolean afterSnmp = null;
        if (before != null && before.isSnmpTrapLinkStatus() != null) {
            beforeSnmp = before.isSnmpTrapLinkStatus();
        }
        if (after != null && after.isSnmpTrapLinkStatus() != null) {
            afterSnmp = after.isSnmpTrapLinkStatus();
        }

        if (!Objects.equals(beforeSnmp, afterSnmp)) {
            if (afterSnmp != null) {
                return afterSnmp ? "snmp trap link-status" : "no snmp trap link-status";
            } else {
                return "snmp trap link-status";
            }
        }
        return null;
    }

    private String getStormControlCommands(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        final StringBuilder currentControls = new StringBuilder();

        if (after != null) {
            if (after.getStormControl() != null) {
                // create commands for required storm controls
                for (final StormControl stormControl : after.getStormControl()) {
                    currentControls.append("storm-control ").append(stormControl.getAddress().getName());
                    currentControls.append(" level ").append(stormControl.getLevel().toString()).append("\n");
                }
            }
        }

        if (before != null) {
            // create no-commands for remaining storm controls only if they were there before
            if (before.getStormControl() != null) {
                for (final StormControl.Address address : StormControl.Address.values()) {
                    for (final StormControl stormControl : before.getStormControl()) {
                        if (!currentControls.toString().contains(address.getName())
                                && stormControl.getAddress().getName().equals(address.getName())) {
                            currentControls.append("no storm-control ").append(address.getName()).append(" level\n");
                        }
                    }
                }
            }
        }

        return currentControls.toString();
    }

    private String getFhrpMinimumDelay(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Integer beforeMinimumDelay = null;
        if (before != null) {
            beforeMinimumDelay = before.getFhrpMinimumDelay();
        }

        Integer afterMinimumDelay = null;
        if (after != null) {
            afterMinimumDelay = after.getFhrpMinimumDelay();
        }

        return getFhrp(beforeMinimumDelay, afterMinimumDelay, "fhrp delay minimum");
    }

    private String getFhrpReloadDelay(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Integer beforeReloadDelay = null;
        if (before != null) {
            beforeReloadDelay = before.getFhrpReloadDelay();
        }

        Integer afterReloadDelay = null;
        if (after != null) {
            afterReloadDelay = after.getFhrpReloadDelay();
        }

        return getFhrp(beforeReloadDelay, afterReloadDelay, "fhrp delay reload");
    }

    private String getFhrp(final Integer beforeValue, final Integer afterValue, final String command) {
        if (!Objects.equals(beforeValue, afterValue)) {
            if (afterValue != null) {
                return command + " " + afterValue + "\n";
            } else {
                return "no " + command + "\n";
            }
        }
        return null;
    }

    private String getIpRedirect(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeIpRedirect = null;
        Boolean afterIpRedirect = null;
        if (before != null && before.isIpRedirects() != null) {
            beforeIpRedirect = before.isIpRedirects();
        }
        if (after != null && after.isIpRedirects() != null) {
            afterIpRedirect = after.isIpRedirects();
        }

        if (!Objects.equals(beforeIpRedirect, afterIpRedirect)) {
            if (afterIpRedirect != null) {
                return afterIpRedirect ? "ip redirects" : "no ip redirects";
            } else {
                return beforeIpRedirect ? "ip redirects" : "no ip redirects";
            }
        }
        return null;
    }

    private String getIpProxyArp(final IfCiscoExtAug before, IfCiscoExtAug after) {
        Boolean beforeIpProxyArp = null;
        Boolean afterIpProxyArp = null;
        if (before != null && before.isIpProxyArp() != null) {
            beforeIpProxyArp = before.isIpProxyArp();
        }
        if (after != null && after.isIpProxyArp() != null) {
            afterIpProxyArp = after.isIpProxyArp();
        }

        if (!Objects.equals(beforeIpProxyArp, afterIpProxyArp)) {
            if (afterIpProxyArp != null) {
                return afterIpProxyArp ? "ip proxy-arp" : "no ip proxy-arp";
            } else {
                return "no ip proxy-arp";
            }
        }
        return null;
    }

    private String getIpv6NdRaSuppress(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        String beforeIpv6NdRaSuppress = null;
        if (before != null && before.getIpv6NdRaSuppress() != null) {
            beforeIpv6NdRaSuppress = before.getIpv6NdRaSuppress();
        }

        String afterIpv6NdRaSuppress = null;
        if (after != null && after.getIpv6NdRaSuppress() != null) {
            afterIpv6NdRaSuppress = after.getIpv6NdRaSuppress();
        }

        if (!Objects.equals(beforeIpv6NdRaSuppress, afterIpv6NdRaSuppress)) {
            if (afterIpv6NdRaSuppress != null) {
                return "ipv6 nd ra suppress " + afterIpv6NdRaSuppress;
            } else {
                return "no ipv6 nd ra suppress all";
            }
        }
        return null;
    }

    private String getHoldQueueIn(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Long beforeHoldQueueIn = null;
        if (before != null && before.getHoldQueue() != null && before.getHoldQueue().getIn() != null) {
            beforeHoldQueueIn = before.getHoldQueue().getIn();
        }

        Long afterHoldQueueIn = null;
        if (after != null && after.getHoldQueue() != null && after.getHoldQueue().getOut() != null) {
            afterHoldQueueIn = after.getHoldQueue().getIn();
        }

        return getHoldQueue(beforeHoldQueueIn, afterHoldQueueIn, true);
    }

    private String getHoldQueueOut(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Long beforeHoldQueueOut = null;
        if (before != null && before.getHoldQueue() != null && before.getHoldQueue().getOut() != null) {
            beforeHoldQueueOut = before.getHoldQueue().getOut();
        }

        Long afterHoldQueueOut = null;
        if (after != null && after.getHoldQueue() != null && after.getHoldQueue() != null) {
            afterHoldQueueOut = after.getHoldQueue().getOut();
        }

        return getHoldQueue(beforeHoldQueueOut, afterHoldQueueOut, false);
    }

    private String getHoldQueue(final Long beforeValue, final Long afterValue, final boolean in) {
        if (!Objects.equals(beforeValue, afterValue)) {
            if (afterValue != null) {
                return in ? "hold-queue " + afterValue + " in" : "hold-queue " + afterValue + " out";
            } else {
                return in ? "no hold-queue in" : "no hold-queue out";
            }
        }
        return null;
    }

    @Override
    protected boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data.getType());
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }

}
