/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    public static final String SH_INTERFACE = "sh ip inter brie";

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(SH_INTERFACE, cli, instanceIdentifier, readContext));
    }

    private static final Pattern INTERFACE_ID_LINE =
            Pattern.compile("(?<id>[^\\s]+)\\s+(?<ipAddr>[^\\s]+)\\s+(?<status>[^\\s]+)\\s+(?<protocol>[^\\s]+)\\s+(?<vrfName>[^\\s]+).*");

    public static final Pattern SUBINTERFACE_NAME =
            Pattern.compile("(?<ifcId>.+)[.](?<subifcIndex>[0-9]+)");

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(String output) {
        return parseAllInterfaceIds(output)
                // Now exclude subinterfaces
                .stream()
                .filter(ifcName -> !isSubinterface(ifcName))
                .collect(Collectors.toList());
    }

    public static List<InterfaceKey> parseAllInterfaceIds(String output) {
        return NEWLINE.splitAsStream(output)
                .map(String::trim)
                // Remove interfaces marked as deleted ! (deleted subinterface is still reported in the output as deleted)
                .filter(line -> !line.contains("deleted"))
                .map(INTERFACE_ID_LINE::matcher)
                .filter(Matcher::matches)
                .skip(2)
                .map(m -> m.group("id"))
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    public static boolean isSubinterface(InterfaceKey ifcName) {
        return SUBINTERFACE_NAME.matcher(ifcName.getName()).matches();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Nonnull
    @Override
    public InterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Interface> instanceIdentifier) {
        return new InterfaceBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }

}
