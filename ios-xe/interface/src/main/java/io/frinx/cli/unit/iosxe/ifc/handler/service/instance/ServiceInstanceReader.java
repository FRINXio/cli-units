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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceReader
        implements CliConfigListReader<ServiceInstance, ServiceInstanceKey, ServiceInstanceBuilder> {

    private static final String SH_SERVICE_INSTANCES =
            InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG + " | include service instance";

    public static final Pattern SERVICE_INSTANCE_LINE =
            Pattern.compile("service instance(?<trunk> trunk)? (?<id>\\d+) ethernet( (?<evc>\\S+))?.*");

    private final Cli cli;

    public ServiceInstanceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ServiceInstanceKey> getAllIds(@Nonnull InstanceIdentifier<ServiceInstance> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final String ifcOutput = blockingRead(f(SH_SERVICE_INSTANCES, ifcName), cli, instanceIdentifier, readContext);
        return parseIds(ifcOutput);
    }

    public static List<ServiceInstanceKey> parseIds(final String ifcOutput) {
        return ParsingUtils.parseFields(ifcOutput, 0,
            SERVICE_INSTANCE_LINE::matcher,
            matcher -> matcher.group("id"),
            id -> new ServiceInstanceKey(Long.parseLong(id)));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ServiceInstance> instanceIdentifier,
                                      @Nonnull ServiceInstanceBuilder serviceInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        serviceInstanceBuilder.setId(serviceInstanceId);
        serviceInstanceBuilder.setKey(new ServiceInstanceKey(serviceInstanceId));
    }

}
