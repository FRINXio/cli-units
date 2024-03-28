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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private final Cli cli;
    public static final String SH_IFACES = "show running-config router hsrp | include ^ interface";
    private static final Pattern IFACE_LINE = Pattern.compile("interface (?<id>[\\S]+)");

    public HsrpInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
            @NotNull InterfaceBuilder interfaceBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        interfaceBuilder.setInterfaceId(instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId());
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
            @NotNull ReadContext readContext) throws ReadFailedException {
        return parseAllInterfaceIds(blockingRead(SH_IFACES, cli, instanceIdentifier, readContext))
                .stream().collect(Collectors.toList());
    }

    @VisibleForTesting
    public static List<InterfaceKey> parseAllInterfaceIds(String output) {
        return ParsingUtils.parseFields(output, 0, IFACE_LINE::matcher, matcher -> matcher.group("id"),
                InterfaceKey::new);
    }
}