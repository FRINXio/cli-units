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

package io.frinx.cli.unit.huawei.ifc.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.SubIfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.SubIfHuaweiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.TrafficDirection.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei.sub._if.extension.config.TrafficFilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei.sub._if.extension.config.TrafficPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceConfigReader extends AbstractSubinterfaceConfigReader {

    private static final Pattern TRAFFIC_FILTER_LINE =
            Pattern.compile("traffic-filter (?<direction>\\S+) (?<ipv6>ipv6\\s)?acl name (?<aclName>.+)");
    private static final Pattern TRAFFIC_POLICY_LINE =
            Pattern.compile("traffic-policy (?<policyName>.+) (?<direction>.+)");
    private static final Pattern VPN_INSTANCE_LINE = Pattern.compile("ip binding vpn-instance (?<aclName>.+)");
    private static final Pattern DOT1Q_LINE = Pattern.compile("dot1q termination vid (?<id>\\d+)");
    private static final Pattern TRUST_DSCP_LINE = Pattern.compile("trust dscp");

    public SubinterfaceConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String subIfcName) {
        return f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, subIfcName);
    }

    @Override
    protected String getSubinterfaceName(InstanceIdentifier<Config> id) {
        final InterfaceKey ifcKey = id.firstKeyOf(Interface.class);
        final SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);
        return ifcKey.getName() + SubinterfaceReader.SEPARATOR + subKey.getIndex().toString();
    }

    @Override
    protected Pattern getShutdownLine() {
        return InterfaceConfigReader.SHUTDOWN_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return InterfaceConfigReader.DESCR_LINE;
    }

    @Override
    @VisibleForTesting
    public void parseSubinterface(final String output, final ConfigBuilder builder, Long subKey, String name) {
        super.parseSubinterface(output, builder, subKey, name);

        final SubIfHuaweiAugBuilder subIfHuaweiAugBuilder = new SubIfHuaweiAugBuilder();
        setVpnInstance(output, subIfHuaweiAugBuilder);
        setTrustDscp(output, subIfHuaweiAugBuilder);
        setDot1q(output, subIfHuaweiAugBuilder);
        setTrafficFilter(output, subIfHuaweiAugBuilder);
        setTrafficPolicy(output, subIfHuaweiAugBuilder);
        if (isHuaweiExtAugNotEmpty(subIfHuaweiAugBuilder)) {
            builder.addAugmentation(SubIfHuaweiAug.class, subIfHuaweiAugBuilder.build());
        }
    }

    private void setVpnInstance(String output, SubIfHuaweiAugBuilder subIfHuaweiAugBuilder) {
        ParsingUtils.parseField(output, 0,
            VPN_INSTANCE_LINE::matcher,
            matcher -> matcher.group("aclName"),
            subIfHuaweiAugBuilder::setIpBindingVpnInstance);
    }

    private void setTrustDscp(String output, SubIfHuaweiAugBuilder subIfHuaweiAugBuilder) {
        ParsingUtils.parseField(output, 0,
            TRUST_DSCP_LINE::matcher,
            matcher -> true,
            subIfHuaweiAugBuilder::setTrustDscp);
    }

    private void setDot1q(String output, SubIfHuaweiAugBuilder subIfHuaweiAugBuilder) {
        ParsingUtils.parseField(output, 0,
            DOT1Q_LINE::matcher,
            matcher -> matcher.group("id"),
            value -> subIfHuaweiAugBuilder.setDot1qVlanId(Long.valueOf(value)));
    }

    private void setTrafficFilter(String output, SubIfHuaweiAugBuilder subIfHuaweiAugBuilder) {
        final Optional<String> direction = ParsingUtils.parseField(output, 0,
            TRAFFIC_FILTER_LINE::matcher,
            matcher -> matcher.group("direction"));

        final Optional<String> aclName = ParsingUtils.parseField(output, 0,
            TRAFFIC_FILTER_LINE::matcher,
            matcher -> matcher.group("aclName"));

        final boolean isIpv6 = ParsingUtils.parseField(output, 0,
            TRAFFIC_FILTER_LINE::matcher,
            matcher -> matcher.group("ipv6")).isPresent();

        TrafficFilterBuilder subTrafficFilterBuilder = new TrafficFilterBuilder();
        direction.ifPresent(value -> subTrafficFilterBuilder.setDirection(getDirection(value, "filter")));
        aclName.ifPresent(subTrafficFilterBuilder::setAclName);

        if (subTrafficFilterBuilder.getAclName() != null && subTrafficFilterBuilder.getDirection() != null) {
            subTrafficFilterBuilder.setIpv6(isIpv6);
            subIfHuaweiAugBuilder.setTrafficFilter(subTrafficFilterBuilder.build());
        }
    }

    private void setTrafficPolicy(String output, SubIfHuaweiAugBuilder subIfHuaweiAugBuilder) {
        final Optional<String> direction = ParsingUtils.parseField(output, 0,
            TRAFFIC_POLICY_LINE::matcher,
            matcher -> matcher.group("direction"));

        final Optional<String> policyName = ParsingUtils.parseField(output, 0,
            TRAFFIC_POLICY_LINE::matcher,
            matcher -> matcher.group("policyName"));

        TrafficPolicyBuilder subTrafficPolicyBuilder = new TrafficPolicyBuilder();
        direction.ifPresent(value -> subTrafficPolicyBuilder.setDirection(getDirection(value, "policy")));
        policyName.ifPresent(subTrafficPolicyBuilder::setTrafficName);

        if (subTrafficPolicyBuilder.getTrafficName() != null && subTrafficPolicyBuilder.getDirection() != null) {
            subIfHuaweiAugBuilder.setTrafficPolicy(subTrafficPolicyBuilder.build());
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

    private boolean isHuaweiExtAugNotEmpty(final SubIfHuaweiAugBuilder subIfHuaweiAugBuilder) {
        return !subIfHuaweiAugBuilder.build().equals((new SubIfHuaweiAugBuilder()).build());
    }

}
