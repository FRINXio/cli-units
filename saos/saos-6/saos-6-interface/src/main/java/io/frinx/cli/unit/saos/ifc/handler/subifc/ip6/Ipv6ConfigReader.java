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

package io.frinx.cli.unit.saos.ifc.handler.subifc.ip6;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigReader extends AbstractIpv6ConfigReader {

    private static final Pattern IPV6_UNICAST_ADDRESS =
            Pattern.compile("\\| IPv6 Addr/Mask *\\| (?<ip>[a-fA-F0-9:]+)/(?<prefix>[\\d]+) *\\| .*\\|");

    public Ipv6ConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected Pattern getIpLine() {
        return IPV6_UNICAST_ADDRESS;
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(Ipv6AddressReader.SH_INTERFACE_IP, ifcName);
    }

    private String getAlternativeReadCommand(String ifcName) {
        return f(Ipv6AddressReader.SH_IP_INTERFACE_IP, ifcName);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            Ipv6AddressNoZone address = instanceIdentifier.firstKeyOf(Address.class).getIp();
            parseAddressConfig(configBuilder,
                    blockingRead(getReadCommand(ifcName), cli, instanceIdentifier, readContext), address);
            // we have 2 diff commands, so if parsing first one returns null then try the other one
            if (configBuilder.getIp() == null && configBuilder.getPrefixLength() == null) {
                parseAddressConfig(configBuilder, blockingRead(getAlternativeReadCommand(ifcName),
                        cli, instanceIdentifier, readContext), address);
            }
        }
    }

    @Override
    public boolean hasIpAddress(InstanceIdentifier instanceIdentifier, String ifcName, ReadContext ctx)
            throws ReadFailedException {
        String output = blockingRead(getReadCommand(ifcName), cli, instanceIdentifier, ctx);
        String alternativeReadCommand = getAlternativeReadCommand(ifcName);
        // we have 2 diff commands, so if first one returns error then try the other one
        if (output.contains("no matching entry found")) {
            output = blockingRead(alternativeReadCommand, cli, instanceIdentifier, ctx);
        }
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(getIpLine()::matcher)
                .anyMatch(Matcher::matches);
    }
}