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
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_SERVICE_INSTANCE =
            InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG + " | section service instance.* %s ethernet";

    private final Cli cli;

    public ServiceInstanceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseConfig(serviceInstanceOutput, serviceInstanceId, configBuilder);
    }

    public static void parseConfig(final String output,
                                   final Long serviceInstanceId,
                                   final ConfigBuilder configBuilder) {
        configBuilder.setId(serviceInstanceId);

        ParsingUtils.parseField(output, 0,
            ServiceInstanceReader.SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("trunk"),
            value -> configBuilder.setTrunk(true));

        ParsingUtils.parseField(output, 0,
            ServiceInstanceReader.SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("evc"),
            configBuilder::setEvc);
    }
}