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

package io.frinx.cli.unit.brocade.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceReader extends AbstractSubinterfaceReader {

    private Ipv4ConfigReader v4reader;
    private Ipv6ConfigReader v6reader;

    public SubinterfaceReader(Cli cli) {
        super(cli);
        v4reader = new Ipv4ConfigReader(cli);
        v6reader = new Ipv6ConfigReader(cli);
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        boolean hasIpv4Address = v4reader.hasIpAddress(instanceIdentifier, ifcName, readContext);
        boolean hasIpv6Address = v6reader.hasIpAddress(instanceIdentifier, ifcName, readContext);

        if (hasIpv4Address || hasIpv6Address) {
            return Collections.singletonList(new SubinterfaceKey(ZERO_SUBINTERFACE_ID));
        }
        return Collections.emptyList();
    }

    @Override
    protected String getReadCommand() {
        return "";
    }

    @Override
    protected List<SubinterfaceKey> parseSubinterfaceIds(String output, String ifcName) {
        return Collections.emptyList();
    }
}
