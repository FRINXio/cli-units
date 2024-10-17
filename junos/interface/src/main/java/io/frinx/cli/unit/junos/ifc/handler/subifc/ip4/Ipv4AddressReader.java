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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4AddressesReader;
import io.frinx.cli.unit.junos.ifc.Util;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Ipv4AddressReader extends AbstractIpv4AddressesReader {

    static final String SH_RUN_INT_IP =
        "show configuration interfaces %s | display set | match \" inet address \"";

    static final Pattern INTERFACE_IP_LINE = Pattern
        .compile("set interfaces (?<ifcId>.+) unit (?<subifcIndex>[0-9]+) family "
            + "inet address (?<ip>[^/]+)/(?<prefix>[0-9]+)");

    static boolean SUPPORTED_INTERFACE = true;

    public Ipv4AddressReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_RUN_INT_IP, ifcName);
    }

    @Override
    protected Pattern getIpLine() {
        return INTERFACE_IP_LINE;
    }

    @Override
    protected String getInterfaceName(String ifcName, Long subId) {
        return Util.getSubinterfaceName(ifcName, subId);
    }

    @Override
    public boolean isSupportedInterface(InstanceIdentifier<Address> instanceIdentifier) {
        return SUPPORTED_INTERFACE;
    }
}
