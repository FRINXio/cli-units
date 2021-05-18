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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.ifc.Util;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern SERVICE_INSTANCE_ENCAPSULATION_LINE =
        Pattern.compile("encapsulation (untagged)?( , )?(dot1q (?<ids>.+))?");

    private final Cli cli;

    public ServiceInstanceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final String ifcOutput = blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName),
                cli, instanceIdentifier, readContext);
        final Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        final String serviceInstanceOutput = Util.extractServiceInstance(serviceInstanceId, ifcOutput);
        parseConfig(serviceInstanceOutput, serviceInstanceId, configBuilder);
    }

    public static void parseConfig(final String output,
                                   final Long serviceInstanceId,
                                   final ConfigBuilder configBuilder) {
        configBuilder.setId(serviceInstanceId);

        final Optional<String> trunk = ParsingUtils.parseField(output, 0,
            ServiceInstanceReader.SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("trunk"));
        configBuilder.setTrunk(trunk.isPresent());

        final Optional<String> evc = ParsingUtils.parseField(output, 0,
            ServiceInstanceReader.SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("evc"));
        configBuilder.setEvc(evc.orElse(null));

        configBuilder.setEncapsulation(parseServiceInstanceEncapsulation(output));
    }

    private static Encapsulation parseServiceInstanceEncapsulation(final String output) {
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

    private static List<Integer> splitMultipleVlans(final List<String> vlanStrings) {
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

}