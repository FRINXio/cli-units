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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6ConfigReader extends AbstractIpv6ConfigReader {

    public Ipv6ConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected Pattern getIpLine() {
        return Ipv6AddressReader.IPV6_UNICAST_ADDRESS;
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(Ipv6AddressReader.SH_INTERFACE_IP, ifcName);
    }

    @VisibleForTesting
    @Override
    public void parseAddressConfig(ConfigBuilder configBuilder, String output, Ipv6AddressNoZone address) {
        Optional<String> optionalAddressLine = ParsingUtils.NEWLINE.splitAsStream(output)
            .filter(line -> line.contains(address.getValue()))
            .findAny();

        if (!optionalAddressLine.isPresent()) {
            return;
        }

        configBuilder.setIp(address);
        configBuilder.setPrefixLength(AbstractIpv6ConfigReader.DEFAULT_PREFIX_LENGHT);

        ParsingUtils.parseField(optionalAddressLine.get(),
            Ipv6AddressReader.IPV6_UNICAST_ADDRESS::matcher,
            m -> Short.parseShort(m.group("prefix")),
            configBuilder::setPrefixLength);
    }
}
