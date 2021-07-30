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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceEncapsulationReader implements CliConfigReader<Encapsulation, EncapsulationBuilder> {

    private static final Pattern SERVICE_INSTANCE_ENCAPSULATION_LINE =
            Pattern.compile("encapsulation (untagged)?( , )?(dot1q (?<ids>.+))?");

    private final Cli cli;

    public ServiceInstanceEncapsulationReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Encapsulation> instanceIdentifier,
                                      @Nonnull EncapsulationBuilder encapsulationBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(ServiceInstanceConfigReader.SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseEncapsulation(serviceInstanceOutput, encapsulationBuilder);
    }

    public static void parseEncapsulation(final String output,
                                          final EncapsulationBuilder encapsulationBuilder) {
        ParsingUtils.parseField(output, 0,
            SERVICE_INSTANCE_ENCAPSULATION_LINE::matcher,
            Matcher::group,
            value -> encapsulationBuilder.setUntagged(value.contains("untagged")));

        ParsingUtils.parseField(output, 0,
            SERVICE_INSTANCE_ENCAPSULATION_LINE::matcher,
            matcher -> matcher.group("ids"),
            value -> encapsulationBuilder.setDot1q(Arrays.asList(value.replaceAll(" ", "")
                    .split(","))));
    }
}