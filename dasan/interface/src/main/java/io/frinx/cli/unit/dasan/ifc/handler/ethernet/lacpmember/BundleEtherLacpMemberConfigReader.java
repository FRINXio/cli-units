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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember;

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
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpMemberConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String AGGREGATE_IFC_NAME = "Bundle-Ether";
    @VisibleForTesting
    public static final String SHOW_LACP_PORT = "show running-config bridge | include ^ lacp port";
    private static final Pattern LACP_PORT_LINE_PATTERN = Pattern
            .compile("lacp port (?<ports>[^\\s]+)\\s+aggregator\\s+(?<id>[0-9]+)$");

    private final Cli cli;

    public BundleEtherLacpMemberConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull ConfigBuilder builder,
            @NotNull ReadContext ctx) throws ReadFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);

        if (!matcher.matches()) {
            return;
        }

        if (!PhysicalPortInterfaceConfigWriter.PHYS_IFC_TYPES.contains(InterfaceReader.parseTypeByName(ifcName))) {
            return;
        }
        String portId = matcher.group("portid");
        Optional<String> lacpId = getAssignedLacpId(portId, this, cli, id, ctx);

        lacpId.ifPresent(s -> {
            Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
            ethIfAggregationConfigBuilder.setAggregateId(getLAGInterfaceId(s));
            builder.addAugmentation(Config1.class, ethIfAggregationConfigBuilder.build());
        });
    }

    public static <O extends DataObject> Optional<String> getAssignedLacpId(
            String portId,
            CliReader<O, ? extends Builder<O>> cliReader,
            Cli cli,
            InstanceIdentifier<O> id,
            ReadContext ctx) throws ReadFailedException {

        List<String> ports = DasanCliUtil.getPhysicalPorts(cli, cliReader, id, ctx);

        return getAssignedLacpId(portId, ports, cliReader, cli, id, ctx);
    }

    public static <O extends DataObject> Optional<String> getAssignedLacpId(
            String portId,
            List<String> ports,
            CliReader<O, ? extends Builder<O>> cliReader,
            Cli cli,
            InstanceIdentifier<O> id,
            ReadContext ctx) throws ReadFailedException {

        String output = cliReader.blockingRead(SHOW_LACP_PORT, cli, id, ctx);

        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(LACP_PORT_LINE_PATTERN::matcher)
                .filter(Matcher::matches)
                .filter(m -> DasanCliUtil.containsPort(ports, m.group("ports"), portId))
                .map(m -> m.group("id"))
                .findFirst();
    }

    public static <O extends DataObject> List<String> getPortMembers(
        String lacpId,
        CliReader<O, ? extends Builder<O>> cliReader,
        Cli cli,
        InstanceIdentifier<O> id,
        ReadContext ctx) throws ReadFailedException {

        List<String> ports = DasanCliUtil.getPhysicalPorts(cli, cliReader, id, ctx);

        return getPortMembers(lacpId, ports, cliReader, cli, id,ctx);
    }

    public static <O extends DataObject> List<String> getPortMembers(
        String lacpId,
        List<String> ports,
        CliReader<O, ? extends Builder<O>> cliReader,
        Cli cli,
        InstanceIdentifier<O> id,
        ReadContext ctx) throws ReadFailedException {

        String output = cliReader.blockingRead(SHOW_LACP_PORT, cli, id, ctx);

        return ParsingUtils.NEWLINE.splitAsStream(output)
            .map(String::trim)
            .map(LACP_PORT_LINE_PATTERN::matcher)
            .filter(Matcher::matches)
            .filter(m -> lacpId.equals(m.group("id")))
            .flatMap(m -> DasanCliUtil.parsePortRanges(ports, m.group("ports")).stream())
            .collect(Collectors.toList());
    }

    private static String getLAGInterfaceId(String bundleId) {
        return AGGREGATE_IFC_NAME + bundleId;
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