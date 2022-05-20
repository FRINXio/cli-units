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

package io.frinx.cli.unit.cer.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cer.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.cer.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceReader extends AbstractSubinterfaceReader {

    public static final String SEPARATOR = ".";

    private final InterfaceReader interfaceReader;
    private final Ipv4ConfigReader v4Reader;
    private final Ipv6ConfigReader v6Reader;
    private final Cli cli;

    public SubinterfaceReader(Cli cli) {
        super(cli);
        this.cli = cli;
        this.interfaceReader = new InterfaceReader(cli);
        this.v4Reader = new Ipv4ConfigReader(cli);
        this.v6Reader = new Ipv6ConfigReader(cli);
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final List<SubinterfaceKey> keys = parseSubinterfaceIds(blockingRead(getReadCommand(),
                cli, instanceIdentifier, readContext), ifcName);

        boolean hasIpv4Address = v4Reader.hasIpAddress(instanceIdentifier, ifcName, readContext);
        boolean hasIpv6Address = v6Reader.hasIpAddress(instanceIdentifier, ifcName, readContext);

        if (hasIpv4Address || hasIpv6Address) {
            keys.add(new SubinterfaceKey(ZERO_SUBINTERFACE_ID));
        }

        return keys;
    }

    @Override
    protected List<SubinterfaceKey> parseSubinterfaceIds(String output, String ifcName) {
        // Now exclude interfaces
        return interfaceReader.parseAllInterfaceIds(output)
                .stream()
                .filter(key -> isSubinterface(key.getName()))
                .map(InterfaceKey::getName)
                .filter(subifcName -> subifcName.startsWith(ifcName))
                .map(name -> name.substring(name.lastIndexOf(SEPARATOR) + 1))
                .map(subifcIndex -> new SubinterfaceKey(Long.valueOf(subifcIndex)))
                .collect(Collectors.toList());
    }

    @Override
    protected String getReadCommand() {
        return InterfaceReader.SH_INTERFACES;
    }

    public static boolean isSubinterface(final String ifcName) {
        return InterfaceReader.SUBINTERFACE_NAME.matcher(ifcName).matches();
    }

}