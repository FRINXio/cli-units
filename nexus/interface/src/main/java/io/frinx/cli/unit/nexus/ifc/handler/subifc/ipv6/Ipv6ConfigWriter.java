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

package io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;

public final class Ipv6ConfigWriter extends AbstractIpv6ConfigWriter {

    private static final String WRITE_TEMPLATE = """
            interface {$name}
            {% if($delete) %}no {% endif %}ipv6 address {$data.ip.value}/{$data.prefix_length}
            root""";


    public Ipv6ConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(WRITE_TEMPLATE, "name", ifcName, "data", config);
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(WRITE_TEMPLATE, "name", ifcName, "data", config, "delete", true);
    }
}
