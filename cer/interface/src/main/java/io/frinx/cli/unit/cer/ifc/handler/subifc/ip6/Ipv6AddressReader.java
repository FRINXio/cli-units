/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.subifc.ip6;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6AddressesReader;
import java.util.regex.Pattern;

public final class Ipv6AddressReader extends AbstractIpv6AddressesReader {

    public static final String SH_INTERFACE_IP = "show running-config interface %s | include ^ ipv6 address";
    private static final Pattern IPV6_LOCAL_ADDRESS = Pattern.compile("ipv6 address (?<ipv6local>\\S+) link-local");
    private static final Pattern IPV6_UNICAST_ADDRESS =
            Pattern.compile("ipv6 address (?<ipv6unicast>[a-fA-F0-9:]+)/(?<prefix>\\d+)");

    public Ipv6AddressReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_INTERFACE_IP, ifcName);
    }

    @Override
    protected Pattern getLocalIpLine() {
        return IPV6_LOCAL_ADDRESS;
    }

    @Override
    protected Pattern getUnicastIpLine() {
        return IPV6_UNICAST_ADDRESS;
    }

}
