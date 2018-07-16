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

package io.frinx.cli.unit.brocade.cdp.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_CDP_INTER = "sh run | include ^interface|cdp";

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_CDP_INTER, cli, instanceIdentifier, readContext);
        return parseCdpInterfaces(output);
    }

    private static final Pattern CDP_INTER_LINE = Pattern.compile("interface (?<ifcType>\\S+)\\s+(?<ifcNumber>\\S+)"
            + "\\s*(?<isCdp>no cdp enable)?.*");

    @VisibleForTesting
    static List<InterfaceKey> parseCdpInterfaces(String output) {
        // The output needs to be preprocessed, so put everything on 1 line
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), " ");
        // And then split per neighbor
        String linePerInterfaceOutput = withoutNewlines.replace("interface", "\ninterface");

        return NEWLINE.splitAsStream(linePerInterfaceOutput)
                .map(String::trim)
                .map(CDP_INTER_LINE::matcher)
                .filter(Matcher::matches)
                // only filter interfaces with cdp
                .filter(matcher -> matcher.group("isCdp") == null)
                .map(matcher -> matcher.group("ifcType") + matcher.group("ifcNumber"))
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Nonnull
    @Override
    public InterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Interface> instanceIdentifier) {
        return new InterfaceBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        // TODO check reading existing interface
        interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}
