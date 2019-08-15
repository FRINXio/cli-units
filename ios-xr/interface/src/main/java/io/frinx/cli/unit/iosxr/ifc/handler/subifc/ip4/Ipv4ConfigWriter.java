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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigWriter;
import io.frinx.cli.unit.ifc.base.util.NetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4ConfigWriter extends AbstractIpv4ConfigWriter {

    private static final String TEMPLATE = "interface {$name}\n"
            + "{% if ($delete) %}no ipv4 address\n{% else %}ipv4 address {$ip} {$netmask}\n{% endif %}"
            + "root";

    public Ipv4ConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(TEMPLATE, "name", ifcName,
                "ip", config.getIp().getValue(),
                "netmask", NetUtils.getSubnetInfo(config.getIp(), config.getPrefixLength()));
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(TEMPLATE, "name", ifcName, "delete", true);
    }

    @Override
    protected String getInterfaceName(String ifcName, Long subId) {
        return Ipv4AddressReader.getTargetInterfaceName(ifcName, subId);
    }

    @Override
    public boolean isSupportedInterface(InstanceIdentifier<Config> instanceIdentifier) {
        return Ipv4AddressReader.SUPPORTED_INTERFACE;
    }
}
