/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.snmp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    static final String SHOW_SNMP_INTERFACE = "show snmp interface notification regular-expression %s";
    static final Pattern INTERFACE_PATTERN = Pattern.compile("ifName : (?<interface>.+) linkUp/Down: (?<updown>.+)");

    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }


    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(String.format(SHOW_SNMP_INTERFACE, ".*"), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<InterfaceKey> parseInterfaceIds(String snmpInterfaceOutput) {
        return ParsingUtils.parseFields(snmpInterfaceOutput.replaceAll("\\h+", " "), 0,
                INTERFACE_PATTERN::matcher,
                matcher -> matcher.group("interface"),
                v -> new InterfaceKey(new InterfaceId(v)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        interfaceBuilder.setInterfaceId(instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId());
    }
}
