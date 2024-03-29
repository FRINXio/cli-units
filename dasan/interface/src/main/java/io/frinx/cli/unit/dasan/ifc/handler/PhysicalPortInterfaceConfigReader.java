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
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState.AdminStatus;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    @VisibleForTesting
    static final String SH_SINGLE_INTERFACE_CFG = "show port %s | include ^%s ";  //Need the last space.

    static final Pattern INTERFACE_ID_LINE =
        Pattern.compile(
            "^(?<id>[^\\s]+)\\s+(?<porttype>[^\\s]+)\\s+(?<pvid>[\\d]+)\\s+(?<stateadmin>[^/]+)/(?<stateoper>\\S+).*");

    @VisibleForTesting
    static final String SHOW_JUMBO_FRAME = "show running-config bridge | include jumbo-frame ";

    private static final Pattern JUMBO_FRAME_LINE =
        Pattern.compile("^jumbo-frame (?<ports>[^\\s]+)\\s+(?<size>[\\d]+)$");

    private Cli cli;

    public PhysicalPortInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(name);

        if (!matcher.matches()) {
            return;
        }

        String portId = matcher.group("portid");
        parseInterface(blockingRead(f(SH_SINGLE_INTERFACE_CFG, portId, portId), cli, id, ctx), builder, name);

        List<String> ports = DasanCliUtil.getPhysicalPorts(cli, this, id, ctx);
        parseJumboFrame(blockingRead(SHOW_JUMBO_FRAME, cli, id, ctx), ports, portId, builder);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder, String name) {
        // Set disabled unless proven otherwise
        builder.setName(name);
        builder.setEnabled(false);
        builder.setType(Other.class);

        Matcher lineMatcher = INTERFACE_ID_LINE.matcher(output);

        if (!lineMatcher.matches()) {
            return;
        }

        builder.setType(EthernetCsmacd.class);

        AdminStatus adminStatus =
            InterfaceCommonState.AdminStatus.valueOf(lineMatcher.group("stateadmin").toUpperCase(Locale.ROOT));

        builder.setEnabled(adminStatus == InterfaceCommonState.AdminStatus.UP);
    }

    @VisibleForTesting
    static void parseJumboFrame(String output, List<String> ports, String name, ConfigBuilder builder) {

        ParsingUtils.NEWLINE.splitAsStream(output)
            .map(String::trim)
            .map(JUMBO_FRAME_LINE::matcher)
            .filter(Matcher::matches)
            .filter(m -> DasanCliUtil.containsPort(ports, m.group("ports"), name))
            .map(m -> m.group("size"))
            .findFirst()
            .ifPresent(s -> builder.setMtu(Integer.valueOf(s)));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}