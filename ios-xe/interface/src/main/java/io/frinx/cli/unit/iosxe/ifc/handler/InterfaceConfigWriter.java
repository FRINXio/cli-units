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

import com.x5.template.Chunk;
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

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($data.description) %}description {$data.description}\n{% else %}no description\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($ipRedirects) %}{$ipRedirects}\n{% endif %}"
            + "{% if ($pt) %}{$pt}{% endif %}"
            + "{% if ($snmpTrap) %}no snmp trap link-status\n{% else %}snmp trap link-status\n{% endif %}"
            + "{% if ($stormControl) %}{$stormControl}{% endif %}"
            + "{% if ($lldpTransmit) %}{$lldpTransmit}{% endif %}"
            + "{% if ($lldpReceive) %}{$lldpReceive}{% endif %}"
            + "{% if ($negotiationAuto) %}{$negotiationAuto}{% endif %}"
            + "{% if ($fhrpMinimum) %}{$fhrpMinimum}{% endif %}"
            + "{% if ($fhrpReload) %}{$fhrpReload}{% endif %}"
            + "{% if ($holdQueueIn) %}hold-queue {$holdQueueIn} in\n{% else %}no hold-queue in\n{% endif %}"
            + "{% if ($holdQueueOut) %}hold-queue {$holdQueueOut} out\n{% else %}no hold-queue out\n{% endif %}"
            + "end";

    private static final String WRITE_TEMPLATE_VLAN = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($data.description) %}description {$data.description}\n{% else %}no description\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($snmpTrap) %}no snmp trap link-status\n{% else %}snmp trap link-status\n{% endif %}"
            + "{% if ($ipProxyArp) %}{$ipProxyArp}\n{% endif %}"
            + "{% if ($ipv6NdRaSuppress) %}{$ipv6NdRaSuppress}\n{% endif %}"
            + "{% if ($ipRedirects) %}{$ipRedirects}\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no interface {$data.name}\n"
            + "end";

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
                    "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
                    "pt", getPhysicalType(before, after),
                    "snmpTrap", (ciscoExtAug != null && ciscoExtAug.isSnmpTrapLinkStatus() != null
                            && !ciscoExtAug.isSnmpTrapLinkStatus()) ? Chunk.TRUE : null,
                    "stormControl", getStormControlCommands(ciscoExtAugBefore, ciscoExtAug),
                    "lldpTransmit", getLldpTransmit(ciscoExtAugBefore, ciscoExtAug),
                    "lldpReceive", getLldpReceive(ciscoExtAugBefore, ciscoExtAug),
                    "negotiationAuto", getNegotiationAuto(ciscoExtAugBefore, ciscoExtAug),
                    "fhrpMinimum", getFhrpMinimumDelay(ciscoExtAugBefore, ciscoExtAug),
                    "fhrpReload", getFhrpReloadDelay(ciscoExtAugBefore, ciscoExtAug),
                    "ipRedirects", getIpRedirect(before, after),
                    "holdQueueIn", getHoldQueueIn(after),
                    "holdQueueOut", getHoldQueueOut(after));
        }
        return fT(WRITE_TEMPLATE_VLAN,
                "before", before,
                "data", after,
                "ipRedirects", getIpRedirect(before, after),
                "ipProxyArp", getIpProxyArp(before, after),
                "snmpTrap", (ciscoExtAug != null && ciscoExtAug.isSnmpTrapLinkStatus() != null
                        && !ciscoExtAug.isSnmpTrapLinkStatus()) ? Chunk.TRUE : null,
                "ipv6NdRaSuppress", getIpv6NdRaSuppress(before, after),
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
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
        Boolean beforeNegotiationAuto = true;
        Boolean afterNegotiationAuto = true;
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

    private String getIpRedirect(final Config before, final Config after) {
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
                return ciscoExtAugBefore.isIpRedirects() ? "ip redirects" : "no ip redirects";
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

    private String getIpv6NdRaSuppress(Config before, Config after) {
        IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        IfCiscoExtAug ciscoExtAugAfter = getIfCiscoExtAug(after);

        if (ciscoExtAugBefore == null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getIpv6NdRaSuppress() != null) {
                return "ipv6 nd ra suppress " + ciscoExtAugAfter.getIpv6NdRaSuppress();
            }
        } else if (ciscoExtAugAfter != null && ciscoExtAugAfter.getIpv6NdRaSuppress() != null) {
            return "ipv6 nd ra suppress " + ciscoExtAugAfter.getIpv6NdRaSuppress();
        } else if (ciscoExtAugBefore.getIpv6NdRaSuppress() != null) {
            if (ciscoExtAugAfter != null && ciscoExtAugAfter.getIpv6NdRaSuppress() != null) {
                return "ipv6 nd ra suppress " + ciscoExtAugAfter.getIpv6NdRaSuppress();
            } else {
                return "no ipv6 nd ra suppress all";
            }
        }
        return null;
    }

    private String getHoldQueueIn(Config config) {
        IfCiscoExtAug ciscoExtAug = getIfCiscoExtAug(config);
        if (ciscoExtAug != null) {
            if (ciscoExtAug.getHoldQueue() != null && ciscoExtAug.getHoldQueue().getIn() != null) {
                return String.valueOf(ciscoExtAug.getHoldQueue().getIn());
            }
        }
        return null;
    }

    private String getHoldQueueOut(Config config) {
        IfCiscoExtAug ciscoExtAug = getIfCiscoExtAug(config);
        if (ciscoExtAug != null) {
            if (ciscoExtAug.getHoldQueue() != null && ciscoExtAug.getHoldQueue().getOut() != null) {
                return String.valueOf(ciscoExtAug.getHoldQueue().getOut());
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
