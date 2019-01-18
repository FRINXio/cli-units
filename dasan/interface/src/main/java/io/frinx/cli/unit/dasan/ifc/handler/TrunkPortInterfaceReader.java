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

public class TrunkPortInterfaceReader
        implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    @VisibleForTesting
    static final String SHOW_ETHERNET_PORTS = "show port | include ^t/";

    private static final Pattern ETHER_PORTS_LINE =
        Pattern.compile("^t/(?<id>[1-9][0-9]*)\\s.*");

    public static final String TRUNK_IF_NAME_PREFIX = "Trunk";

    private Cli cli;

    public TrunkPortInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(SHOW_ETHERNET_PORTS, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(String output) {
        List<InterfaceKey> list = ParsingUtils.parseFields(
            output,
            0,
            ETHER_PORTS_LINE::matcher,
            m -> TRUNK_IF_NAME_PREFIX + m.group("id"),
            InterfaceKey::new);
        return list;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}