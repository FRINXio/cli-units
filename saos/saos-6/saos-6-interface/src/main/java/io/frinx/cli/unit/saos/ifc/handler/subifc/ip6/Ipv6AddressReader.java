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
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6AddressesReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6AddressReader extends AbstractIpv6AddressesReader {

    protected static final String SH_INTERFACE_IP = "interface show %s";
    protected static final String SH_IP_INTERFACE_IP = "interface show ip-interface %s";
    private static final Pattern IPV6_UNICAST_ADDRESS =
            Pattern.compile("\\| IPv6 Addr/Mask *\\| (?<ipv6unicast>[a-fA-F0-9:]+)/(?<prefix>[\\d]+) *\\| .*\\|");
    private static final Pattern IPV6_LOCAL_ADDRESS = Pattern.compile("ipv6 address (?<ipv6local>[^\\s]+) link-local");

    public Ipv6AddressReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_INTERFACE_IP, ifcName);
    }

    private String getAlternativeReadCommand(String ifcName) {
        return f(SH_IP_INTERFACE_IP, ifcName);
    }

    @Override
    protected Pattern getLocalIpLine() {
        return IPV6_LOCAL_ADDRESS;
    }

    @Override
    protected Pattern getUnicastIpLine() {
        return IPV6_UNICAST_ADDRESS;
    }

    @NotNull
    @Override
    public List<AddressKey> getAllIds(@NotNull InstanceIdentifier<Address> instanceIdentifier,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            // we have 2 diff commands, so if parsing first one returns empty list then try the other one
            List<AddressKey> keys = parseAddressIds(blockingRead(getReadCommand(ifcName), cli,
                    instanceIdentifier, ctx));
            if (keys.isEmpty()) {
                return parseAddressIds(blockingRead(getAlternativeReadCommand(ifcName), cli, instanceIdentifier, ctx));
            }
            return keys;
        } else {
            return Collections.emptyList();
        }
    }
}