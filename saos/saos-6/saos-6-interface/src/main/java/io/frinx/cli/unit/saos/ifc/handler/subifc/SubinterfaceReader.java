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

package io.frinx.cli.unit.saos.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.saos.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceReader implements CliConfigListReader<Subinterface, SubinterfaceKey, SubinterfaceBuilder> {

    public static final long ZERO_SUBINTERFACE_ID = 0L;

    private final Ipv4ConfigReader v4reader;
    private final Ipv6ConfigReader v6reader;
    private final Cli cli;

    public SubinterfaceReader(Cli cli) {
        this.cli = cli;
        this.v4reader = new Ipv4ConfigReader(cli);
        this.v6reader = new Ipv6ConfigReader(cli);
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        List<SubinterfaceKey> keys = new ArrayList<>();
        boolean hasIpv4Address = v4reader.hasIpAddress(instanceIdentifier, ifcName, readContext);
        boolean hasIpv6Address = v6reader.hasIpAddress(instanceIdentifier, ifcName, readContext);

        if (hasIpv4Address || hasIpv6Address) {
            keys.add(new SubinterfaceKey(ZERO_SUBINTERFACE_ID));
        }
        return keys;
    }


    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                      @Nonnull SubinterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setIndex(instanceIdentifier.firstKeyOf(Subinterface.class).getIndex());
    }
}
