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

package io.frinx.cli.unit.brocade.ifc.handler.subifc.ip6;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class Ipv6ConfigReader extends AbstractIpv6ConfigReader {

    private static final Pattern IPV6_UNICAST_ADDRESS =
            Pattern.compile("ipv6 address (?!(fe80:))(?<ip>[^\\\\s]+)/(?<prefix>[0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8])");

    public Ipv6ConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected Pattern getIpLine() {
        return IPV6_UNICAST_ADDRESS;
    }

    @Override
    protected String getReadCommand(String ifcName) {
        Class<? extends InterfaceType> ifcType = Util.parseType(ifcName);
        String ifcNumber = Util.getIfcNumber(ifcName);
        return fT(Ipv6AddressReader.SH_INTERFACE_IP, "ifcType", Util.getTypeOnDevice(ifcType), "ifcNumber", ifcNumber);
    }

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
            IPV6_UNICAST_ADDRESS::matcher,
            m -> Short.parseShort(m.group("prefix")),
            configBuilder::setPrefixLength);
    }
}
