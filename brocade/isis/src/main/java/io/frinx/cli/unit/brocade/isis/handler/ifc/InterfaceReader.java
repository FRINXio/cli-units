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

package io.frinx.cli.unit.brocade.isis.handler.ifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String ISIS_IFC = "show running-config | include ^interface "
            + "|^ ip router isis";
    private static final Pattern ISIS_IFC_NAME = Pattern.compile("interface (?<ifcName>\\S+ \\S+)"
            + " ip router isis.*");
    private static final Pattern ISIS_SPLITTER = Pattern.compile("\n ");

    private final Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(ISIS_IFC, cli, instanceIdentifier, readContext);
        return ParsingUtils.parseFields(ISIS_SPLITTER.matcher(output).replaceAll(" "), 0,
            ISIS_IFC_NAME::matcher,
            g -> g.group("ifcName"),
            name -> new InterfaceKey(new InterfaceId(name)));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        interfaceBuilder.setInterfaceId(interfaceId);
    }
}
