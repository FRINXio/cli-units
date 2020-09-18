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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfExtensionConfig.SwitchportMode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.policy.ServicePolicyBuilder;
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
    public static final Pattern SWITCHPORT_ACCESS_VLAN_LINE =
            Pattern.compile("\\s*switchport access vlan (?<switchportVlan>\\d+)");
    public static final Pattern SWITCHPORT_TRUNK_ALLOWED_VLAN_LINE =
            Pattern.compile("\\s*switchport trunk allowed vlan (?<allowedVlan>.+)");
    public static final Pattern SWITCHPORT_TRUNK_ALLOWED_VLAN_ADD_LINE =
            Pattern.compile("\\s*switchport trunk allowed vlan add (?<allowedVlan>.+)");
    public static final Pattern SERVICE_POLICY_INPUT_LINE = Pattern.compile("\\s*service-policy input (?<input>.+)");
    public static final Pattern SERVICE_POLICY_OUTPUT_LINE = Pattern.compile("\\s*service-policy output (?<output>.+)");
    public static final Pattern NO_IP_REDIRECTS_LINE = Pattern.compile("\\s*no ip redirects");
    public static final Pattern NO_IP_UNREACHABLES_LINE = Pattern.compile("\\s*no ip unreachables");
    public static final Pattern NO_IP_PROXY_ARP_LINE = Pattern.compile("\\s*no ip proxy-arp");

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
        setSwitchportAccessVlan(output, ifCiscoExtAugBuilder);
        setSwitchportTrunkAllowedVlanAdd(output, ifCiscoExtAugBuilder);
        setServicePolicy(output, ifCiscoExtAugBuilder);
        setIpRedirects(output, ifCiscoExtAugBuilder);
        setIpUnreachables(output, ifCiscoExtAugBuilder);
        setIpProxyArp(output, ifCiscoExtAugBuilder);
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

    private void setServicePolicy(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        ServicePolicyBuilder servicePolicyBuilder = new ServicePolicyBuilder();
        setServicePolicyInput(output, servicePolicyBuilder);
        setServicePolicyOutput(output, servicePolicyBuilder);

        if (hasServicePolicyBuilderData(servicePolicyBuilder)) {
            ifCiscoExtAugBuilder.setServicePolicy(servicePolicyBuilder.build());
        }
    }

    private void setServicePolicyOutput(String output, ServicePolicyBuilder servicePolicyBuilder) {
        Optional<String> outputValue = ParsingUtils.parseField(output, 0,
            SERVICE_POLICY_OUTPUT_LINE::matcher,
            matcher -> matcher.group("output"));

        if (outputValue.isPresent()) {
            servicePolicyBuilder.setOutput(outputValue.get());
        }
    }

    private void setServicePolicyInput(String output, ServicePolicyBuilder servicePolicyBuilder) {
        Optional<String> inputValue = ParsingUtils.parseField(output, 0,
            SERVICE_POLICY_INPUT_LINE::matcher,
            matcher -> matcher.group("input"));

        if (inputValue.isPresent()) {
            servicePolicyBuilder.setInput(inputValue.get());
        }
    }

    private boolean hasServicePolicyBuilderData(ServicePolicyBuilder servicePolicyBuilder) {
        return servicePolicyBuilder.getInput() != null || servicePolicyBuilder.getOutput() != null;
    }

    private void setSwitchportTrunkAllowedVlanAdd(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        Optional<String> allowedValues = ParsingUtils.parseField(output, 0,
            SWITCHPORT_TRUNK_ALLOWED_VLAN_LINE::matcher,
            matcher -> matcher.group("allowedVlan"));

        if (allowedValues.isPresent()) {
            List<Long> list = getSwitchportTrunkAllowedVlanList(allowedValues);
            ifCiscoExtAugBuilder.setSwitchportTrunkAllowedVlanAdd(list);
        }

        allowedValues = ParsingUtils.parseField(output, 0,
            SWITCHPORT_TRUNK_ALLOWED_VLAN_ADD_LINE::matcher,
            matcher -> matcher.group("allowedVlan"));

        if (allowedValues.isPresent()) {
            List<Long> list = getSwitchportTrunkAllowedVlanList(allowedValues);
            ifCiscoExtAugBuilder.setSwitchportTrunkAllowedVlanAdd(
                    Stream.concat(ifCiscoExtAugBuilder.getSwitchportTrunkAllowedVlanAdd().stream(), list.stream())
                    .collect(Collectors.toList()));
        }
    }

    private List<Long> getSwitchportTrunkAllowedVlanList(Optional<String> allowedValues) {
        ArrayList<String> vlanStrings = new ArrayList<>(Arrays.asList(allowedValues.get().split(",")));
        splitMultipleVlans(vlanStrings);
        return vlanStrings
                .stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private void splitMultipleVlans(ArrayList<String> vlanStrings) {
        for (int index = 0; index < vlanStrings.size(); index++) {
            if (vlanStrings.get(index).contains("-")) {
                List<Integer> list = Arrays.stream(vlanStrings.get(index).split("-"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                IntStream stream = IntStream.range(list.get(0), list.get(1) + 1);
                stream.forEach(i -> vlanStrings.add(String.valueOf(i)));
                vlanStrings.remove(vlanStrings.get(index));
            }
        }
    }

    private void setSwitchportAccessVlan(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        Optional<String> accessVlanValue = ParsingUtils.parseField(output, 0,
            SWITCHPORT_ACCESS_VLAN_LINE::matcher,
            matcher -> matcher.group("switchportVlan"));

        if (accessVlanValue.isPresent()) {
            long value = Long.parseLong(accessVlanValue.get());
            ifCiscoExtAugBuilder.setSwitchportAccessVlan(value);
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