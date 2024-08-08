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
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState.AdminStatus;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TrunkPortInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    @VisibleForTesting
    static final Pattern TRUNK_IF_NAME_PATTERN = Pattern.compile("Trunk(?<portid>[1-9][0-9]*)$");

    private Cli cli;

    public TrunkPortInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = TRUNK_IF_NAME_PATTERN.matcher(name);

        if (!matcher.matches()) {
            return;
        }

        String portId = "t/" + matcher.group("portid");
        parseInterface(blockingRead(f(PhysicalPortInterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, portId, portId),
                cli, id, ctx), builder, name);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder, String name) {
        // Set disabled unless proven otherwise
        builder.setName(name);
        builder.setEnabled(false);
        builder.setType(Other.class);

        Matcher lineMatcher = PhysicalPortInterfaceConfigReader.INTERFACE_ID_LINE.matcher(output);

        if (!lineMatcher.matches()) {
            return;
        }

        builder.setType(Ieee8023adLag.class);

        AdminStatus adminStatus =
            InterfaceCommonState.AdminStatus.valueOf(lineMatcher.group("stateadmin").toUpperCase(Locale.ROOT));

        builder.setEnabled(adminStatus == InterfaceCommonState.AdminStatus.UP);
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}