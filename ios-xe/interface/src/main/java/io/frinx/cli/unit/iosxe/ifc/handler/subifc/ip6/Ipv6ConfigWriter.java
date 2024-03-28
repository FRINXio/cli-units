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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigWriter;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;

public final class Ipv6ConfigWriter extends AbstractIpv6ConfigWriter {

    private static final Pattern LINK_LOCAL = Pattern.compile("[Ff][Ee][89AaBb].*");
    private static final String TEMPLATE = """
            configure terminal
            interface {$name}
            {% if ($delete) %}no {% endif %}ipv6 address {$address}
            end""";

    public Ipv6ConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(TEMPLATE,
                "name", ifcName,
                "address", createAddressCommand(config));
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(TEMPLATE,
                "name", ifcName,
                "address", createAddressCommand(config),
                "delete", Chunk.TRUE);
    }

    private String createAddressCommand(final Config config) {
        final String ip = config.getIp().getValue();
        return ip + (isLinkLocal(ip) ? " link-local" : "/" + config.getPrefixLength().toString());
    }

    // check if address belongs to subnet FE80::/10
    private boolean isLinkLocal(final String ip) {
        return LINK_LOCAL.matcher(ip).find();
    }

}