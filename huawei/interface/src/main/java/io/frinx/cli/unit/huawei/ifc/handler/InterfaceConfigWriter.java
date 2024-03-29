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

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.HuaweiIfExtensionConfig.Trust;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    // FIXME: https://github.com/tomj74/chunk-templates/pull/29
    private static final String WRITE_UPDATE_TEMPLATE = "system-view\n"
            + "interface {$data.name}\n"
            + "{$data|update(mtu,mtu `$data.mtu`\n,undo mtu\n)}"
            + "{$data|update(description,description `$data.description`\n,undo description\n)}"
            //  + "{$data|update(is_enabled,shutdown\n,undo shutdown\n}"
            + "{% if ($enabled) %}undo shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($before_huawei_aug.ip_binding_vpn_instance) %}"
            + "undo ip binding vpn-instance {$before_huawei_aug.ip_binding_vpn_instance}\n{% endif %}"
            + "{% if ($huawei_aug.ip_binding_vpn_instance) %}"
            + "ip binding vpn-instance {$huawei_aug.ip_binding_vpn_instance}\n{% endif %}"
            + "{% if ($huawei_aug.flow_stat_interval) %}set flow-stat interval {$huawei_aug.flow_stat_interval}\n"
            + "{% else %}undo set flow-stat interval\n{% endif %}"
            + "{% if ($before_huawei_aug.traffic_filter) %}"
            + "undo traffic-filter {$before_huawei_aug.traffic_filter.direction}\n{% endif %}"
            + "{% if ($huawei_aug.traffic_filter) %}{% if ($is_ipv6 == TRUE) %}"
            + "traffic-filter {$huawei_aug.traffic_filter.direction} ipv6 acl "
            + "name {$huawei_aug.traffic_filter.acl_name}\n"
            + "{% else %}traffic-filter {$huawei_aug.traffic_filter.direction} acl "
            + "name {$huawei_aug.traffic_filter.acl_name}\n"
            + "{% endif %}{% endif %}"
            + "{% if ($before_huawei_aug.traffic_policy) %}"
            + "undo traffic-policy {$before_huawei_aug.traffic_policy.direction}\n{% endif %}"
            + "{% if ($huawei_aug.traffic_policy) %}traffic-policy {$huawei_aug.traffic_policy.traffic_policy} "
            + "{$huawei_aug.traffic_policy.direction}\n{% endif %}"
            + "{% if ($trust) %}trust {$trust}\n"
            + "{% else %}undo trust\n{% endif %}"
            + "{% if ($lldp_enabled == TRUE) %}lldp enable\n"
            + "{% else %}undo lldp enable\n{% endif %}"
            + "{% if ($huawei_aug.expire_timeout) %}"
            + "arp expire-time {$huawei_aug.expire_timeout}\n"
            + "{% else %}"
            + "undo arp expire-time\n"
            + "{% endif %}"
            + "return";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo interface {$data.name}
            return""";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        IfHuaweiAug huaweiAug = after.getAugmentation(IfHuaweiAug.class);
        // we need this because chunk template has problems with pulling boolean type from object
        String trust = null;
        Boolean isIpv6 = null;
        Boolean lldpEnabled = null;
        if (huaweiAug != null) {
            lldpEnabled = huaweiAug.isLldpEnabled();
            if (huaweiAug.getTrust() != null) {
                trust = getTrust(huaweiAug.getTrust());
            }
            if (huaweiAug.getTrafficFilter() != null) {
                isIpv6 = huaweiAug.getTrafficFilter().isIpv6();
            }
        }

        IfHuaweiAug augBefore = null;
        if (before != null) {
            augBefore = before.getAugmentation(IfHuaweiAug.class);
        }
        return fT(WRITE_UPDATE_TEMPLATE, "before", before, "data", after,
            "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
            "before_huawei_aug", augBefore,
            "huawei_aug", after.getAugmentation(IfHuaweiAug.class),
            "is_ipv6", isIpv6,
            "trust", trust,
            "lldp_enabled", lldpEnabled);
    }

    private String getTrust(Trust value) {
        switch (value) {
            case Dscp:
                return "dscp";
            case _8021pInner:
                return "8021p inner";
            case _8021pOuter:
                return "8021p outer";
            default:
                throw new IllegalArgumentException("Cannot parse trust value: " + value.getName());
        }
    }

    @Override
    public boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data);
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }
}