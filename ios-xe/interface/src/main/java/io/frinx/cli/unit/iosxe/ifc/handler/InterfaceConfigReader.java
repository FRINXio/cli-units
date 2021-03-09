/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigReader;
import io.frinx.cli.unit.iosxe.ifc.Util;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class InterfaceConfigReader extends AbstractInterfaceConfigReader {

    public static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface %s";

    public static final Pattern SHUTDOWN_LINE = Pattern.compile("shutdown");
    public static final Pattern DESCRIPTION_LINE = Pattern.compile("description (?<desc>.+)");
    private static final Pattern MTU_LINE = Pattern.compile("mtu (?<mtu>.+)");
    private static final Pattern STORM_CONTROL_LINE =
            Pattern.compile("storm-control (?<address>\\S+) level (?<level>.+)");
    private static final Pattern MEDIA_TYPE_LINE = Pattern.compile("media-type (?<mediaType>.+)");
    private static final Pattern LLDP_TRANSMIT_LINE = Pattern.compile("no lldp transmit");
    private static final Pattern LLDP_RECEIVE_LINE = Pattern.compile("no lldp receive");
    private static final Pattern SERVICE_INSTANCE_LINE =
            Pattern.compile("service instance(?<trunk> trunk)? (?<id>\\d+) ethernet( (?<evc>\\S+))?.*");
    private static final Pattern SERVICE_INSTANCE_ENCAPSULATION_LINE =
            Pattern.compile("encapsulation (untagged)?( , )?(dot1q (?<ids>.+))?");

    public InterfaceConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    public void parseInterface(String output, ConfigBuilder builder, String name) {
        super.parseInterface(output, builder, name);

        final IfCiscoExtAugBuilder ifCiscoExtAugBuilder = new IfCiscoExtAugBuilder();
        setStormControl(output, ifCiscoExtAugBuilder);
        setLldpTransmit(output, ifCiscoExtAugBuilder);
        if (Util.isPhysicalInterface(builder.getType())) {
            setLldpReceive(output, ifCiscoExtAugBuilder);
            setServiceInstance(output, ifCiscoExtAugBuilder);
        }
        if (isCiscoExtAugNotEmpty(ifCiscoExtAugBuilder)) {
            builder.addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build());
        }

        final IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        setMode(output, ifSaosAugBuilder);
        if (isSaosAugNotEmpty(ifSaosAugBuilder)) {
            builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        }
    }

    private void setLldpReceive(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        ifCiscoExtAugBuilder.setLldpReceive(!LLDP_RECEIVE_LINE.matcher(output).find());
    }

    private void setLldpTransmit(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        ifCiscoExtAugBuilder.setLldpTransmit(!LLDP_TRANSMIT_LINE.matcher(output).find());
    }

    private boolean isCiscoExtAugNotEmpty(final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        return !ifCiscoExtAugBuilder.build().equals((new IfCiscoExtAugBuilder()).build());
    }

    private Boolean isSaosAugNotEmpty(final IfSaosAugBuilder ifSaosAugBuilder) {
        return !ifSaosAugBuilder.build().equals((new IfSaosAugBuilder()).build());
    }

    private void setMode(final String output, final IfSaosAugBuilder ifSaosAugBuilder) {
        final Optional<String> mode = ParsingUtils.parseField(output, 0,
            MEDIA_TYPE_LINE::matcher,
            matcher -> matcher.group("mediaType"));

        if (mode.isPresent()) {
            final PhysicalType physicalType;
            switch (mode.get()) {
                case "auto-select":
                    physicalType = PhysicalType.Default;
                    break;
                case "rj45":
                    physicalType = PhysicalType.Rj45;
                    break;
                case "sfp":
                    physicalType = PhysicalType.Sfp;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Mode value: " + mode.get());
            }
            ifSaosAugBuilder.setPhysicalType(physicalType);
        }
    }

    private void setStormControl(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        final List<StormControl> stormControls = new ArrayList<>();
        ParsingUtils.parseFields(output, 0,
            STORM_CONTROL_LINE::matcher,
            Matcher::group,
            m -> stormControls.add(parseStormControlLine(m)));

        if (!stormControls.isEmpty()) {
            ifCiscoExtAugBuilder.setStormControl(stormControls);
        }
    }

    private StormControl parseStormControlLine(final String line) {
        final Optional<String> address = ParsingUtils.parseField(line, 0,
            STORM_CONTROL_LINE::matcher,
            matcher -> matcher.group("address"));
        final Optional<String> level = ParsingUtils.parseField(line, 0,
            STORM_CONTROL_LINE::matcher,
            matcher -> matcher.group("level"));

        if (address.isPresent() && level.isPresent()) {
            final StormControlBuilder builder = new StormControlBuilder();
            builder.setAddress(Util.getStormControlAddress(address.get()));
            builder.setKey(new StormControlKey(builder.getAddress()));
            builder.setLevel(new BigDecimal(level.get()));
            return builder.build();
        }

        return null;
    }

    private void setServiceInstance(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        final List<ServiceInstance> serviceInstanceList = ParsingUtils.parseFields(output, 0,
            SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("id"),
            id -> parseServiceInstance(id, output));
        if (serviceInstanceList.size() > 0) {
            final ServiceInstances serviceInstances = new ServiceInstancesBuilder()
                    .setServiceInstance(serviceInstanceList)
                    .build();
            ifCiscoExtAugBuilder.setServiceInstances(serviceInstances);
        }
    }

    private ServiceInstance parseServiceInstance(final String id, final String output) {
        final String extracted = Util.extractServiceInstance(id, output);

        final Optional<String> trunk = ParsingUtils.parseField(extracted, 0,
            SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("trunk"));
        final Optional<String> evc = ParsingUtils.parseField(extracted, 0,
            SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("evc"));

        final ServiceInstanceBuilder serviceInstanceBuilder = new ServiceInstanceBuilder();
        serviceInstanceBuilder.setId(Long.valueOf(id));
        serviceInstanceBuilder.setKey(new ServiceInstanceKey(serviceInstanceBuilder.getId()));
        serviceInstanceBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                .cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder()
                .setId(serviceInstanceBuilder.getId())
                .setTrunk(trunk.isPresent())
                .setEvc(evc.orElse(null))
                .setEncapsulation(parseServiceInstanceEncapsulation(extracted))
                .build());
        return serviceInstanceBuilder.build();
    }

    private Encapsulation parseServiceInstanceEncapsulation(final String output) {
        final Optional<String> encapsulationLine = ParsingUtils.parseField(output, 0,
            SERVICE_INSTANCE_ENCAPSULATION_LINE::matcher,
            Matcher::group);

        if (encapsulationLine.isPresent()) {
            final EncapsulationBuilder encapsulationBuilder = new EncapsulationBuilder();
            encapsulationBuilder.setUntagged(encapsulationLine.get().contains("untagged"));

            final Optional<String> dot1qLine = ParsingUtils.parseField(output, 0,
                SERVICE_INSTANCE_ENCAPSULATION_LINE::matcher,
                matcher -> matcher.group("ids"));
            dot1qLine.ifPresent(s -> encapsulationBuilder.setDot1q(splitMultipleVlans(Arrays.asList(s.split(",")))));

            return encapsulationBuilder.build();
        }

        return null;
    }

    private List<Integer> splitMultipleVlans(final List<String> vlanStrings) {
        final List<Integer> splitVlans = new ArrayList<>();

        for (final String vlanString : vlanStrings) {
            if (vlanString.contains("-")) {
                final List<Integer> list = Arrays.stream(vlanString.split("-"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                final IntStream stream = IntStream.range(list.get(0), list.get(1) + 1);
                stream.forEach(splitVlans::add);
            } else {
                splitVlans.add(Integer.parseInt(vlanString));
            }
        }

        return splitVlans;
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_SINGLE_INTERFACE_CFG, ifcName);
    }

    @Override
    protected Pattern getShutdownLine() {
        return SHUTDOWN_LINE;
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCRIPTION_LINE;
    }

    @Override
    public Class<? extends InterfaceType> parseType(String name) {
        return Util.parseType(name);
    }

}