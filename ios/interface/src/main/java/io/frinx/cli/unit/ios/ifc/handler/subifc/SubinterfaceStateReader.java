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

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import static io.frinx.cli.unit.ios.ifc.handler.InterfaceConfigReader.DESCR_LINE;
import static io.frinx.cli.unit.ios.ifc.handler.InterfaceStateReader.SH_SINGLE_INTERFACE;
import static io.frinx.cli.unit.ios.ifc.handler.InterfaceStateReader.STATUS_LINE;
import static io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;
import static io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader.getSubinterfaceName;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceStateReader implements CliOperReader<State, StateBuilder> {

    private final Cli cli;

    public SubinterfaceStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> id, @Nonnull StateBuilder builder, @Nonnull
            ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        // Only parse configuration for non 0 subifc
        if (subKey.getIndex() == ZERO_SUBINTERFACE_ID) {
            return;
        }

        String subIfcName = getSubinterfaceName(id);

        String cmd = String.format(SH_SINGLE_INTERFACE, subIfcName);
        parseInterfaceState(blockingRead(cmd, cli, id, ctx), builder, subKey.getIndex(), subIfcName);
    }

    @VisibleForTesting
    static void parseInterfaceState(final String output, final StateBuilder builder, Long index, String name) {
        builder.setName(name);
        builder.setIndex(index);

        parseField(output,
                STATUS_LINE::matcher,
            matcher -> InterfaceCommonState.AdminStatus.valueOf(matcher.group("admin").toUpperCase()),
            adminStatus -> {
                builder.setAdminStatus(adminStatus);
                builder.setEnabled(adminStatus == InterfaceCommonState.AdminStatus.UP);
            });

        parseField(output,
                STATUS_LINE::matcher,
            matcher -> InterfaceCommonState.OperStatus.valueOf(matcher.group("line").toUpperCase()),
                builder::setOperStatus);

        parseField(output,
                DESCR_LINE::matcher,
            matcher -> matcher.group("desc"),
                builder::setDescription);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull State readValue) {
        ((SubinterfaceBuilder) parentBuilder).setState(readValue);
    }
}
