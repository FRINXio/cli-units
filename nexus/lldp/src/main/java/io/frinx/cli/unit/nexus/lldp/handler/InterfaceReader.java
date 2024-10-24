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

package io.frinx.cli.unit.nexus.lldp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliOperListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SHOW_LLDP_INTERFACE = "show lldp all";
    private static final Pattern INTERFACE_PATTERN =
        Pattern.compile("Interface Information: (?<interface>.+) Enable.+");

    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }


    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(SHOW_LLDP_INTERFACE, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(String showLldpInterfaceOutput) {
        return ParsingUtils.parseFields(showLldpInterfaceOutput, 0,
                INTERFACE_PATTERN::matcher,
            matcher -> matcher.group("interface"),
                InterfaceKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder, @NotNull ReadContext readContext)
            throws ReadFailedException {
        interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}