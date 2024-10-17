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

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.SubIfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigWriter extends AbstractSubinterfaceConfigWriter {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_UPDATE_TEMPLATE = """
            system-view
            interface {$data.name}
            {% if ($data.description) %}description {$data.description}
            {% else %}undo description
            {% endif %}{% if ($before_huawei_aug.dot1q) %}undo dot1q termination vid {$before_huawei_aug.dot1q}
            {% endif %}{% if ($huawei_aug.dot1q) %}dot1q termination vid {$huawei_aug.dot1q}
            {% endif %}{% if ($before_huawei_aug.ip_binding_vpn_instance) %}undo ip binding vpn-instance {$before_huawei_aug.ip_binding_vpn_instance}
            {% endif %}{% if ($huawei_aug.ip_binding_vpn_instance) %}ip binding vpn-instance {$huawei_aug.ip_binding_vpn_instance}
            {% endif %}{% if ($before_huawei_aug.traffic_filter) %}undo traffic-filter {$before_huawei_aug.traffic_filter.direction}
            {% endif %}{% if ($huawei_aug.traffic_filter) %}{% if ($is_ipv6 == TRUE) %}traffic-filter {$huawei_aug.traffic_filter.direction} ipv6 acl name {$huawei_aug.traffic_filter.acl_name}
            {% else %}traffic-filter {$huawei_aug.traffic_filter.direction} acl name {$huawei_aug.traffic_filter.acl_name}
            {% endif %}{% endif %}{% if ($before_huawei_aug.traffic_policy) %}undo traffic-policy {$before_huawei_aug.traffic_policy.direction}
            {% endif %}{% if ($huawei_aug.traffic_policy) %}traffic-policy {$huawei_aug.traffic_policy.traffic_name} {$huawei_aug.traffic_policy.direction}
            {% endif %}{% if ($trust_dscp == TRUE) %}trust dscp
            {% else %}undo trust
            {% endif %}return""";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo interface {$name}
            return""";

    public SubinterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after, InstanceIdentifier<Config> id) {
        SubIfHuaweiAug huaweiAug = after.getAugmentation(SubIfHuaweiAug.class);

        // we need this because chunk template has problems with pulling boolean type from object
        Boolean trustDscp = null;
        Boolean isIpv6 = null;
        if (huaweiAug != null) {
            trustDscp = huaweiAug.isTrustDscp();
            if (huaweiAug.getTrafficFilter() != null) {
                isIpv6 = huaweiAug.getTrafficFilter().isIpv6();
            }
        }

        SubIfHuaweiAug augBefore = null;
        if (before != null) {
            augBefore = before.getAugmentation(SubIfHuaweiAug.class);
        }
        return fT(WRITE_UPDATE_TEMPLATE,
                "before", before,
                "data", after,
                "before_huawei_aug", augBefore,
                "huawei_aug", after.getAugmentation(SubIfHuaweiAug.class),
                "trust_dscp", trustDscp,
                "is_ipv6", isIpv6,
                "name", getSubinterfaceName(id));
    }

    @Override
    protected String deleteTemplate(InstanceIdentifier<Config> id) {
        return fT(DELETE_TEMPLATE, "name", getSubinterfaceName(id));
    }

    private String getSubinterfaceName(InstanceIdentifier<Config> id) {
        final InterfaceKey ifcKey = id.firstKeyOf(Interface.class);
        final SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);
        return ifcKey.getName() + SubinterfaceReader.SEPARATOR + subKey.getIndex().toString();
    }
}
