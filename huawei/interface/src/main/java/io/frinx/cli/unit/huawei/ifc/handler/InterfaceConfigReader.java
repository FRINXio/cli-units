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

package io.frinx.cli.unit.huawei.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.HuaweiIfExtensionConfig.Trust;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.TrafficDirection.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei._if.extension.config.TrafficFilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei._if.extension.config.TrafficPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.RadioMAC;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class InterfaceConfigReader extends AbstractInterfaceConfigReader {

    public static final String SH_SINGLE_INTERFACE_CFG = "display current-configuration interface %s";
    public static final Pattern SHUTDOWN_LINE = Pattern.compile("shutdown");
    private static final Pattern MTU_LINE = Pattern.compile("\\s*mtu (?<mtu>.+)\\s*");
    public static final Pattern DESCR_LINE = Pattern.compile("\\s*description (?<desc>.+)\\s*");
    private static final Pattern FLOW_STAT_INTERVAL_LINE =
            Pattern.compile("\\s*set flow-stat interval (?<interval>.+)\\s*");
    private static final Pattern TRAFFIC_FILTER_LINE =
            Pattern.compile("traffic-filter (?<direction>\\S+) (?<ipv6>ipv6\\s)?acl name (?<aclName>.+)");
    private static final Pattern TRAFFIC_POLICY_LINE =
            Pattern.compile("traffic-policy (?<policyName>.+) (?<direction>.+)");
    private static final Pattern VPN_INSTANCE_LINE = Pattern.compile("ip binding vpn-instance (?<aclName>.+)");
    private static final Pattern RADIO_LINE = Pattern.compile("\\sundo radio enable");
    private static final Pattern LLDP_LINE = Pattern.compile("undo lldp enable");
    private static final Pattern ARP_EXPIRE_TIME_LINE = Pattern.compile("arp expire-time (?<arpExpTime>.*)");
    private static final Pattern TRUST_LINE = Pattern.compile("trust (?<trust>.+)");

    public InterfaceConfigReader(Cli cli) {
        super(cli);
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

    @Override
    public void parseInterface(String output, ConfigBuilder builder, String name) {
        super.parseInterface(output, builder, name);

        final IfHuaweiAugBuilder ifHuaweiAugBuilder = new IfHuaweiAugBuilder();
        setFlowStatInterval(output, ifHuaweiAugBuilder);
        setTrust(output, ifHuaweiAugBuilder);
        setTrafficFilter(output, ifHuaweiAugBuilder);
        setTrafficPolicy(output, ifHuaweiAugBuilder);
        setVpnInstance(output, ifHuaweiAugBuilder);
        setArpExpireTime(output, ifHuaweiAugBuilder);
        setLldp(output, ifHuaweiAugBuilder);
        if (builder.getType() == RadioMAC.class) {
            setRadio(output, ifHuaweiAugBuilder);
        }
        if (isHuaweiExtAugNotEmpty(ifHuaweiAugBuilder)) {
            builder.addAugmentation(IfHuaweiAug.class, ifHuaweiAugBuilder.build());
        }
    }

    private void setVpnInstance(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        ParsingUtils.parseField(output, 0,
            VPN_INSTANCE_LINE::matcher,
            matcher -> matcher.group("aclName"),
            ifHuaweiAugBuilder::setIpBindingVpnInstance);
    }

    private void setTrust(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        ParsingUtils.parseField(output, 0,
            TRUST_LINE::matcher,
            matcher -> matcher.group("trust"),
            trust -> ifHuaweiAugBuilder.setTrust(getTrust(trust)));
    }

    private Trust getTrust(String value) {
        switch (value) {
            case "dscp":
                return Trust.Dscp;
            case "8021p inner":
                return Trust._8021pInner;
            case "8021p outer":
                return Trust._8021pOuter;
            default:
                throw new IllegalArgumentException("Cannot parse trust value: " + value);
        }
    }

    private void setRadio(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        ParsingUtils.parseField(output, 0,
            RADIO_LINE::matcher,
            matcher -> false,
            ifHuaweiAugBuilder::setRadioEnabled);
    }

    private void setLldp(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        ifHuaweiAugBuilder.setLldpEnabled(true);

        ParsingUtils.parseField(output, 0,
                LLDP_LINE::matcher,
                matcher -> false,
                ifHuaweiAugBuilder::setLldpEnabled);
    }

    private void setTrafficFilter(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        final Optional<String> direction = ParsingUtils.parseField(output, 0,
            TRAFFIC_FILTER_LINE::matcher,
            matcher -> matcher.group("direction"));

        final Optional<String> aclName = ParsingUtils.parseField(output, 0,
            TRAFFIC_FILTER_LINE::matcher,
            matcher -> matcher.group("aclName"));

        final boolean isIpv6 = ParsingUtils.parseField(output, 0,
            TRAFFIC_FILTER_LINE::matcher,
            matcher -> matcher.group("ipv6")).isPresent();

        TrafficFilterBuilder trafficFilterBuilder = new TrafficFilterBuilder();
        direction.ifPresent(value -> trafficFilterBuilder.setDirection(getDirection(value, "filter")));
        aclName.ifPresent(trafficFilterBuilder::setAclName);

        if (trafficFilterBuilder.getAclName() != null && trafficFilterBuilder.getDirection() != null) {
            trafficFilterBuilder.setIpv6(isIpv6);
            ifHuaweiAugBuilder.setTrafficFilter(trafficFilterBuilder.build());
        }
    }

    private void setTrafficPolicy(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        TrafficPolicyBuilder trafficPolicyBuilder = new TrafficPolicyBuilder();

        final Optional<String> direction = ParsingUtils.parseField(output, 0,
            TRAFFIC_POLICY_LINE::matcher,
            matcher -> matcher.group("direction"));

        final Optional<String> policyName = ParsingUtils.parseField(output, 0,
            TRAFFIC_POLICY_LINE::matcher,
            matcher -> matcher.group("policyName"));

        direction.ifPresent(value -> trafficPolicyBuilder.setDirection(getDirection(value, "policy")));
        policyName.ifPresent(trafficPolicyBuilder::setTrafficPolicy);

        if (trafficPolicyBuilder.getTrafficPolicy() != null && trafficPolicyBuilder.getDirection() != null) {
            ifHuaweiAugBuilder.setTrafficPolicy(trafficPolicyBuilder.build());
        }
    }

    private Direction getDirection(String value, String type) {
        switch (value) {
            case "inbound":
                return Direction.Inbound;
            case "outbound":
                return Direction.Outbound;
            default:
                throw new IllegalArgumentException("Cannot parse traffic-" + type + " direction value: " + value);
        }
    }

    private boolean isHuaweiExtAugNotEmpty(final IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        return !ifHuaweiAugBuilder.build().equals((new IfHuaweiAugBuilder()).build());
    }

    private void setFlowStatInterval(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        final Optional<String> interval = ParsingUtils.parseField(output, 0,
            FLOW_STAT_INTERVAL_LINE::matcher,
            matcher -> matcher.group("interval"));
        interval.ifPresent(value -> ifHuaweiAugBuilder.setFlowStatInterval(Long.valueOf(value)));
    }

    private void setArpExpireTime(String output, IfHuaweiAugBuilder ifHuaweiAugBuilder) {
        final Optional<String> arpExpTime = ParsingUtils.parseField(output, 0,
            ARP_EXPIRE_TIME_LINE::matcher,
            matcher -> matcher.group("arpExpTime"));
        ifHuaweiAugBuilder.setExpireTimeout(1200L);
        arpExpTime.ifPresent(value -> ifHuaweiAugBuilder.setExpireTimeout(Long.valueOf(value)));
    }
}