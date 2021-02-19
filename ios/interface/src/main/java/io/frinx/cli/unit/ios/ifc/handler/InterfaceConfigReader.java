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

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigReader;
import io.frinx.cli.unit.ios.ifc.Util;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig.SwitchportMode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig.SwitchportPortSecurityAgingType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig.SwitchportPortSecurityViolation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class InterfaceConfigReader extends AbstractInterfaceConfigReader {

    public static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface %s";

    public static final Pattern SHUTDOWN_LINE = Pattern.compile("shutdown");
    private static final Pattern MTU_LINE = Pattern.compile("\\s*mtu (?<mtu>.+)$");
    public static final Pattern DESCR_LINE = Pattern.compile("\\s*description (?<desc>.+)");
    public static final Pattern PORT_TYPE_LINE = Pattern.compile("\\s*port-type (?<portType>.+)");
    public static final Pattern NO_SNMP_TRAP_LINE = Pattern.compile("\\s*no snmp trap link-status");
    public static final Pattern SWITCHPORT_MODE_LINE = Pattern.compile("\\s*switchport mode (?<mode>.+)");
    public static final Pattern NO_IP_REDIRECTS_LINE = Pattern.compile("\\s*no ip redirects");
    public static final Pattern NO_IP_UNREACHABLES_LINE = Pattern.compile("\\s*no ip unreachables");
    public static final Pattern NO_IP_PROXY_ARP_LINE = Pattern.compile("\\s*no ip proxy-arp");
    private static final Pattern STORM_CONTROL_LINE =
            Pattern.compile("\\s*storm-control (?<address>\\S+) level (?<level>.+)");
    private static final Pattern MEDIA_TYPE_LINE = Pattern.compile("\\s*media-type (?<mediaType>.+)");
    private static final Pattern SWITCHPORT_PORT_SECURITY = Pattern.compile("(?m)\\s*switchport port-security$");
    private static final Pattern SWITCHPORT_PORT_SECURITY_MAXIMUM =
            Pattern.compile("\\s*switchport port-security maximum (?<value>.+)");
    private static final Pattern SWITCHPORT_PORT_SECURITY_VIOLATION =
            Pattern.compile("\\s*switchport port-security violation (?<mode>.+)");
    private static final Pattern SWITCHPORT_PORT_SECURITY_AGING_TIME =
            Pattern.compile("\\s*switchport port-security aging time (?<value>.+)");
    private static final Pattern SWITCHPORT_PORT_SECURITY_AGING_TYPE =
            Pattern.compile("\\s*switchport port-security aging type (?<type>.+)");
    private static final Pattern SWITCHPORT_PORT_SECURITY_AGING_STATIC =
            Pattern.compile("\\s*switchport port-security aging static");
    private static final Pattern L2_PROTOCOL_TUNNEL_LINE = Pattern.compile("\\s*l2protocol-tunnel (?<entry>.+)");
    private static final Pattern LLDP_TRANSMIT_LINE = Pattern.compile("(?m)\\s*no lldp transmit$");
    private static final Pattern LLDP_RECEIVE_LINE = Pattern.compile("(?m)\\s*no lldp receive$");
    private static final Pattern CDP_ENABLE_LINE = Pattern.compile("(?m)\\s*no cdp enable$");

    public InterfaceConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    public void parseInterface(String output, ConfigBuilder builder, String name) {
        super.parseInterface(output, builder, name);

        IfCiscoExtAugBuilder ifCiscoExtAugBuilder = new IfCiscoExtAugBuilder();
        setPortType(output, ifCiscoExtAugBuilder);
        setSnmpTrap(output, ifCiscoExtAugBuilder);
        setSwitchportMode(output, ifCiscoExtAugBuilder);
        setSwitchportPortSecurity(output, ifCiscoExtAugBuilder);
        setSwitchportPortSecurityMaximum(output, ifCiscoExtAugBuilder);
        setSwitchportPortSecurityViolation(output, ifCiscoExtAugBuilder);
        setSwitchportPortSecurityAgingTime(output, ifCiscoExtAugBuilder);
        setSwitchportPortSecurityAgingType(output, ifCiscoExtAugBuilder);
        setSwitchportPortSecurityAgingStatic(output, ifCiscoExtAugBuilder);
        setIpRedirects(output, ifCiscoExtAugBuilder);
        setIpUnreachables(output, ifCiscoExtAugBuilder);
        setIpProxyArp(output, ifCiscoExtAugBuilder);
        setStormControl(output, ifCiscoExtAugBuilder);
        setL2ProtocolTunnel(output, ifCiscoExtAugBuilder);
        setLldpTransmit(output, ifCiscoExtAugBuilder);
        setLldpReceive(output, ifCiscoExtAugBuilder);
        setCdpEnable(output, ifCiscoExtAugBuilder);
        if (isCiscoExtAugNotEmpty(ifCiscoExtAugBuilder)) {
            builder.addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build());
        }

        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        setMode(output, ifSaosAugBuilder);
        if (isSaosAugNotEmpty(ifSaosAugBuilder)) {
            builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        }
    }

    private void setCdpEnable(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (CDP_ENABLE_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setCdpEnable(false);
        }
    }

    private void setLldpReceive(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (LLDP_RECEIVE_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setLldpReceive(false);
        }
    }

    private void setLldpTransmit(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (LLDP_TRANSMIT_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setLldpTransmit(false);
        }
    }

    private void setL2ProtocolTunnel(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        List<String> entries = ParsingUtils.parseFields(output, 0,
            L2_PROTOCOL_TUNNEL_LINE::matcher,
            matcher -> matcher.group("entry"),
            String::new);

        if (!entries.isEmpty()) {
            ifCiscoExtAugBuilder.setL2Protocols(entries);
        }
    }

    private void setSwitchportPortSecurityAgingStatic(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (SWITCHPORT_PORT_SECURITY_AGING_STATIC.matcher(output).find()) {
            ifCiscoExtAugBuilder.setSwitchportPortSecurityAgingStatic(true);
        }
    }

    private void setSwitchportPortSecurityAgingType(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        Optional<String> mode = ParsingUtils.parseField(output, 0,
            SWITCHPORT_PORT_SECURITY_AGING_TYPE::matcher,
            matcher -> matcher.group("type"));

        if (mode.isPresent()) {
            SwitchportPortSecurityAgingType type;
            switch (mode.get()) {
                case "absolute":
                    type = SwitchportPortSecurityAgingType.Absolute;
                    break;
                case "inactivity":
                    type = SwitchportPortSecurityAgingType.Inactivity;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse switchport port-security aging type value: "
                            + mode.get());
            }
            ifCiscoExtAugBuilder.setSwitchportPortSecurityAgingType(type);
        }
    }

    private void setSwitchportPortSecurityAgingTime(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        ParsingUtils.parseField(output,
            SWITCHPORT_PORT_SECURITY_AGING_TIME::matcher,
            matcher -> Long.valueOf(matcher.group("value")),
            ifCiscoExtAugBuilder::setSwitchportPortSecurityAgingTime);
    }

    private void setSwitchportPortSecurityViolation(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        Optional<String> mode = ParsingUtils.parseField(output, 0,
            SWITCHPORT_PORT_SECURITY_VIOLATION::matcher,
            matcher -> matcher.group("mode"));

        if (mode.isPresent()) {
            SwitchportPortSecurityViolation violation;
            switch (mode.get()) {
                case "protect":
                    violation = SwitchportPortSecurityViolation.Protect;
                    break;
                case "restrict":
                    violation = SwitchportPortSecurityViolation.Restrict;
                    break;
                case "shutdown":
                    violation = SwitchportPortSecurityViolation.Shutdown;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse switchport port-security violation mode value: "
                            + mode.get());
            }
            ifCiscoExtAugBuilder.setSwitchportPortSecurityViolation(violation);
        }
    }

    private void setSwitchportPortSecurityMaximum(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        ParsingUtils.parseField(output,
            SWITCHPORT_PORT_SECURITY_MAXIMUM::matcher,
            matcher -> Long.valueOf(matcher.group("value")),
            ifCiscoExtAugBuilder::setSwitchportPortSecurityMaximum);
    }

    private void setSwitchportPortSecurity(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (SWITCHPORT_PORT_SECURITY.matcher(output).find()) {
            ifCiscoExtAugBuilder.setSwitchportPortSecurityEnable(true);
        }
    }

    private boolean isCiscoExtAugNotEmpty(IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        return !ifCiscoExtAugBuilder.build().equals((new IfCiscoExtAugBuilder()).build());
    }

    private Boolean isSaosAugNotEmpty(IfSaosAugBuilder ifSaosAugBuilder) {
        return !ifSaosAugBuilder.build().equals((new IfSaosAugBuilder()).build());
    }

    private void setMode(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        Optional<String> mode = ParsingUtils.parseField(output, 0,
            MEDIA_TYPE_LINE::matcher,
            matcher -> matcher.group("mediaType"));

        if (mode.isPresent()) {
            PhysicalType physicalType;
            switch (mode.get()) {
                case "auto-select":
                    physicalType = PhysicalType.Default;
                    break;
                case "rj45":
                    physicalType = PhysicalType.Rj45;
                    break;
                case "sfp":
                    physicalType = PhysicalType.Sfp;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Mode value: " + mode.get());
            }
            ifSaosAugBuilder.setPhysicalType(physicalType);
        }
    }

    private void setSwitchportMode(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        Optional<String> modeVolue = ParsingUtils.parseField(output, 0,
            SWITCHPORT_MODE_LINE::matcher,
            matcher -> matcher.group("mode"));

        if (modeVolue.isPresent()) {
            SwitchportMode switchportMode;
            switch (modeVolue.get()) {
                case "trunk":
                    switchportMode = SwitchportMode.Trunk;
                    break;
                case "access":
                    switchportMode = SwitchportMode.Access;
                    break;
                case "dot1q-tunnel":
                    switchportMode = SwitchportMode.Dot1qTunnel;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse switchport mode value: " + modeVolue.get());
            }
            ifCiscoExtAugBuilder.setSwitchportMode(switchportMode);
        }
    }

    private void setIpProxyArp(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_IP_PROXY_ARP_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setIpProxyArp(false);
        }
    }

    private void setIpUnreachables(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_IP_REDIRECTS_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setIpRedirects(false);
        }
    }

    private void setIpRedirects(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_IP_UNREACHABLES_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setIpUnreachables(false);
        }
    }

    private void setSnmpTrap(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_SNMP_TRAP_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setSnmpTrapLinkStatus(false);
        }
    }

    private void setPortType(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        Optional<String> portTypeValue = ParsingUtils.parseField(output, 0,
            PORT_TYPE_LINE::matcher,
            matcher -> matcher.group("portType"));

        if (portTypeValue.isPresent()) {
            CiscoIfExtensionConfig.PortType portType;
            switch (portTypeValue.get()) {
                case "eni":
                    portType = CiscoIfExtensionConfig.PortType.Eni;
                    break;
                case "nni":
                    portType = CiscoIfExtensionConfig.PortType.Nni;
                    break;
                case "uni":
                    portType = CiscoIfExtensionConfig.PortType.Uni;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse port-type value: " + portTypeValue.get());
            }
            ifCiscoExtAugBuilder.setPortType(portType);
        }
    }

    private void setStormControl(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        List<StormControl> stormControls = new ArrayList<>();
        ParsingUtils.parseFields(output, 0, STORM_CONTROL_LINE::matcher,
            Matcher::group,
            m -> stormControls.add(parseStormControlLine(m)));

        if (!stormControls.isEmpty()) {
            ifCiscoExtAugBuilder.setStormControl(stormControls);
        }
    }

    private StormControl parseStormControlLine(String line) {
        Optional<String> address = ParsingUtils.parseField(line, 0, STORM_CONTROL_LINE::matcher,
            matcher -> matcher.group("address"));
        Optional<String> level = ParsingUtils.parseField(line, 0, STORM_CONTROL_LINE::matcher,
            matcher -> matcher.group("level"));

        if (address.isPresent() && level.isPresent()) {
            StormControlBuilder builder = new StormControlBuilder();
            builder.setAddress(Util.getStormControlAddress(address.get()));
            builder.setKey(new StormControlKey(builder.getAddress()));
            builder.setLevel(new BigDecimal(level.get()));
            return builder.build();
        }

        return null;
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_SINGLE_INTERFACE_CFG, ifcName);
    }

    @Override
    protected Pattern getShutdownLine() {
        return SHUTDOWN_LINE;
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCR_LINE;
    }

    @Override
    public Class<? extends InterfaceType> parseType(String name) {
        return Util.parseType(name);
    }
}
