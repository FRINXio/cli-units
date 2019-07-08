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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigWriter;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;

public final class Ipv6ConfigWriter extends AbstractIpv6ConfigWriter {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface ${name}\n"
            + "{% if($delete) %}no {% endif %}ipv6 address ${address}\n"
            + "end";

    private static final Pattern LINK_LOCAL = Pattern.compile("[Ff][Ee][89AaBb].*");


    public Ipv6ConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(WRITE_TEMPLATE, "name", ifcName, "address", createAddressCommand(config));
    }

    private static String createAddressCommand(Config config) {
        String ip = config.getIp().getValue();
        return ip + (isLinkLocal(ip) ? " link-local" : "/" + config.getPrefixLength().toString());
    }

    /*
     * Check if address belongs to subnet FE80::/10
     */
    private static boolean isLinkLocal(String ip) {
        return LINK_LOCAL.matcher(ip).find();
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(WRITE_TEMPLATE, "name", ifcName, "address", createAddressCommand(config), "delete", true);
    }
}
