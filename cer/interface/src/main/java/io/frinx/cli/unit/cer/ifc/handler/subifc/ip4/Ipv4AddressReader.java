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

package io.frinx.cli.unit.cer.ifc.handler.subifc.ip4;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4AddressesReader;
import java.util.regex.Pattern;

public final class Ipv4AddressReader extends AbstractIpv4AddressesReader {

    public static final String SH_INTERFACE_IP = "show running-config interface %s | include ^ ip address";
    public static final Pattern INTERFACE_IP_LINE = Pattern.compile("ip address (?<ip>\\S+) (?<prefix>\\S+)");

    public Ipv4AddressReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_INTERFACE_IP, ifcName);
    }

    @Override
    protected Pattern getIpLine() {
        return INTERFACE_IP_LINE;
    }

}
