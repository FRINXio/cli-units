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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4.Ipv4ConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceReader extends AbstractSubinterfaceReader {

    public static final String SEPARATOR = ".";

    private InterfaceReader ifaceReader;
    private Ipv4ConfigReader v4reader;
    private Ipv6ConfigReader v6reader;
    private Cli cli;

    private static final Pattern SUBINTERFACE_NAME = Pattern.compile("(?<ifcId>.+)[.](?<subifcIndex>[0-9]+)");

    public SubinterfaceReader(Cli cli) {
        super(cli);
        this.cli = cli;
        this.ifaceReader = new InterfaceReader(cli);
        this.v4reader = new Ipv4ConfigReader(cli);
        this.v6reader = new Ipv6ConfigReader(cli);
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        List<SubinterfaceKey> keys = parseSubinterfaceIds(blockingRead(getReadCommand(),
                cli, instanceIdentifier, readContext), ifcName);
        boolean hasIpv4Address = v4reader.hasIpAddress(instanceIdentifier, ifcName, readContext);
        boolean hasIpv6Address = v6reader.hasIpAddress(instanceIdentifier, ifcName, readContext);

        if (hasIpv4Address || hasIpv6Address) {
            keys.add(new SubinterfaceKey(ZERO_SUBINTERFACE_ID));
        }
        return keys;
    }

    @Override
    protected String getReadCommand() {
        return InterfaceReader.SH_RUN_INTERFACE;
    }

    @VisibleForTesting
    @Override
    public List<SubinterfaceKey> parseSubinterfaceIds(String output, String ifcName) {
        return ifaceReader.parseAllInterfaceIds(output)
            // Now exclude interfaces
            .stream()
            .filter(key -> isSubinterface(key.getName()))
            .map(InterfaceKey::getName)
            .filter(subifcName -> subifcName.startsWith(ifcName))
            .map(name -> name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length()))
            .map(subifcIndex -> new SubinterfaceKey(Long.valueOf(subifcIndex)))
            .collect(Collectors.toList());
    }

    private boolean isSubinterface(String ifcName) {
        return SUBINTERFACE_NAME.matcher(ifcName).matches();
    }
}
