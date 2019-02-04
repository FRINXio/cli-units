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

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip4.Ipv4AddressReader;
import io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6AddressReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceReader implements CliConfigListReader<Subinterface, SubinterfaceKey,
        SubinterfaceBuilder> {

    private static final char SEPARATOR = '.';
    public static final long ZERO_SUBINTERFACE_ID = 0L;

    private Cli cli;

    public SubinterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Interface.class).getName();

        List<SubinterfaceKey> subinterfaceKeys = parseInterfaceIds(blockingRead(InterfaceReader.SH_INTERFACE, cli,
                instanceIdentifier, readContext), id);

        // Subinterface with ID 0 is reserved for IP addresses of the interface
        boolean hasIpv4Address = !Ipv4AddressReader.parseAddressIds(
                blockingRead(String.format(Ipv4AddressReader.SH_INTERFACE_IP, id),
                        cli, instanceIdentifier, readContext)).isEmpty();

        boolean hasIpv6Address = !Ipv6AddressReader.parseAddressIds(
                blockingRead(String.format(Ipv6AddressReader.SH_INTERFACE_IP, id),
                        cli, instanceIdentifier, readContext)).isEmpty();
        if (hasIpv4Address || hasIpv6Address) {
            subinterfaceKeys.add(new SubinterfaceKey(ZERO_SUBINTERFACE_ID));
        }

        return subinterfaceKeys;
    }

    @VisibleForTesting
    static List<SubinterfaceKey> parseInterfaceIds(String output, String ifcName) {
        return InterfaceReader.parseAllInterfaceIds(output)
                // Now exclude interfaces
                .stream()
                .filter(InterfaceReader::isSubinterface)
                .map(InterfaceKey::getName)
                .filter(subifcName -> subifcName.startsWith(ifcName))
                .map(name -> name.substring(name.lastIndexOf(SEPARATOR) + 1))
                .map(subifcIndex -> new SubinterfaceKey(Long.valueOf(subifcIndex)))
                .collect(Collectors.toList());
    }

    static String getSubinterfaceName(InstanceIdentifier<?> id) {
        InterfaceKey ifcKey = id.firstKeyOf(Interface.class);
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        return ifcKey.getName() + SEPARATOR + subKey.getIndex().toString();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Subinterface> id,
                                      @Nonnull SubinterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        builder.setIndex(id.firstKeyOf(Subinterface.class).getIndex());
    }
}
