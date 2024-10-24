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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpinterval;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.PhysicalPortInterfaceConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.PhysicalPortInterfaceReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpIntervalConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {
    @VisibleForTesting
    static final String SHOW_LACP_PORT = "show running-config bridge | include ^ lacp port";

    private static final Pattern TIME_OUT_LINE_PATTERN =
             Pattern.compile("lacp port timeout (?<ports>[^\\s]+)\\s+short$"); //only support "short"

    private final Cli cli;

    public BundleEtherLacpIntervalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);

        if (!matcher.matches()) {
            return;
        }
        String portId = matcher.group("portid");

        if (!PhysicalPortInterfaceConfigWriter.PHYS_IFC_TYPES.contains(InterfaceReader.parseTypeByName(ifcName))) {
            return;
        }
        List<String> ports = DasanCliUtil.getPhysicalPorts(cli, this, id, ctx);
        parseEthernetConfig(blockingRead(SHOW_LACP_PORT, cli, id, ctx), builder, ports, portId);
    }

    @VisibleForTesting
    static void parseEthernetConfig(String output, ConfigBuilder builder, List<String> ports, String portId) {

        LacpEthConfigAugBuilder c1b = new LacpEthConfigAugBuilder();
        ParsingUtils.NEWLINE.splitAsStream(output)
            .map(String::trim)
            .map(TIME_OUT_LINE_PATTERN::matcher)
            .filter(Matcher::matches)
            .filter(m -> DasanCliUtil.containsPort(ports, m.group("ports"), portId))
            .findFirst()
            .ifPresent(s -> c1b.setInterval(LacpPeriodType.FAST));

        if (c1b.getInterval() == null) {
            return;
        }
        builder.addAugmentation(LacpEthConfigAug.class, c1b.build());
    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> parentBuilder, @NotNull Config readValue) {
        ((EthernetBuilder) parentBuilder).setConfig(readValue);
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}