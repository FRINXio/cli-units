/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.ifc.handler;

import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getIfcNumber;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.parseType;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceStateReader implements CliOperReader<State, StateBuilder> {

    private Cli cli;

    public InterfaceStateReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public StateBuilder getBuilder(@Nonnull final InstanceIdentifier<State> id) {
        return new StateBuilder();
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> builder, @Nonnull final State value) {
        ((InterfaceBuilder) builder).setState(value);
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<State> id,
                                      @Nonnull final StateBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = parseType(name);
        String ifcTypeOnDevice = InterfaceConfigReader.getTypeOnDevice(ifcType);
        String ifcNumber = getIfcNumber(name);
        parseInterfaceState(blockingRead(String.format(SH_SINGLE_INTERFACE, ifcTypeOnDevice, ifcNumber), cli, id, ctx), builder, name, ifcType);
    }

    public static final String SH_SINGLE_INTERFACE = "sh inter %s %s";

    public static final Pattern STATUS_LINE =
            Pattern.compile("(?<id>.+)[.\\s]* is (?<admin>[^,]+), line protocol is (?<line>.+)");
    public static final Pattern MTU_LINE = Pattern.compile("\\s*MTU (?<mtu>.+) bytes.*$");
    public static final Pattern DESCR_LINE = Pattern.compile("\\s*Port name is (?<desc>.+)");

    @VisibleForTesting
    static void parseInterfaceState(final String output, final StateBuilder builder, final String name, Class<? extends InterfaceType> type) {
        builder.setName(name);
        builder.setType(type);

        parseField(output,
                STATUS_LINE::matcher,
                matcher -> {
                    switch (matcher.group("admin").toUpperCase()) {
                        case "UP" : return InterfaceCommonState.AdminStatus.UP;
                        case "DOWN" : return InterfaceCommonState.AdminStatus.DOWN;
                        default: return InterfaceCommonState.AdminStatus.DOWN;
                    }
                },
                adminStatus -> {
                    builder.setAdminStatus(adminStatus);
                    builder.setEnabled(adminStatus == InterfaceCommonState.AdminStatus.UP);
                });

        parseField(output,
                STATUS_LINE::matcher,
                matcher -> {
                    switch (matcher.group("line").toUpperCase()) {
                        case "UP" : return InterfaceCommonState.OperStatus.UP;
                        case "DOWN" : return InterfaceCommonState.OperStatus.DOWN;
                        default: return InterfaceCommonState.OperStatus.UNKNOWN;
                    }
                },
                builder::setOperStatus);

        parseField(output,
                MTU_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("mtu")),
                builder::setMtu);

        parseField(output,
                DESCR_LINE::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
    }

}
