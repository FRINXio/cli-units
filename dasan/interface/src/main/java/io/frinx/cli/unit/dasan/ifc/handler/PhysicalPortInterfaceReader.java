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

package io.frinx.cli.unit.dasan.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortInterfaceReader
        implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    private Cli cli;

    public PhysicalPortInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SHOW_INSTALLED_ETHER_PORTS = "show port | include Ethernet .*Y$";

    private static final Pattern ETHER_PORTS_LINE =
        Pattern.compile("^(?<id>[1-9][0-9]*/[1-9][0-9]*)\\s+Ethernet\\s.*");

    public static final String PHYSICAL_PORT_NAME_PREFIX = "Ethernet";

    public static final Pattern PHYSICAL_PORT_NAME_PATTERN =
        Pattern.compile(PHYSICAL_PORT_NAME_PREFIX + "(?<portid>[1-9][0-9]*/[1-9][0-9]*)$");

    public static final Pattern PHYSICAL_PORT_SPRITED_NAME_PATTERN =
        Pattern.compile("^(?<slot>[1-9][0-9]*)/(?<port>[1-9][0-9]*)$");

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(SHOW_INSTALLED_ETHER_PORTS, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(String output) {
        return ParsingUtils.parseFields(
            output,
            0,
            ETHER_PORTS_LINE::matcher,
            m -> PHYSICAL_PORT_NAME_PREFIX + m.group("id"),
            InterfaceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}