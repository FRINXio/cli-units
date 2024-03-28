/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ifc.base.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractInterfaceStateReader implements CliOperReader<State, StateBuilder>  {

    private Cli cli;

    protected AbstractInterfaceStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<State> id,
                                      @NotNull final StateBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        parseInterfaceState(blockingRead(getReadCommand(ifcName), cli, id, ctx), builder, ifcName);
    }

    protected abstract String getReadCommand(String ifcName);

    protected void parseInterfaceState(String output, StateBuilder builder, String name) {
        builder.setName(name);

        ParsingUtils.parseField(output,
            getAdminStatusLine()::matcher,
            matcher -> InterfaceCommonState.AdminStatus.valueOf(matcher.group("admin").toUpperCase(Locale.ROOT)),
            adminStatus -> {
                builder.setAdminStatus(adminStatus);
                builder.setEnabled(adminStatus == InterfaceCommonState.AdminStatus.UP);
            });

        if (builder.getAdminStatus() == null) {
            // We cannot parse AdminSatus from output, fallback to AdminStatus.DOWN
            builder.setAdminStatus(InterfaceCommonState.AdminStatus.DOWN);
            builder.setEnabled(false);
        }

        ParsingUtils.parseField(output,
            getOperStatusLine()::matcher,
            matcher -> InterfaceCommonState.OperStatus.valueOf(matcher.group("oper").toUpperCase(Locale.ROOT)),
            builder::setOperStatus);

        if (builder.getOperStatus() == null) {
            // We cannot parse OperSatus from output, fallback to OperStatus.Unknown
            builder.setOperStatus(InterfaceCommonState.OperStatus.UNKNOWN);
        }

        ParsingUtils.parseField(output,
            getMtuLine()::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);

        ParsingUtils.parseField(output,
            getDescriptionLine()::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);
    }

    protected abstract Pattern getMtuLine();

    protected abstract Pattern getAdminStatusLine();

    protected abstract Pattern getOperStatusLine();

    protected abstract Pattern getDescriptionLine();
}