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

package io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigReader;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Ipv4ConfigReader extends AbstractIpv4ConfigReader {

    public Ipv4ConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected Pattern getIpLine() {
        return Ipv4AddressReader.INTERFACE_IP_LINE;
    }

    @Override
    protected String getReadCommand(String ifcName, Long subId) {
        if (subId != 0) {
            return f(Ipv4AddressReader.DISPLAY_IP_INT_BRIEF, ifcName + "." + subId);
        }
        return f(Ipv4AddressReader.DISPLAY_IP_INT_BRIEF, ifcName);
    }

    @Override
    @VisibleForTesting
    public boolean isSupportedInterface(InstanceIdentifier<Config> instanceIdentifier) {
        return true;
    }
}
