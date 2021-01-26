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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
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
        setIpRedirects(output, ifCiscoExtAugBuilder);
        setIpUnreachables(output, ifCiscoExtAugBuilder);
        setIpProxyArp(output, ifCiscoExtAugBuilder);
        setStormControl(output, ifCiscoExtAugBuilder);
        builder.addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build());
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
