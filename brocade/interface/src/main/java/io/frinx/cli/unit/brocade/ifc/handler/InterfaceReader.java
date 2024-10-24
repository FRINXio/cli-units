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

package io.frinx.cli.unit.brocade.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_INTERFACE = "show running-config interface | include interface";
    private static final String SH_OPER_INTERFACES = "show interface brief";

    private static final Pattern IFC_CFG_LINE = Pattern.compile("interface (?<id>.+)[.\\s]*");
    private static final Pattern IFC_OPER_LINE = Pattern.compile("^(?<id>\\d+/\\d+)\\s+.*");

    private final Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> id,
                                        @NotNull ReadContext context) throws ReadFailedException {
        List<InterfaceKey> interfaceKeys = parseInterfaceIds(blockingRead(SH_INTERFACE, cli, id, context));
        List<InterfaceKey> interfaceOperKeys = parseOperInterfaceIds(
                blockingRead(Command.showCommandNoLocalProcessing(SH_OPER_INTERFACES, context), cli, id));
        Sets.SetView<InterfaceKey> hidden =
                Sets.difference(new HashSet<>(interfaceOperKeys), new HashSet<>(interfaceKeys));
        interfaceKeys.addAll(hidden);
        return interfaceKeys;
    }

    @VisibleForTesting
    List<InterfaceKey> parseInterfaceIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            IFC_CFG_LINE::matcher,
            matcher -> matcher.group("id"),
            InterfaceKey::new);
    }

    @VisibleForTesting
    List<InterfaceKey> parseOperInterfaceIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            IFC_OPER_LINE::matcher,
            matcher -> matcher.group("id"),
            number -> new InterfaceKey("ethernet " + number));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> id,
                                      @NotNull InterfaceBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        builder.setName(id.firstKeyOf(Interface.class).getName());
    }
}