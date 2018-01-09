/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.ifc.handler;

import static io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigReader.parseType;
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
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceStateReader implements CliOperReader<State, StateBuilder> {

    private Cli cli;

    public InterfaceStateReader(Cli cli) {
        this.cli = cli;
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
        parseInterfaceState(blockingRead(String.format(DISPLAY_SINGLE_INTERFACE, name), cli, id, ctx), builder, name);
    }

    private static final String DISPLAY_SINGLE_INTERFACE = "display inter %s";

    private static final Pattern ADMIN_STATUS_LINE =
            Pattern.compile("(?<id>\\S+) current state : (?<admin>UP|DOWN).*");
    private static final Pattern OPER_STATUS_LINE =
            Pattern.compile("\\s*Line protocol current state : (?<oper>UP|DOWN).*");

    private static final Pattern MTU_LINE = Pattern.compile(".*The Maximum Transmit Unit is (?<mtu>.+).*$");
    private static final Pattern DESCR_LINE = Pattern.compile("\\s*Description: (?<desc>.+)\\s*");

    @VisibleForTesting
    // TODO Parametrize logs with instanceId
    static void parseInterfaceState(final String output, final StateBuilder builder, final String name) {
        builder.setName(name);
        builder.setType(parseType(name));

        parseField(output,
                ADMIN_STATUS_LINE::matcher,
                matcher -> InterfaceCommonState.AdminStatus.valueOf(matcher.group("admin").toUpperCase()),
                adminStatus -> {
                    builder.setAdminStatus(adminStatus);
                    builder.setEnabled(adminStatus == InterfaceCommonState.AdminStatus.UP);
                });

        if (builder.getAdminStatus() == null) {
            // We cannot parse AdminStatus from output, fallback to AdminStatus.DOWN
            builder.setAdminStatus(InterfaceCommonState.AdminStatus.DOWN);
            builder.setEnabled(false);
        }

        parseField(output,
                OPER_STATUS_LINE::matcher,
                matcher -> InterfaceCommonState.OperStatus.valueOf(matcher.group("oper").toUpperCase()),
                builder::setOperStatus);

        if (builder.getOperStatus() == null) {
            // We cannot parse OperStatus from output, fallback to OperStatus.Unknown
            builder.setOperStatus(InterfaceCommonState.OperStatus.UNKNOWN);
        }

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
