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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigWriter;
import io.frinx.cli.unit.ifc.base.util.NetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;

public final class Ipv4ConfigWriter extends AbstractIpv4ConfigWriter {

    private static final String TEMPLATE = """
            configure terminal
            interface {$name}
            {% if ($delete) %}no ip address
            {% else %}ip address {$ip} {$netmask}
            {% endif %}end""";

    public Ipv4ConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(TEMPLATE,
                "name", ifcName,
                "ip", config.getIp().getValue(),
                "netmask", NetUtils.getSubnetInfo(config.getIp(), config.getPrefixLength()));
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(TEMPLATE,
                "name", ifcName,
                "delete", Chunk.TRUE);
    }

}
