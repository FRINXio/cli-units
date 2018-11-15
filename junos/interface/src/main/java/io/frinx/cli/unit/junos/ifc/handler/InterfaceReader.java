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

package io.frinx.cli.unit.junos.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SHOW_INTERFACES = "show configuration interfaces | display set";

    private static final Pattern INTERFACE_ID_LINE = Pattern.compile("set interfaces (?<id>[\\S]+) .*");

    private static final Pattern SUBINTERFACE_ID_LINE = Pattern.compile("set interfaces (?<id>[\\S]+ unit [0-9]+).*");

    private static final Pattern SUBINTERFACE_NAME = Pattern.compile("(?<ifcId>[\\S]+) unit (?<subifcIndex>[0-9]+)");

    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        List<InterfaceKey> ids = parseInterfaceIds(blockingRead(SHOW_INTERFACES, cli, instanceIdentifier, readContext));

        return ids.stream().distinct().collect(Collectors.toList());
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(String output) {
        return parseAllInterfaceIds(output)
            // Now exclude subinterfaces
            .stream().filter(ifcName -> !isSubinterface(ifcName)).collect(Collectors.toList());
    }

    public static List<InterfaceKey> parseAllInterfaceIds(String output) {
        return Stream
            .concat(
                ParsingUtils.parseFields(output, 0, INTERFACE_ID_LINE::matcher, matcher -> matcher.group("id"),
                    InterfaceKey::new).stream(),
                ParsingUtils.parseFields(output, 0, SUBINTERFACE_ID_LINE::matcher, matcher -> matcher.group("id"),
                    InterfaceKey::new).stream())
            .collect(Collectors.toList());
    }

    public static boolean isSubinterface(InterfaceKey ifcName) {
        return SUBINTERFACE_NAME.matcher(ifcName.getName()).matches();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
        @Nonnull InterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}
