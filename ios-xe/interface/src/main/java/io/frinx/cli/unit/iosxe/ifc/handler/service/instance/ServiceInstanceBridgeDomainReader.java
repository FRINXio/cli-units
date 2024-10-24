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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.BridgeDomain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.BridgeDomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceBridgeDomainReader implements CliConfigReader<BridgeDomain, BridgeDomainBuilder> {

    private static final Pattern BRIDGE_DOMAIN_LINE =
            Pattern.compile("bridge-domain (?<value>\\S+)( split-horizon group (?<group>\\d+))?.*");

    private final Cli cli;

    public ServiceInstanceBridgeDomainReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<BridgeDomain> instanceIdentifier,
                                      @NotNull BridgeDomainBuilder bridgeDomainBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(ServiceInstanceConfigReader.SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseBridgeDomain(serviceInstanceOutput, bridgeDomainBuilder);
    }

    public static void parseBridgeDomain(final String output, final BridgeDomainBuilder bridgeDomainBuilder) {
        ParsingUtils.parseField(output, 0,
            BRIDGE_DOMAIN_LINE::matcher,
            matcher -> matcher.group("value"),
            bridgeDomainBuilder::setValue);

        ParsingUtils.parseField(output, 0,
            BRIDGE_DOMAIN_LINE::matcher,
            matcher -> matcher.group("group"),
            number -> bridgeDomainBuilder.setGroupNumber(Short.parseShort(number)));
    }
}